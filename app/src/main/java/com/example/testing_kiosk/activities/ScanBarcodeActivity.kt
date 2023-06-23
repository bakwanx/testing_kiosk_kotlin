package com.example.testing_kiosk.activities

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.testing_kiosk.R
import com.example.testing_kiosk.databinding.ActivityMainBinding
import com.example.testing_kiosk.databinding.ActivityScanBarcodeBinding

class ScanBarcodeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityScanBarcodeBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScanBarcodeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initAction()
    }

    fun initAction(){
        binding.btnShowValue.setOnClickListener {
            Toast.makeText(this, binding.editTextScan.text.toString(), Toast.LENGTH_SHORT).show()
        }
    }
}