package com.example.camerax_example

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.camerax_example.camera.Callback
import com.example.camerax_example.databinding.ActivityMainBinding
import com.example.camerax_example.ext.showAlert
import com.example.camerax_example.home.HomeFragment
import com.example.camerax_example.utils.PermissionHelper
import com.example.camerax_example.utils.Permissionist


private const val FLAGS_FULLSCREEN = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
        View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR

class MainActivity : AppCompatActivity(), Permissionist, Callback {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()
    override val permissioner by lazy { PermissionHelper(this, ::showRationaleDialog) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        transparentStatusBar()
        setContentView(binding.root)

        if (savedInstanceState == null){
            supportFragmentManager
                    .beginTransaction()
                    .add(R.id.container, HomeFragment())
                    .commit()
        }
    }

    override fun showFragment(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.container, fragment)
            .addToBackStack("Fragment")
            .commit()
    }


    private fun showRationaleDialog(permissions: Collection<String>, retry: () -> Unit): Boolean {
        return viewModel.getPermissionRationaleText(permissions)
                ?.let {
                    showAlert {
                        setMessage(it)
                        setPositiveButton(R.string.yes) { _, _ -> retry() }
                        setNegativeButton(R.string.no) { _, _ -> finish() }
                    }
                    true
                } ?: false
    }

    private fun transparentStatusBar() {
        window.decorView.systemUiVisibility = window.decorView.systemUiVisibility or FLAGS_FULLSCREEN
    }


}