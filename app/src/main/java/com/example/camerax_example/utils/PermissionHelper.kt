package com.example.camerax_example.utils

import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

class PermissionHelper(
        private val activity: Activity,
        private val rationaleCallback: ((permissions: Collection<String>, retry: () -> Unit) -> Boolean)?
) {
    private val grantPermissions = PublishSubject.create<Collection<String>>()
    private val denyPermissions = PublishSubject.create<Collection<String>>()

    private fun request(permission: String) {
        request(setOf(permission))
    }

    private fun request(permissions: Set<String>, ignoreRationale: Collection<String>? = null) {
        rationaleCallback
                ?.let { callback ->
                    permissions
                            .filter { permission ->
                                ignoreRationale?.contains(permission) != true &&
                                        ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
                            }.takeIf { it.isNotEmpty() }
                            ?.let {
                                val ignore = (ignoreRationale?.toMutableList() ?: mutableListOf())
                                        .apply { addAll(it) }
                                if (callback(it) { request(permissions, ignore) }) {
                                    Unit
                                } else {
                                    null
                                }
                            }
                } ?: ActivityCompat.requestPermissions(activity, permissions.toTypedArray(), 0)
    }

    fun check(permission: String): Boolean =
            ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED

    fun need(permission: String): Observable<String> {
        return Observable
                .defer {
                    if (check(permission)) {
                        grantPermissions.startWith(setOf(permission))
                    } else {
                        request(permission)
                        grantPermissions
                    }
                }
                .mergeWith(getDenyPermission(setOf(permission)))
                .filter { it.contains(permission) }
                .map { permission }
    }

    fun onResult(permissions: Array<out String>, grantResults: IntArray) {
        val granted = mutableSetOf<String>()
        val denied = mutableSetOf<String>()

        permissions.forEachIndexed { index, s ->
            if (grantResults.getOrNull(index) == PackageManager.PERMISSION_GRANTED) {
                granted.add(s)
            } else {
                denied.add(s)
            }
        }

        if (denied.isNotEmpty()) {
            denyPermissions.onNext(denied)
        }

        if (granted.isNotEmpty()) {
            grantPermissions.onNext(granted)
        }
    }

    private fun getDenyPermission(permissions: Set<String>): Observable<Set<String>> {
        return denyPermissions
                .map { it.intersect(permissions) }
                .filter { it.isNotEmpty() }
                .flatMap {
                    Observable.error<Set<String>>(PermissionDeniedException(it))
                }
    }
}

data class PermissionDeniedException(
        val permissions: Set<String>
) : RuntimeException("Permission was denied")

interface Permissionist {

    val permissioner: PermissionHelper
}