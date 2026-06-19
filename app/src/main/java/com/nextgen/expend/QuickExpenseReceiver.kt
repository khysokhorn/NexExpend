package com.nextgen.expend

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Settings

/**
 * Receives the shortcut intent and starts the overlay service directly,
 * so the user's current app (e.g. Facebook) stays in the foreground.
 *
 * If the overlay permission hasn't been granted yet, it falls back to
 * opening [QuickExpenseTrampolineActivity] which will request it.
 */
class QuickExpenseReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (!Settings.canDrawOverlays(context)) {
            // Can't show overlay — launch the trampoline activity to request permission
            val fallback = Intent(context, QuickExpenseTrampolineActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(fallback)
            return
        }

        // Start the overlay service directly — no activity appears, so the foreground app stays visible.
        // Use plain startService: QuickExpenseOverlayService is a background service by design
        // (it never calls startForeground), so startForegroundService must NOT be used here.
        val serviceIntent = Intent(context, QuickExpenseOverlayService::class.java)
        context.startService(serviceIntent)
    }
}
