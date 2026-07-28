package com.nextgen.expend.network.localllm

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.ai.edge.litertlm.Backend
import com.google.ai.edge.litertlm.Content
import com.google.ai.edge.litertlm.Contents
import com.google.ai.edge.litertlm.ConversationConfig
import com.google.ai.edge.litertlm.Engine
import com.google.ai.edge.litertlm.EngineConfig
import com.google.ai.edge.litertlm.Message
import com.nextgen.expend.data.model.Category
import com.nextgen.expend.data.model.Transaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

/**
 * On-device LLM service powered by LiteRT (litertlm-android 0.14.0).
 *
 * Responsibilities:
 *  1. Copy and initialise the Granite model from assets.
 *  2. [parseNotification] — parse a raw bank notification into structured fields.
 *  3. [generateFinancialTip] — generate a short financial tip from recent transactions.
 */
class LocalLlmService(private val context: Context) {

    enum class Status {
        NOT_INITIALIZED,
        COPYING_MODEL,
        INITIALIZING,
        READY,
        INFERENCE,
        ERROR
    }

    /**
     * Result of the LLM parsing a bank notification text.
     * Any field the model couldn't determine is left null / empty.
     */
    data class ExtractedInfo(
        val amount: Double?,
        val currency: String?,   // "USD" or "KHR"
        val category: Category?,
        val remark: String       // merchant / description extracted by LLM
    )

    private val _status = MutableStateFlow(Status.NOT_INITIALIZED)
    val status: StateFlow<Status> = _status.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val modelFileName = "granite-4.0-350m_q8_ekv1280.litertlm"

    private var engine: Engine? = null

    // -------------------------------------------------------------------------
    //  Initialisation
    // -------------------------------------------------------------------------

    /**
     * Initialises the engine from whatever model file is currently on disk
     * (falling back to the bundled asset, if any). Pass [forceReload] to
     * re-run this after [loadModelFromUri] swaps the model file in place.
     */
    suspend fun initialize(forceReload: Boolean = false) = withContext(Dispatchers.IO) {
        if (_status.value == Status.READY && !forceReload) return@withContext

        try {
            val destFile = File(context.filesDir, modelFileName)

            // 1. Copy model from assets if missing (no-op when a model was
            //    already loaded onto disk via loadModelFromUri).
            if (!destFile.exists()) {
                Log.d(TAG, "Model file not found. Copying from assets...")
                _status.value = Status.COPYING_MODEL

                context.assets.open(modelFileName).use { input ->
                    FileOutputStream(destFile).use { output ->
                        val buffer = ByteArray(8 * 1024)
                        var bytesRead: Int
                        while (input.read(buffer).also { bytesRead = it } != -1) {
                            output.write(buffer, 0, bytesRead)
                        }
                        output.flush()
                    }
                }
                Log.d(TAG, "Model copied successfully.")
            }

            // 2. Initialise the LiteRT Engine (0.14.0: Backend.CPU is a data class)
            _status.value = Status.INITIALIZING
            Log.d(TAG, "Initializing LiteRT Engine...")

            engine?.close()

            val engineConfig = EngineConfig(
                modelPath = destFile.absolutePath,
                backend = Backend.CPU(),
                cacheDir = context.cacheDir.path
            )

            val newEngine = Engine(engineConfig)
            newEngine.initialize()
            engine = newEngine

            _status.value = Status.READY
            _errorMessage.value = null
            Log.d(TAG, "LiteRT Engine ready.")

        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize LiteRT Engine", e)
            _status.value = Status.ERROR
            _errorMessage.value = e.message ?: "Unknown initialization error"
        }
    }

    /**
     * Copies a user-picked `.litertlm` file (e.g. from a document picker) into
     * app storage and (re)initialises the engine against it. Lets the app ship
     * without bundling the multi-hundred-MB model in the APK/assets — the user
     * supplies it from local storage instead.
     */
    suspend fun loadModelFromUri(uri: Uri) = withContext(Dispatchers.IO) {
        try {
            _status.value = Status.COPYING_MODEL
            engine?.close()
            engine = null

            val destFile = File(context.filesDir, modelFileName)
            val input = context.contentResolver.openInputStream(uri)
                ?: throw IllegalStateException("Unable to open the selected file")

            input.use { stream ->
                FileOutputStream(destFile).use { output ->
                    val buffer = ByteArray(8 * 1024)
                    var bytesRead: Int
                    while (stream.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                    }
                    output.flush()
                }
            }

            Log.d(TAG, "Model loaded from local storage (${destFile.length()} bytes).")
            initialize(forceReload = true)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load model from picked file", e)
            _status.value = Status.ERROR
            _errorMessage.value = e.message ?: "Failed to load model"
        }
    }

    // -------------------------------------------------------------------------
    //  Notification parsing
    // -------------------------------------------------------------------------

    /**
     * Parses a raw bank notification using the on-device LLM.
     * Falls back to regex extraction if the engine is unavailable.
     */
    suspend fun parseNotification(rawText: String): ExtractedInfo = withContext(Dispatchers.IO) {
        if (engine == null) initialize()

        val currentEngine = engine ?: return@withContext fallbackParse(rawText)

        try {
            _status.value = Status.INFERENCE

            val categoryNames = Category.entries.joinToString(", ") { it.name }

            val prompt = """
                You are a financial transaction parser. Extract structured fields from the bank notification text below.

                NOTIFICATION:
                "$rawText"

                Reply ONLY with these four lines, no extra text:
                AMOUNT: <plain number such as 25.50, or UNKNOWN>
                CURRENCY: <USD or KHR, or UNKNOWN>
                CATEGORY: <one of $categoryNames, or UNKNOWN>
                REMARK: <merchant name or short description, or UNKNOWN>

                Rules:
                - AMOUNT must be a plain number with no currency symbols.
                - CURRENCY must be USD, KHR, or UNKNOWN.
                - CATEGORY must be exactly one of the listed values.
                - REMARK should be the merchant name or a concise description from the notification.
                - If a field cannot be determined, write UNKNOWN.
            """.trimIndent()

            Log.d(TAG, "parseNotification prompt:\n$prompt")

            // litertlm-android 0.14.0: ConversationConfig(systemInstruction: Contents)
            val config = ConversationConfig(Contents.of(
                "You are a precise financial transaction parser. " +
                        "Output only the four requested fields, nothing else."
            ))

            val conversation = currentEngine.createConversation(config)
            // sendMessage(String) overload available in 0.14.0
            val responseMsg = conversation.sendMessage(prompt)
            conversation.close()

            _status.value = Status.READY

            // response.contents → Contents wrapper; .contents → List<Content>
            val responseText: String = responseMsg.contents.contents
                .mapNotNull { c -> (c as? Content.Text)?.text }
                .joinToString(" ")

            val parsed = parseLlmOutput(responseText)
            Log.d(TAG, "parseNotification result: $parsed")
            return@withContext parsed

        } catch (e: Exception) {
            Log.e(TAG, "parseNotification inference failed", e)
            _status.value = Status.READY
            return@withContext fallbackParse(rawText)
        }
    }

    /**
     * Parses the LLM's plain-text output into an [ExtractedInfo].
     */
    private fun parseLlmOutput(output: String): ExtractedInfo {
        val lines = output.lines()

        fun field(prefix: String): String? =
            lines.firstOrNull { it.startsWith(prefix, ignoreCase = true) }
                ?.substringAfter(":", "")
                ?.trim()
                ?.takeIf { it.isNotBlank() && !it.equals("UNKNOWN", ignoreCase = true) }

        val amount = field("AMOUNT")?.toDoubleOrNull()

        val currency = field("CURRENCY")?.uppercase()?.takeIf {
            it == "USD" || it == "KHR"
        }

        val categoryRaw = field("CATEGORY")?.uppercase()
        val category = categoryRaw?.let { raw ->
            Category.entries.firstOrNull { it.name == raw }
        }

        val remark = field("REMARK") ?: ""

        return ExtractedInfo(
            amount = amount,
            currency = currency,
            category = category,
            remark = remark
        )
    }

    /**
     * Regex-based fallback when the LLM engine is unavailable.
     * Extracts amount and currency; leaves category and remark empty.
     */
    private fun fallbackParse(text: String): ExtractedInfo {
        val usdMatch =
            Regex("""(?:USD|\$|US\$)\s*(\d[\d,]*(?:\.\d{1,2})?)""", RegexOption.IGNORE_CASE).find(text)
                ?: Regex("""(\d[\d,]*(?:\.\d{1,2})?)\s*(?:USD)""", RegexOption.IGNORE_CASE).find(text)

        if (usdMatch != null) {
            return ExtractedInfo(
                amount = usdMatch.groupValues[1].replace(",", "").toDoubleOrNull(),
                currency = "USD",
                category = null,
                remark = ""
            )
        }

        val khrMatch =
            Regex("""(\d[\d,]*(?:\.\d{1,2})?)\s*(?:KHR|RIEL|៛)""", RegexOption.IGNORE_CASE).find(text)
                ?: Regex("""(?:KHR|RIEL|៛)\s*(\d[\d,]*(?:\.\d{1,2})?)""", RegexOption.IGNORE_CASE).find(text)

        if (khrMatch != null) {
            return ExtractedInfo(
                amount = khrMatch.groupValues[1].replace(",", "").toDoubleOrNull(),
                currency = "KHR",
                category = null,
                remark = ""
            )
        }

        return ExtractedInfo(amount = null, currency = null, category = null, remark = "")
    }

    // -------------------------------------------------------------------------
    //  Financial tip generation
    // -------------------------------------------------------------------------

    suspend fun generateFinancialTip(transactions: List<Transaction>): String =
        withContext(Dispatchers.IO) {
            if (engine == null) initialize()
            val currentEngine = engine ?: return@withContext "AI Assistant unavailable"

            try {
                _status.value = Status.INFERENCE

                val txSummary = if (transactions.isEmpty()) {
                    "No transaction history recorded yet."
                } else {
                    transactions.take(15).joinToString("\n") { tx ->
                        "- ${tx.merchant}: $${tx.amount} (${tx.category.label}, ${tx.dateLabel})"
                    }
                }

                val prompt = """
                    You are a smart on-device personal finance assistant.
                    Based on these recent transactions:
                    $txSummary

                    Provide a single, short, personalized financial tip or warning (maximum 12 words).
                    Be direct, action-oriented, and don't include introductory text, numbers, or bullet points.
                """.trimIndent()

                Log.d(TAG, "generateFinancialTip prompt:\n$prompt")

                val config = ConversationConfig(
                    Contents.of("You are a helpful and concise financial advice bot.")
                )

                val conversation = currentEngine.createConversation(config)
                val responseMsg = conversation.sendMessage(prompt)
                conversation.close()

                _status.value = Status.READY

                val responseText: String = responseMsg.contents.contents
                    .mapNotNull { c -> (c as? Content.Text)?.text }
                    .joinToString(" ")

                return@withContext responseText.ifBlank {
                    "Save more by tracking daily dining out expenses."
                }

            } catch (e: Exception) {
                Log.e(TAG, "generateFinancialTip inference failed", e)
                _status.value = Status.READY
                return@withContext "Save more by tracking daily dining out expenses."
            }
        }

    // -------------------------------------------------------------------------
    //  Lifecycle
    // -------------------------------------------------------------------------

    fun release() {
        try {
            engine?.close()
            engine = null
            _status.value = Status.NOT_INITIALIZED
        } catch (e: Exception) {
            Log.e(TAG, "Error closing engine", e)
        }
    }

    companion object {
        private const val TAG = "LocalLlmService"
    }
}
