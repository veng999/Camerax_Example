package com.example.camerax_example.utils

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import com.example.camerax_example.App
import java.io.File

private const val TAG = "CustomImageSaveCallback"

class CustomImageSaveCallback(private val file: File) : ImageCapture.OnImageSavedCallback{

    private val appContext: Context by lazy { App.getApp().applicationContext }

    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
        val msg = "Photo capture succeeded: ${file.absolutePath}"
        Toast.makeText(appContext.applicationContext, msg, Toast.LENGTH_SHORT).show()
        Log.d(TAG, msg)
    }

    override fun onError(exception: ImageCaptureException) {
        val reason = when (exception.imageCaptureError) {
            ImageCapture.ERROR_UNKNOWN -> "unknown error"
            ImageCapture.ERROR_FILE_IO -> "unable to save file"
            ImageCapture.ERROR_CAPTURE_FAILED -> "capture failed"
            ImageCapture.ERROR_CAMERA_CLOSED -> "camera closed"
            ImageCapture.ERROR_INVALID_CAMERA -> "invalid camera"
            else -> "unknown error"
        }
        val msg = "Photo capture failed: $reason"
        Toast.makeText(appContext, msg, Toast.LENGTH_SHORT).show()
        Log.e(TAG, msg)
        exception.printStackTrace()
    }

}