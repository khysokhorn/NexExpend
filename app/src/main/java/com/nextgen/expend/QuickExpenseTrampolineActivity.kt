package com.nextgen.expend

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast

/**
 * Invisible trampoline activity that starts the overlay service.
 *
 * The overlay service uses TYPE_APPLICATION_OVERLAY, which renders on top of any app.
 * This activity must NOT call moveTaskToBack() — doing so pushes the trampoline task to the
 * back while Facebook is already backgrounded, leaving Android with no foreground task and
 * causing it to fall back to the home screen before showing the overlay.
 *
 * Instead, we call finishAndRemoveTask() immediately after starting the service. This removes
 * the trampoline task entirely, so Android restores the most recently active task (Facebook)
 * naturally — and the overlay floats on top of it.
 */
class QuickExpenseTrampolineActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Suppress all transition animations so this invisible activity is never seen
        @Suppress("DEPRECATION")
        overridePendingTransition(0, 0)

        if (!Settings.canDrawOverlays(this)) {
            // Permission not granted — open the system settings page to request it
            val permIntent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            ).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            try {
                startActivity(permIntent)
                Toast.makeText(this, "Please grant overlay permission for NexExpend", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Toast.makeText(this, "Unable to open overlay settings", Toast.LENGTH_SHORT).show()
            }
            finishAndRemoveTask()
        } else {
            // Permission granted — start the overlay service, then remove this task so Android
            // restores the previous foreground app (e.g. Facebook) seamlessly.
            startOverlayService()
            finishAndRemoveTask()
        }
    }

    private fun startOverlayService() {
        val serviceIntent = Intent(this, QuickExpenseOverlayService::class.java)
        try {
            // Use plain startService — this is a background service (not foreground),
            // so startForegroundService must NOT be used or the OS will crash it after 5s.
            startService(serviceIntent)
            Log.d("QuickExpense", "Successfully started overlay service from trampoline")
        } catch (e: Exception) {
            Log.e("QuickExpense", "Failed to start overlay service", e)
            Toast.makeText(this, "Unable to launch quick input", Toast.LENGTH_SHORT).show()
        }
    }
}
