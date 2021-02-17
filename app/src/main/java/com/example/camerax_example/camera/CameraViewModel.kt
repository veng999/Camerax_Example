package com.example.camerax_example.camera

import android.app.Application
import android.view.Surface
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.core.content.ContextCompat
import androidx.lifecycle.*
import com.example.camerax_example.ext.waitForCameraPermission
import com.example.camerax_example.utils.CustomImageSaveCallback
import com.example.camerax_example.utils.PermissionHelper
import com.example.camerax_example.utils.SingleLiveEvent
import io.reactivex.disposables.CompositeDisposable
import java.io.File

private const val TAG = "CameraViewModel"

class CameraViewModel(private val app: Application) : AndroidViewModel(app), LifecycleObserver {
    private val _startCamera = SingleLiveEvent<Unit>()
    val startCamera: LiveData<Unit> = _startCamera

    private val _quit = SingleLiveEvent<Unit>()
    val quit: LiveData<Unit> = _quit

    private val _lensFacing = MutableLiveData(CameraSelector.LENS_FACING_BACK)
    val lensFacing: LiveData<Int> = _lensFacing

    private val disposables = CompositeDisposable()

    fun init(permissioner: PermissionHelper) {
        waitForCameraPermission(
            app.applicationContext,
            permissioner,
            { _startCamera.call() },
            { _quit.call() }
        ).let { disposables.add(it) }
    }

    fun takePicture(imageCapture: ImageCapture) {
        //Name of saved file
        val file = File(getOutputDirectory(), "${System.currentTimeMillis()}.jpg")
        // Create output options object which contains file + metadata
        val outputOptions = ImageCapture.OutputFileOptions.Builder(file).build()
        // Set up image capture listener, which is triggered after photo has been taken
        imageCapture.apply {
            targetRotation = Surface.ROTATION_0
            takePicture(
                    outputOptions,
                    ContextCompat.getMainExecutor(app.applicationContext),
                    CustomImageSaveCallback(file)
            )
        }
    }

    fun switchToNextLensFacing() {
        _lensFacing.value = when (_lensFacing.value) {
            //swap front and back cameras
            CameraSelector.LENS_FACING_FRONT -> CameraSelector.LENS_FACING_BACK
            CameraSelector.LENS_FACING_BACK -> CameraSelector.LENS_FACING_FRONT
            else -> throw IllegalStateException("Unexpected state, lensFacing=${_lensFacing.value}")
        }
    }

    /** Use external media if it is available, our app's file directory otherwise */
    private fun getOutputDirectory(): File {
        return app.applicationContext.externalMediaDirs
            .firstOrNull()
            ?.let { file ->
                File(file, "photos").apply { mkdirs() }
            }
            ?.takeIf { it.exists() }
            ?: app.applicationContext.filesDir
    }
}