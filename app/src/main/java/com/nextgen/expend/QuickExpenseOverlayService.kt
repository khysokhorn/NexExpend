package com.nextgen.expend

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.view.Gravity
import android.view.WindowManager
import androidx.annotation.RequiresApi
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
import com.nextgen.expend.dialog.QuickExpenseTopPopup
import com.nextgen.expend.ui.theme.NexExpendTheme

@RequiresApi(Build.VERSION_CODES.O)
class QuickExpenseOverlayService : Service(), LifecycleOwner, SavedStateRegistryOwner,
    ViewModelStoreOwner {

    private lateinit var windowManager: WindowManager
    private var overlayView: ComposeView? = null

    private val lifecycleRegistry = LifecycleRegistry(this)

    private val savedStateRegistryController = SavedStateRegistryController.create(this)

    private val vmStore = ViewModelStore()

    override val lifecycle: Lifecycle
        get() = lifecycleRegistry

    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry

    override val viewModelStore: ViewModelStore
        get() = vmStore

    override fun onCreate() {
        super.onCreate()

        savedStateRegistryController.performRestore(null)

        lifecycleRegistry.currentState = Lifecycle.State.CREATED
        lifecycleRegistry.currentState = Lifecycle.State.STARTED
        lifecycleRegistry.currentState = Lifecycle.State.RESUMED

        showOverlay()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showOverlay() {
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        val composeView = ComposeView(this).apply {

            setViewTreeLifecycleOwner(this@QuickExpenseOverlayService)
            setViewTreeSavedStateRegistryOwner(this@QuickExpenseOverlayService)
            setViewTreeViewModelStoreOwner(this@QuickExpenseOverlayService)

            setContent {
                NexExpendTheme {
                    QuickExpenseTopPopup(
                        onSave = { closeOverlay() },
                        onDismiss = { closeOverlay() }
                    )
                }
            }
        }
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
            softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        }

        overlayView = composeView

        Log.d("QuickExpense", "Before addView")
        windowManager.addView(composeView, params)
        Log.d("QuickExpense", "After addView")
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

