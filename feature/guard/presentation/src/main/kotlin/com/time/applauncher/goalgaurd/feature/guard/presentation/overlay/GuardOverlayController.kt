package com.time.applauncher.goalgaurd.feature.guard.presentation.overlay

import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.provider.Settings
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner

/**
 * Shows / dismisses the doom-scroll intervention as a `TYPE_APPLICATION_OVERLAY` window hosting a
 * [ComposeView]. Single-instance: showing while already visible is a no-op. Safe to call dismiss
 * when nothing is shown.
 */
class GuardOverlayController(private val context: Context) {

    private val windowManager =
        context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

    private var composeView: ComposeView? = null
    private var lifecycleOwner: OverlayLifecycleOwner? = null

    val isShowing: Boolean get() = composeView != null

    fun canShow(): Boolean = Settings.canDrawOverlays(context)

    fun show(
        content: OverlayContent,
        onStartHabit: () -> Unit,
        onContinueScrolling: () -> Unit,
    ) {
        if (isShowing || !canShow()) return

        val owner = OverlayLifecycleOwner().apply { onCreate() }
        val view = ComposeView(context).apply {
            setViewTreeLifecycleOwner(owner)
            setViewTreeViewModelStoreOwner(owner)
            setViewTreeSavedStateRegistryOwner(owner)
            setContent {
                InterventionOverlay(
                    content = content,
                    onStartHabit = onStartHabit,
                    onContinueScrolling = onContinueScrolling,
                )
            }
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            overlayWindowType(),
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT,
        ).apply { gravity = Gravity.CENTER }

        runCatching {
            windowManager.addView(view, params)
            composeView = view
            lifecycleOwner = owner
        }.onFailure {
            // e.g. permission revoked between canShow() and addView — fail closed, stay off.
            owner.onDestroy()
        }
    }

    fun dismiss() {
        composeView?.let { view ->
            runCatching { windowManager.removeView(view) }
        }
        lifecycleOwner?.onDestroy()
        composeView = null
        lifecycleOwner = null
    }

    private fun overlayWindowType(): Int =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }
}
