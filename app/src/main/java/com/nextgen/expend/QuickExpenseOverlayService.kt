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

    override fun onCreate() {
        super.onCreate()

        savedStateRegistryController.performRestore(null)

        lifecycleRegistry.currentState = Lifecycle.State.CREATED
        lifecycleRegistry.currentState = Lifecycle.State.STARTED
        lifecycleRegistry.currentState = Lifecycle.State.RESUMED

        showOverlay()
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
                        onSave = { amount, category, note ->
                            saveExpense(amount, category, note)
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

        Log.d("QuickExpense", "Before addView")
        try {
            windowManager.addView(composeView, params)
            Log.d("QuickExpense", "After addView")
        } catch (e: Exception) {
            Log.e("QuickExpense", "Failed to add overlay view to WindowManager", e)
            Toast.makeText(this, "Overlay permission is required", Toast.LENGTH_LONG).show()
            stopSelf()
        }
    }

    private fun saveExpense(amount: Double, category: Category, note: String) {
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
                timeLabel = timeFormat.format(now)
            )
            repository.addTransaction(transaction)
            Log.d("QuickExpense", "Saved expense: $amount ${category.label}")
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
}
