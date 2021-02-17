package com.example.camerax_example.ext

import android.app.Activity
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer

private const val ANIMATION_FAST_MILLIS = 300L

/**
 * Simulate a button click, including a small delay while it is being pressed to trigger the
 * appropriate animations.
 */
fun View.simulateClick(delay: Long = ANIMATION_FAST_MILLIS) {
    performClick()
    isPressed = true
    invalidate()
    postDelayed({
        invalidate()
        isPressed = false
    }, delay)
}

fun Activity.showAlert(setup: AlertDialog.Builder.() -> Unit) {
    val builder = AlertDialog.Builder(this)
    setup(builder)
    builder.show()
}

inline fun <T> Fragment.observe(data: LiveData<T>, crossinline callback: (T) -> Unit) =
        data.observe(viewLifecycleOwner, Observer { event -> event?.let { callback(it) } })

inline fun <T> AppCompatActivity.observe(data: LiveData<T>, crossinline callback: (T) -> Unit) =
        data.observe(this, Observer { event -> event.let { callback(it) } })

inline fun <reified T> Fragment.requireParent(): T {
    return optParent(T::class.java)
        ?: throw IllegalArgumentException("Unable to resolve interface $host")
}

fun <T> Fragment.optParent(host: Class<T>): T? {
    var parent: Fragment? = parentFragment
    while (parent != null) {
        // Return the nearest parent fragment that implements the given interface
        if (host.isInstance(parent)) {
            return host.cast(parent)
        }
        parent = parent.parentFragment
    }

    // If none of the parent fragments implement the given interface try to cast Activity to it
    val activity = activity
    if (host.isInstance(activity)) {
        return host.cast(activity)
    }

    return null
}