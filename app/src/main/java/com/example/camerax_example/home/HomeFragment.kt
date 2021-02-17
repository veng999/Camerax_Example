package com.example.camerax_example.home

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.camerax_example.MainActivity
import com.example.camerax_example.R
import com.example.camerax_example.camera.Callback
import com.example.camerax_example.camera.CameraFragment
import com.example.camerax_example.ext.requireParent
import kotlinx.android.synthetic.main.fragment_home.*

class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var callback: Callback

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callback = context as MainActivity
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        showCamera.setOnClickListener {
            callback.showFragment(CameraFragment())
        }

    }
}