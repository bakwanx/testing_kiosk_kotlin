package com.example.testing_kiosk.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.testing_kiosk.databinding.ActivityMainBinding
import com.example.testing_kiosk.models.DataBean
import java.util.*

var m_printerQueueList: Queue<DataBean> = LinkedList()

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        init()
        initAction()
//        initAlertDialog()
//        runThread()
    }

    fun initAction(){
        binding.btnTestPrint.setOnClickListener {
            val intent = Intent(this@MainActivity, PrintActivity::class.java)
            startActivity(intent)
        }

        binding.btnTestScanBarcode.setOnClickListener {
            val intent = Intent(this@MainActivity, ScanBarcodeActivity::class.java)
            startActivity(intent)
        }

        binding.btnTestFingerprint.setOnClickListener {
            val intent = Intent(this@MainActivity, FingerprintActivity::class.java)
            startActivity(intent)
        }
    }


}