package com.nextgen.expend

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.view.Gravity
import android.view.WindowManager
import android.widget.Toast
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.nextgen.expend.data.TransactionRepository
import com.nextgen.expend.data.model.Category
import com.nextgen.expend.data.model.Transaction
import com.nextgen.expend.data.model.TransactionType
import com.nextgen.expend.dialog.QuickExpenseTopPopup
import com.nextgen.expend.ui.theme.NexExpendTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Regular background service that displays the floating Quick Expense popup overlay.
 *
 * When launched via [ACTION_SHOW_NOTIFICATION_PREVIEW] the overlay is pre-filled
 * with LLM-extracted payment details so the user can review / correct them before saving.
 *
 * We do not make it a foreground service. This avoids requiring notification permissions
 * and showing a persistent notification banner, while preventing the system from interrupting
 * the user's active task (like Facebook).
 */
class QuickExpenseOverlayService : Service(), LifecycleOwner, SavedStateRegistryOwner,
    ViewModelStoreOwner {

    private val repository: TransactionRepository by inject()
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private lateinit var windowManager: WindowManager
    private var overlayView: ComposeView? = null

    private val lifecycleRegistry = LifecycleRegistry(this)
    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    private val vmStore = ViewModelStore()

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry
    override val viewModelStore: ViewModelStore get() = vmStore

    // ---- Pre-fill state (updated when a notification preview is requested) ----
    private val prefilledAmount = mutableStateOf<Double?>(null)
    private val prefilledCategory = mutableStateOf<Category?>(null)
    private val prefilledRemark = mutableStateOf("")
    private val prefilledCurrency = mutableStateOf<String?>(null)
    private val isFromNotification = mutableStateOf(false)

    override fun onCreate() {
        super.onCreate()

        savedStateRegistryController.performRestore(null)

        lifecycleRegistry.currentState = Lifecycle.State.CREATED
        lifecycleRegistry.currentState = Lifecycle.State.STARTED
        lifecycleRegistry.currentState = Lifecycle.State.RESUMED

        showOverlay()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_SHOW_NOTIFICATION_PREVIEW) {
            // Update pre-fill state from LLM-extracted extras
            val amount = if (intent.hasExtra(EXTRA_AMOUNT)) intent.getDoubleExtra(EXTRA_AMOUNT, 0.0).takeIf { it > 0 } else null
            val categoryName = intent.getStringExtra(EXTRA_CATEGORY)
            val category = categoryName?.let {
                runCatching { Category.valueOf(it) }.getOrNull()
            }
            val remark = intent.getStringExtra(EXTRA_REMARK) ?: ""
            val currency = intent.getStringExtra(EXTRA_CURRENCY)
            val fromNotif = intent.getBooleanExtra(EXTRA_IS_FROM_NOTIFICATION, false)

            prefilledAmount.value = amount
            prefilledCategory.value = category
            prefilledRemark.value = remark
            prefilledCurrency.value = currency
            isFromNotification.value = fromNotif

            // If overlay is already showing, the Compose state update will re-compose it.
            // If it wasn't showing yet (service just started via onCreate → showOverlay),
            // the values are already set before setContent reads them.
            Log.d(
                TAG,
                "Notification preview received: amount=$amount, category=$category, " +
                        "remark=$remark, currency=$currency"
            )
        }
        return START_NOT_STICKY
    }

    private fun showOverlay() {
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        val composeView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@QuickExpenseOverlayService)
            setViewTreeSavedStateRegistryOwner(this@QuickExpenseOverlayService)
            setViewTreeViewModelStoreOwner(this@QuickExpenseOverlayService)

            setContent {
                NexExpendTheme {
                    QuickExpenseTopPopup(
                        initialAmount = prefilledAmount.value,
                        initialCategory = prefilledCategory.value,
                        initialNote = prefilledRemark.value,
                        initialCurrency = prefilledCurrency.value,
                        isFromNotification = isFromNotification.value,
                        onSave = { amount, category, note, currency ->
                            saveExpense(amount, category, note, currency)
                            closeOverlay()
                        },
                        onDismiss = { closeOverlay() }
                    )
                }
            }
        }

        // Configure LayoutParams to support keyboard resizing
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
            @Suppress("DEPRECATION")
            softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        }

        overlayView = composeView

        Log.d(TAG, "Before addView")
        try {
            windowManager.addView(composeView, params)
            Log.d(TAG, "After addView")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to add overlay view to WindowManager", e)
            Toast.makeText(this, "Overlay permission is required", Toast.LENGTH_LONG).show()
            stopSelf()
        }
    }

    private fun saveExpense(amount: Double, category: Category, note: String, currency: String) {
        serviceScope.launch {
            val timeFormat = SimpleDateFormat("hh:mm a", Locale.US)
            val now = Date()

            val transaction = Transaction(
                id = (System.currentTimeMillis() % Int.MAX_VALUE).toInt(),
                merchant = note.ifBlank { category.label },
                amount = amount,
                type = TransactionType.EXPENSE,
                category = category,
                dateLabel = "Today",
                timeLabel = timeFormat.format(now),
                currency = currency
            )
            repository.addTransaction(transaction)
            Log.d(TAG, "Saved expense: $amount $currency ${category.label}")
        }
    }

    private fun closeOverlay() {
        overlayView?.let {
            windowManager.removeView(it)
        }
        overlayView = null
        stopSelf()
    }

    override fun onDestroy() {
        vmStore.clear()
        serviceScope.cancel()
        closeOverlaySafely()
        lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
        super.onDestroy()
    }

    private fun closeOverlaySafely() {
        try {
            overlayView?.let {
                windowManager.removeView(it)
            }
        } catch (_: Exception) {
        }
        overlayView = null
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val TAG = "QuickExpenseOverlay"

        /** Intent action that triggers a pre-filled notification preview in the overlay. */
        const val ACTION_SHOW_NOTIFICATION_PREVIEW =
            "com.nextgen.expend.ACTION_SHOW_NOTIFICATION_PREVIEW"

        // Intent extras
        const val EXTRA_AMOUNT = "extra_amount"
        const val EXTRA_CURRENCY = "extra_currency"
        const val EXTRA_CATEGORY = "extra_category"
        const val EXTRA_REMARK = "extra_remark"
        const val EXTRA_BANK_NAME = "extra_bank_name"
        const val EXTRA_IS_FROM_NOTIFICATION = "extra_is_from_notification"
    }
}
