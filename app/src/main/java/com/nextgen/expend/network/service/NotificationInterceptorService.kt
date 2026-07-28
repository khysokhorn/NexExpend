package com.nextgen.expend.network.service

import android.app.Notification
import android.content.Intent
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.annotation.RequiresApi
import com.nextgen.expend.QuickExpenseOverlayService
import com.nextgen.expend.network.localllm.LocalLlmService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

class NotificationInterceptorService : NotificationListenerService() {

    private val localLlmService: LocalLlmService by inject()
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val processedNotifications = ConcurrentHashMap<String, Long>()

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.d(TAG, "Notification listener connected")
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        Log.d(TAG, "Notification listener disconnected")
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val packageName = sbn.packageName

        if (packageName !in SUPPORTED_BANK_PACKAGES) {
            return
        }

        val notification = sbn.notification ?: return

        if (notification.flags and Notification.FLAG_ONGOING_EVENT != 0) {
            return
        }

        if (notification.flags and Notification.FLAG_GROUP_SUMMARY != 0) {
            return
        }

        val fullText = extractNotificationText(notification)

        if (fullText.isBlank()) {
            return
        }

        Log.d(TAG, "Notification from $packageName: $fullText")

        if (!isPaymentNotification(fullText)) {
            Log.d(TAG, "Ignored: not a payment notification")
            return
        }

        val payment = parsePayment(fullText) ?: run {
            Log.d(TAG, "Ignored: amount or currency not found")
            return
        }

        if (isDuplicate(sbn, payment)) {
            Log.d(TAG, "Ignored: duplicate notification")
            return
        }

        val bankName = getBankName(packageName)

        Log.d(
            TAG,
            "Payment detected: bank=$bankName, " +
                    "amount=${payment.amount}, " +
                    "currency=${payment.currency}"
        )

        onPaymentReceived(
            payment.copy(
                bankName = bankName,
                packageName = packageName,
                rawText = fullText
            )
        )
    }

    private fun onPaymentReceived(payment: PaymentInfo) {
        Log.d(TAG, "Intercepted payment: $payment — launching LLM enrichment…")

        serviceScope.launch {
            // Run LLM to extract enriched info (category, remark, confirm amount)
            val extracted = localLlmService.parseNotification(payment.rawText)

            Log.d(TAG, "LLM extracted: $extracted")

            // Determine final amount: prefer LLM result, fall back to regex result
            val finalAmount = extracted.amount ?: payment.amount

            // Build the intent to launch / update the overlay
            val overlayIntent = Intent(
                applicationContext,
                QuickExpenseOverlayService::class.java
            ).apply {
                action = QuickExpenseOverlayService.ACTION_SHOW_NOTIFICATION_PREVIEW
                putExtra(QuickExpenseOverlayService.EXTRA_AMOUNT, finalAmount)
                putExtra(QuickExpenseOverlayService.EXTRA_CURRENCY, extracted.currency ?: payment.currency)
                putExtra(QuickExpenseOverlayService.EXTRA_CATEGORY, extracted.category?.name)
                putExtra(QuickExpenseOverlayService.EXTRA_REMARK, extracted.remark)
                putExtra(QuickExpenseOverlayService.EXTRA_BANK_NAME, payment.bankName)
                putExtra(QuickExpenseOverlayService.EXTRA_IS_FROM_NOTIFICATION, true)
            }

            applicationContext.startService(overlayIntent)
        }
    }

    private fun extractNotificationText(
        notification: Notification
    ): String {
        val extras = notification.extras

        val title = extras
            .getCharSequence(Notification.EXTRA_TITLE)
            ?.toString()
            .orEmpty()

        val text = extras
            .getCharSequence(Notification.EXTRA_TEXT)
            ?.toString()
            .orEmpty()

        val bigText = extras
            .getCharSequence(Notification.EXTRA_BIG_TEXT)
            ?.toString()
            .orEmpty()

        val subText = extras
            .getCharSequence(Notification.EXTRA_SUB_TEXT)
            ?.toString()
            .orEmpty()

        val textLines = extras
            .getCharSequenceArray(Notification.EXTRA_TEXT_LINES)
            ?.joinToString(" ") { it.toString() }
            .orEmpty()

        return listOf(
            title,
            text,
            bigText,
            subText,
            textLines
        )
            .filter { it.isNotBlank() }
            .distinct()
            .joinToString(" ")
            .trim()
    }

    private fun isPaymentNotification(text: String): Boolean {
        val normalizedText = text.lowercase(Locale.US)

        val hasPaymentKeyword = PAYMENT_KEYWORDS.any { keyword ->
            normalizedText.contains(keyword)
        }

        val hasIgnoredKeyword = IGNORED_KEYWORDS.any { keyword ->
            normalizedText.contains(keyword)
        }

        return hasPaymentKeyword && !hasIgnoredKeyword
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun parsePayment(text: String): PaymentInfo? {
        val normalizedText = normalizeKhmerDigits(text)

        for (pattern in PAYMENT_PATTERNS) {
            val match = pattern.find(normalizedText) ?: continue

            val amountRaw = match.groups["amount"]
                ?.value
                ?: continue

            val currencyRaw = match.groups["currency"]?.value
                ?: match.groups["symbol"]?.value
                ?: continue

            val amount = amountRaw
                .replace(",", "")
                .replace(" ", "")
                .toDoubleOrNull()
                ?: continue

            if (amount <= 0.0) {
                continue
            }

            val currency = when (
                currencyRaw.uppercase(Locale.US)
            ) {
                "$", "US$", "USD" -> "USD"
                "៛", "KHR", "RIEL" -> "KHR"
                else -> continue
            }

            return PaymentInfo(
                amount = amount,
                currency = currency
            )
        }

        return null
    }

    private fun isDuplicate(
        sbn: StatusBarNotification,
        payment: PaymentInfo
    ): Boolean {
        val currentTime = System.currentTimeMillis()

        processedNotifications.entries.removeIf { entry ->
            currentTime - entry.value > DUPLICATE_WINDOW_MS
        }

        val key = buildString {
            append(sbn.packageName)
            append(':')
            append(sbn.id)
            append(':')
            append(sbn.tag.orEmpty())
            append(':')
            append(payment.amount)
            append(':')
            append(payment.currency)
        }

        val previousTime = processedNotifications.put(
            key,
            currentTime
        )

        return previousTime != null &&
                currentTime - previousTime < DUPLICATE_WINDOW_MS
    }

    private fun normalizeKhmerDigits(text: String): String {
        return buildString {
            text.forEach { character ->
                append(
                    when (character) {
                        '០' -> '0'
                        '១' -> '1'
                        '២' -> '2'
                        '៣' -> '3'
                        '៤' -> '4'
                        '៥' -> '5'
                        '៦' -> '6'
                        '៧' -> '7'
                        '៨' -> '8'
                        '៩' -> '9'
                        else -> character
                    }
                )
            }
        }
    }

    private fun getBankName(packageName: String): String {
        return BANK_PACKAGES[packageName] ?: "Unknown Bank"
    }

    override fun onNotificationRemoved(
        sbn: StatusBarNotification
    ) = Unit

    data class PaymentInfo(
        val amount: Double,
        val currency: String,
        val bankName: String = "",
        val packageName: String = "",
        val rawText: String = ""
    )

    companion object {

        private const val TAG = "NotificationInterceptor"
        private const val DUPLICATE_WINDOW_MS = 10_000L

        private val BANK_PACKAGES = mapOf(
            "com.wingmoney.wingpay" to "Wing",
            "com.paygo24.ibank" to "ABA",
        )

        private val SUPPORTED_BANK_PACKAGES =
            BANK_PACKAGES.keys

        // NexExpend logs EXPENSES, so the trigger keywords are outgoing-money
        // signals (sent/paid/transferred/withdrawn) — incoming/received money is
        // explicitly ignored below since this flow only ever saves TransactionType.EXPENSE.
        private val PAYMENT_KEYWORDS = listOf(
            "sent",
            "money sent",
            "payment sent",
            "successfully sent",
            "payment made",
            "you paid",
            "paid to",
            "transferred to",
            "transfer to",
            "debited",
            "withdraw",
            "withdrawal",
            "purchase",
            "បានផ្ញើប្រាក់",
            "បានបង់ប្រាក់",
            "ដកប្រាក់"
        )

        private val IGNORED_KEYWORDS = listOf(
            "received",
            "money received",
            "payment received",
            "successfully received",
            "credited",
            "credit alert",
            "incoming transfer",
            "cash in",
            "failed",
            "declined",
            "pending",
            "otp",
            "verification code",
            "បានទទួលប្រាក់",
            "ទទួលបានប្រាក់",
            "ទទួលប្រាក់",
            "ប្រាក់ចូល",
            "មិនជោគជ័យ"
        )

        private val PAYMENT_PATTERNS = listOf(
            Regex(
                pattern = """
                    (?<currency>USD|KHR|RIEL)\s*
                    (?<amount>\d[\d,\s]*(?:\.\d{1,2})?)
                """.trimIndent().replace("\n", ""),
                option = RegexOption.IGNORE_CASE
            ),
            Regex(
                pattern = """
                    (?<amount>\d[\d,\s]*(?:\.\d{1,2})?)\s*
                    (?<currency>USD|KHR|RIEL)
                """.trimIndent().replace("\n", ""),
                option = RegexOption.IGNORE_CASE
            ),
            Regex(
                pattern = """
                    (?<symbol>US\$|\$)\s*
                    (?<amount>\d[\d,\s]*(?:\.\d{1,2})?)
                """.trimIndent().replace("\n", ""),
                option = RegexOption.IGNORE_CASE
            ),
            Regex(
                pattern = """
                    (?<amount>\d[\d,\s]*(?:\.\d{1,2})?)\s*
                    (?<symbol>៛)
                """.trimIndent().replace("\n", "")
            ),
            Regex(
                pattern = """
                    (?<symbol>៛)\s*
                    (?<amount>\d[\d,\s]*(?:\.\d{1,2})?)
                """.trimIndent().replace("\n", "")
            )
        )
    }
}