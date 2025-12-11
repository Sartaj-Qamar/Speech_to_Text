package com.codetech.speechtotext.Utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Rect
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.SystemClock
import android.util.TypedValue
import android.view.View
import android.view.ViewTreeObserver
import android.view.Window
import androidx.activity.OnBackPressedCallback
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import com.codetech.speechtotext.R
import com.codetech.speechtotext.models.LanguageData

var scaledBitmap: Bitmap? = null
var cameraPick = false
var galleryPick = false




fun Context.isNetworkConnected(): Boolean {
    val connectivityManager =
        getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    val network = connectivityManager.activeNetwork ?: return false
    val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

    return networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
}




fun View.safeClickListener(debounceTime: Long = 1200L, action: () -> Unit) {
    this.setOnClickListener(object : View.OnClickListener {
        private var lastClickTime: Long = 0
        override fun onClick(v: View) {
            if (SystemClock.elapsedRealtime() - lastClickTime < debounceTime) return
            else action()
            lastClickTime = SystemClock.elapsedRealtime()
        }
    })

}

fun View.visible() {
    this.visibility = View.VISIBLE
}

fun View.gone() {
    this.visibility = View.GONE
}

fun View.isKeyboardVisible(): Boolean {
    val rect = Rect()
    getWindowVisibleDisplayFrame(rect)
    val screenHeight = rootView.height
    val keypadHeight = screenHeight - rect.bottom
    return keypadHeight > screenHeight * 0.15
}


fun Fragment.backPress(callback: () -> Unit) {
    requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner,
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                callback()
            }
        }
    )
}





var mLastClickTime: Long = 0
fun View.clickListener(
    action: (view: View) -> Unit,
) {
    this.setOnClickListener {
        if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
            return@setOnClickListener
        }
        mLastClickTime = SystemClock.elapsedRealtime()
        action(it)
    }


}


fun Window.hideSystemUI() {
    val windowInsetsController = WindowCompat.getInsetsController(this, decorView)
    windowInsetsController.systemBarsBehavior =
        WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    windowInsetsController.hide(WindowInsetsCompat.Type.navigationBars())
}




fun Context.dpToPx(valueInDp: Float): Float {
    val metrics = this.resources.displayMetrics
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, valueInDp, metrics)
}

fun View.isKeyboardOpen(context: Context, isKeyBoardOpen: (Boolean) -> Unit) {
    val listener = ViewTreeObserver.OnGlobalLayoutListener {
        val heightDiff = this@isKeyboardOpen.rootView.height - this@isKeyboardOpen.height
        if (heightDiff > context.dpToPx(200F)) {
            isKeyBoardOpen.invoke(true)
        } else {
            isKeyBoardOpen.invoke(false)
        }
    }

    this.viewTreeObserver.addOnGlobalLayoutListener(listener)

    // Remove the listener when the view is detached
    this.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {

        override fun onViewAttachedToWindow(p0: View) {}

        override fun onViewDetachedFromWindow(p0: View) {
            this@isKeyboardOpen.viewTreeObserver .removeOnGlobalLayoutListener(listener)
            this@isKeyboardOpen.removeOnAttachStateChangeListener(this)
        }
    })
}

fun View.manageBottomNavOnKeyboardState(context: Context, bottomNav: View, mic: View) {
    this.isKeyboardOpen(context) { isOpen ->
        if (isOpen) {
            bottomNav.gone()
            mic.gone()
        } else {
            bottomNav.visible()
            mic.visible()
        }
    }
}

