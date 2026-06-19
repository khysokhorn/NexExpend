package com.nextgen.expend

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.nextgen.expend.navigation.AppNavHost
import com.nextgen.expend.ui.theme.NexExpendTheme
import com.nextgen.expend.ui.viewmodel.TransactionViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : ComponentActivity() {
    private val transactionViewModel: TransactionViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        val action = intent?.action
        Log.d("QuickExpense", "MainActivity onCreate action: $action")

        // Dynamically apply the silent trampoline theme BEFORE calling super.onCreate()
        // so that the window manager does not allocate a full-screen window or trigger
        // window swipe transitions if we are launching the quick overlay.
        if (action == "com.nextgen.expend.ACTION_QUICK_EXPENSE" || action == Intent.ACTION_VIEW) {
            setTheme(R.style.Theme_Trampoline)
        }

        super.onCreate(savedInstanceState)

        if (handleQuickExpenseIntent(intent)) {
            return
        }

        enableEdgeToEdge()
        setContent {
            NexExpendTheme {
                val navController = rememberNavController()
                AppNavHost(navController = navController, viewModel = transactionViewModel)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleQuickExpenseIntent(intent)
    }

    private fun handleQuickExpenseIntent(intent: Intent?): Boolean {
        val action = intent?.action
        
        if (action == "com.nextgen.expend.ACTION_QUICK_EXPENSE" || action == Intent.ACTION_VIEW) {
            Log.d("QuickExpense", "Redirecting to overlay service from MainActivity")
            
            // Check overlay permission first to prevent WindowManager$BadTokenException crash
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
                Log.d("QuickExpense", "Overlay permission not granted. Launching trampoline for permission request.")
                val trampolineIntent = Intent(this, QuickExpenseTrampolineActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                startActivity(trampolineIntent)
                finish()
                return true
            }

            val serviceIntent = Intent(this, QuickExpenseOverlayService::class.java)
            try {
                startService(serviceIntent)
                // Remove this task entirely so Android restores the previous foreground app (e.g. Facebook)
                // instead of showing the home screen. Do NOT call moveTaskToBack(true) here — that would
                // push MainActivity's task to the back while Facebook is also backgrounded, causing
                // Android to fall back to the home screen.
                finishAndRemoveTask()
                return true
            } catch (e: Exception) {
                Log.e("QuickExpense", "Failed to start overlay service from MainActivity redirection", e)
                Toast.makeText(this, "Unable to start quick expense", Toast.LENGTH_SHORT).show()
            }
        }
        return false
    }
}