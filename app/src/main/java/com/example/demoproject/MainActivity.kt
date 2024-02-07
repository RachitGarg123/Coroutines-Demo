package com.example.demoproject

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.example.demoproject.databinding.ActivityMainBinding
import com.example.demoproject.viewmodel.MainViewModel

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initVariables()
        setOnClickListeners()
    }

    private fun initVariables() {
        viewModel = MainViewModel()
    }

    private fun setOnClickListeners() {
        binding.btnClick.setOnClickListener(this)
    }

    override fun onClick(view: View?) {
        when(view) {
            binding.btnClick -> {
                viewModel.hitApi()
            }
        }
    }
}