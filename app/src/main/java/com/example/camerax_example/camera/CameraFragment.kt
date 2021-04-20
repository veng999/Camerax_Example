package com.example.camerax_example.camera

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.*
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.core.impl.Config
import androidx.camera.core.impl.PreviewConfig
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.example.camerax_example.MainViewModel
import com.example.camerax_example.databinding.FragmentCameraBinding
import com.example.camerax_example.ext.initCamera
import com.example.camerax_example.ext.observe
import com.example.camerax_example.ext.requireParent
import com.example.camerax_example.ext.simulateClick
import com.example.camerax_example.utils.Permissionist
import kotlinx.android.synthetic.main.fragment_camera.*
import timber.log.Timber

private const val TAG = "CameraFragment"
private const val DEFAULT_LENS_FACING = CameraSelector.LENS_FACING_BACK

class CameraFragment : Fragment() {

    private var binding: FragmentCameraBinding? = null
    private val mainViewModel: MainViewModel by viewModels()
    private val viewModel: CameraViewModel by viewModels()

    private lateinit var previewUseCase: Preview
    private lateinit var captureUseCase: ImageCapture

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycle.addObserver(viewModel)
        viewModel.quit.observe(this, Observer {
            requireActivity().finishAfterTransition()
        })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentCameraBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.let {
            it.switchCamera.setOnClickListener { viewModel.switchToNextLensFacing() }
            it.filterButton.setOnClickListener { startActivity(Intent(requireContext(), FilterActivity::class.java)) }
        }

        setObservers()
        viewModel.init(requireParent<Permissionist>().permissioner)
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }

    private fun setObservers() {
        observe(viewModel.startCamera) {
            binding?.viewFinder?.post(::startCamera)
        }
        observe(mainViewModel.onKeyDown(MainViewModel.KeyCode.SHUTTER)) {
            shutter.simulateClick()
        }
        observe(viewModel.lensFacing) {
            startCamera()
        }
    }

    private fun startCamera() {
        initCamera(requireContext()) { provider ->
            bindCameraUseCases(provider)
        }
    }

    @SuppressLint("UnsafeExperimentalUsageError", "RestrictedApi")
    private fun bindCameraUseCases(cameraProvider: ProcessCameraProvider) {
        // Create configuration object for the viewfinder use case

        previewUseCase = Preview.Builder()
                .build()
                .also { prewiew ->
                    prewiew.setSurfaceProvider(viewFinder.surfaceProvider)
                }


        captureUseCase = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                .build()

        val orientationEventListener = object : OrientationEventListener(requireContext()) {
            override fun onOrientationChanged(orientation : Int) {
                // Monitors orientation values to determine the target rotation value
                val rotation : Int = when (orientation) {
                    in 45..134 -> Surface.ROTATION_270
                    in 135..224 -> Surface.ROTATION_180
                    in 225..314 -> Surface.ROTATION_90
                    else -> Surface.ROTATION_0
                }

                captureUseCase.targetRotation = rotation
            }
        }
        orientationEventListener.enable()

        shutter.setOnClickListener {
            viewModel.takePicture(captureUseCase)
        }

        try {
            // Unbind use cases before rebinding
            cameraProvider.unbindAll()

            val lensFacingValue = viewModel.lensFacing.value ?: DEFAULT_LENS_FACING
            // Bind use cases to lifecycle
            val camera = cameraProvider.bindToLifecycle(
                    viewLifecycleOwner,
                    CameraSelector.Builder().requireLensFacing(lensFacingValue).build(),
                    previewUseCase,
                    captureUseCase
            )
        } catch (exc: IllegalStateException) {
            Timber.e(exc, "Use case binding failed. This must be running on main thread.")
        }
    }
}
