package com.nextgen.expend

import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService

/**
 * Quick Settings Tile for instantly adding an expense from ANY app.
 *
 * WHY A TILE AND NOT A SHORTCUT:
 * App shortcuts (static or dynamic) require launching an Activity, which always triggers
 * onPause() on the current foreground app (e.g. Facebook). Apps like Facebook respond to
 * onPause() by calling moveTaskToBack(), sending themselves to the home screen. There is no
 * workaround — this is the OS enforcing Activity lifecycle rules.
 *
 * A TileService.onClick() is invoked directly on a Service, so NO Activity is ever launched.
 * The Quick Settings panel collapses, the foreground app stays alive, and the overlay
 * (TYPE_APPLICATION_OVERLAY) appears right on top of it.
 *
 * HOW TO ACTIVATE:
 * 1. Pull down the notification shade twice to open Quick Settings.
 * 2. Tap the pencil/edit icon.
 * 3. Find "Add Expense" and drag it into your active tiles.
 * 4. Done — tapping the tile now shows the overlay over any app.
 *
 * On Pixel with Quick Tap: set Quick Tap → "Open app" → NexExpend, then open NexExpend once
 * so the tile is in place. Unfortunately Pixel Quick Tap always routes through the launcher
 * for "Open app" actions, which inherently pauses the foreground app. The tile is the
 * correct trigger for the "stay-over-another-app" use case.
 */
class QuickExpenseTileService : TileService() {

    override fun onStartListening() {
        super.onStartListening()
        qsTile?.apply {
            state = Tile.STATE_INACTIVE
            updateTile()
        }
    }

    override fun onStopListening() {
        super.onStopListening()
    }

    override fun onClick() {
        super.onClick()

        if (!Settings.canDrawOverlays(this)) {
            // Permission not granted — launch trampoline to request it.
            // This WILL briefly interrupt the foreground app, but it only happens once ever.
            val permIntent = Intent(this, QuickExpenseTrampolineActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                // Android 14+ requires PendingIntent overload
                val pending = PendingIntent.getActivity(
                    this, 0, permIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                startActivityAndCollapse(pending)
            } else {
                @Suppress("DEPRECATION")
                startActivityAndCollapse(permIntent)
            }
            return
        }

        // ✅ This is the key path: start the service directly — no Activity, no focus steal.
        // The QS panel will collapse on its own after onClick() returns, revealing the
        // foreground app (Facebook, Chrome, whatever) with our overlay on top.
        startService(Intent(this, QuickExpenseOverlayService::class.java))

        qsTile?.apply {
            state = Tile.STATE_ACTIVE
            updateTile()
        }
    }
}
