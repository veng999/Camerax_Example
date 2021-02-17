package com.example.camerax_example.camera

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.example.camerax_example.R
import com.example.camerax_example.databinding.FilterActivityBinding
import com.example.camerax_example.utils.CameraUtils.getImageAnalysis
import com.example.camerax_example.utils.YuvToRgbConverter
import com.wefika.flowlayout.FlowLayout
import jp.co.cyberagent.android.gpuimage.GPUImage
import jp.co.cyberagent.android.gpuimage.GPUImageView
import jp.co.cyberagent.android.gpuimage.filter.*
import java.util.concurrent.Executors

private val CAMERA_SELECTOR_BACK_CAM = CameraSelector.DEFAULT_BACK_CAMERA
private val BITMAP_CONFIG_ARGB_8888 = Bitmap.Config.ARGB_8888
private const val REQUEST_CODE_PERMISSIONS = 10
private  val FLOWLAYOUT_PARAMS = FlowLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
private const val BRIGHTNESS = .8f
private const val CONTRAST = 2f
private const val PIXEL = 20f
private const val GAMMA = 2f

class FilterActivity: AppCompatActivity() {

    private var cameraProvider: ProcessCameraProvider? = null
    private val executor = Executors.newSingleThreadExecutor()
    private lateinit var converter: YuvToRgbConverter
    private lateinit var bitmap: Bitmap
    private lateinit var buttonContainer: FlowLayout
    private lateinit var binding: FilterActivityBinding
    private lateinit var gpuImageView: GPUImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FilterActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // YuvToRgb converter
        converter = YuvToRgbConverter(this)

        // Init views
        initViews()

        // Camera permission needed for CameraX.
        requestPermissions(arrayOf(Manifest.permission.CAMERA), REQUEST_CODE_PERMISSIONS)

        initCameraX()
    }

    // Init CameraX.
    private fun initCameraX(){

        //Get Future witch contains ProcessCameraProvider (singleton which can be uses for bind camera's lyfecycle to lyfecycleowner)
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(Runnable {
            cameraProvider = cameraProviderFuture.get()
            startCameraIfReady()
        }, ContextCompat.getMainExecutor(this))
    }

    private fun initViews() {
        buttonContainer = binding.buttonContainer
        addButtons()
        // The activity is locked to portrait mode. We only need to correct for sensor rotation.
        /*gpuImageView.rotation = 360F*/
        gpuImageView = binding.gpuImageView.apply {
            setScaleType(GPUImage.ScaleType.CENTER_CROP)
        }
    }

    private fun addButtons() {
        addButton(getString(R.string.no_filter_button_text), GPUImageFilter())
        addButton(getString(R.string.sketch_button_text), GPUImageSketchFilter())
        addButton(getString(R.string.color_invert_button_text), GPUImageColorInvertFilter())
        addButton(getString(R.string.solarize_button_text), GPUImageSolarizeFilter())
        addButton(getString(R.string.grayscale_button_text), GPUImageGrayscaleFilter())
        addButton(getString(R.string.brightness_button_text), GPUImageBrightnessFilter(BRIGHTNESS))
        addButton(getString(R.string.contrast_button_text), GPUImageContrastFilter(CONTRAST))
        addButton(getString(R.string.pixelation_button_text), GPUImagePixelationFilter().apply { setPixel(PIXEL) })
        addButton(getString(R.string.glass_sphere_button_text), GPUImageGlassSphereFilter())
        addButton(getString(R.string.crosshatch_button_text), GPUImageCrosshatchFilter())
        addButton(getString(R.string.gamma_button_text), GPUImageGammaFilter(GAMMA))
    }

    private fun addButton(text: String, filter: GPUImageFilter?) {
        val button = Button(this).apply {
            setText(text)
            setOnClickListener { gpuImageView.filter = filter }
        }
        buttonContainer.addView(button, FLOWLAYOUT_PARAMS)
    }

    @SuppressLint("UnsafeExperimentalUsageError")
    private fun startCameraIfReady() {
        if (!isPermissionsGranted() || cameraProvider == null) {
            return
        }
        val imageAnalysis = getImageAnalysis()
        imageAnalysis.setAnalyzer(executor, ImageAnalysis.Analyzer { imageProxy ->
            if (!::bitmap.isInitialized) {
                allocateBitmapIfNecessary(imageProxy.width, imageProxy.height)
            }
            imageProxy.apply {
                //convert Yuv to Rgb
                image?.let { converter.yuvToRgb(it, bitmap) }
                close()
            }
            gpuImageView.post { gpuImageView.setImage(bitmap) }
        })
        cameraProvider?.let { processCameraProvider ->
            processCameraProvider.unbindAll()
            processCameraProvider.bindToLifecycle(this, CAMERA_SELECTOR_BACK_CAM, imageAnalysis)
        }
    }

    private fun allocateBitmapIfNecessary(width: Int, height: Int): Bitmap {
        if (!::bitmap.isInitialized || bitmap.width != width || bitmap.height != height) {
            bitmap = Bitmap.createBitmap(width, height, BITMAP_CONFIG_ARGB_8888)
        }
        return bitmap
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String?>, grantResults: IntArray) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            startCameraIfReady()
        }
    }

    private fun isPermissionsGranted(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }
}