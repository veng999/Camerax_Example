package com.example.camerax_example.camera

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.util.Rational
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.core.impl.CaptureConfig
import androidx.camera.core.impl.OptionsBundle
import androidx.camera.core.impl.PreviewConfig
import androidx.camera.core.impl.UseCaseConfig
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

private const val TAG = "CameraFragment"

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

    @SuppressLint("RestrictedApi")
    private fun bindCameraUseCases(cameraProvider: ProcessCameraProvider) {
        // Create configuration object for the viewfinder use case


        previewUseCase = Preview.Builder()
                .build()
                .also { prewiew ->
                    prewiew.setSurfaceProvider(viewFinder.surfaceProvider)
                }

        captureUseCase = ImageCapture.Builder()
                // We don't set a resolution for image capture; instead, we
                // select a capture mode which will infer the appropriate
                // resolution based on aspect ration and requested mode
                .build()
        shutter.setOnClickListener {
            viewModel.takePicture(captureUseCase)
        }

        try {
            // Unbind use cases before rebinding
            cameraProvider.unbindAll()

            // Bind use cases to lifecycle
            val camera = cameraProvider.bindToLifecycle(
                    viewLifecycleOwner,
                    CameraSelector.Builder().requireLensFacing(viewModel.lensFacing.value!!).build(),
                    previewUseCase,
                    captureUseCase
            )
        } catch (exc: IllegalStateException) {
            Log.e(TAG, "Use case binding failed. This must be running on main thread.", exc)
        }
    }
}
