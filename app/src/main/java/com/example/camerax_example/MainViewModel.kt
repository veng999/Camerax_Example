package com.example.camerax_example

import android.Manifest
import android.app.Application
import android.view.KeyEvent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.LiveDataReactiveStreams
import io.reactivex.BackpressureStrategy
import io.reactivex.subjects.PublishSubject

class MainViewModel(private var app: Application) : AndroidViewModel(app){

    private val keyDownPublisher = PublishSubject.create<KeyCode>()

    fun registerKeyDown(keyCode: Int): Boolean {
        if (!keyDownPublisher.hasObservers()) {
            return false
        }
        return getKeyCode(keyCode)?.let(keyDownPublisher::onNext) != null
    }

    private fun getPermissionName(permission: String): String? = when (permission) {
        Manifest.permission.CAMERA -> R.string.permission_camera
        else -> null
    }?.let(app.applicationContext::getString)

    private fun getKeyCode(keyCode: Int): KeyCode? = when (keyCode) {
        KeyEvent.KEYCODE_VOLUME_DOWN -> KeyCode.SHUTTER
        else -> null
    }

    fun getPermissionRationaleText(permissions: Collection<String>): String? {
        if (permissions.size > 1) {
            val perms = permissions.mapNotNull(::getPermissionName).joinToString(",\n") { " - $it" }
            return app.applicationContext.getString(R.string.need_permissions, perms)
        }

        return when {
            permissions.contains(Manifest.permission.CAMERA) -> R.string.need_camera_permission
            else -> null
        }?.let(app.applicationContext::getString)
    }

    fun onKeyDown(keyCode: KeyCode): LiveData<KeyCode> = LiveDataReactiveStreams.fromPublisher(
            keyDownPublisher
                    .filter { it == keyCode }
                    .toFlowable(BackpressureStrategy.LATEST)
    )


    enum class KeyCode {
        SHUTTER
    }
}