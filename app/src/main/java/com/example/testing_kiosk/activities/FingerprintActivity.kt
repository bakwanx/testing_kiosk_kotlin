package com.example.testing_kiosk.activities

import android.graphics.Bitmap
import android.os.Bundle
import androidx.annotation.WorkerThread
import androidx.appcompat.app.AppCompatActivity
import com.dt.sdk.fps.DtFpScanner
import com.dt.sdk.fps.ICaptureCallback
import com.dt.sdk.fps.common.AbstractUVCCameraHandler
import com.example.testing_kiosk.databinding.ActivityFingerprintBinding
import com.example.testing_kiosk.utils.DrawConvert

class FingerprintActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFingerprintBinding
    private lateinit var mFpScanner: DtFpScanner
    var m_sbEdtText = StringBuilder("")


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFingerprintBinding.inflate(layoutInflater)
        setContentView(binding.root)
        onCheck()
        binding.mainBtnInitialize.setOnClickListener {
            showMessage(mFpScanner.isOpened.toString())
            mFpScanner.start()
//            if(mFpScanner.openDevice()){
//                showMessage("Usb Fingerprint Found")
//            }
        }


    }

    override fun onStop() {
        super.onStop()
        mFpScanner.stop()
    }

    override fun onStart() {
        super.onStart()
        mFpScanner.openDevice()
    }


    fun onCheck(){
        mFpScanner = DtFpScanner(this, object : ICaptureCallback {
            @WorkerThread
            override fun onCaptureCallback(
                rawByteArray: ByteArray,
                width: Int,
                height: Int,
                colorChannel: Int
            ) {
                val tempBitmap: Bitmap = DrawConvert.raw2bitmap(rawByteArray, width, height)
                onBitMapAccept(tempBitmap)
            }

            override fun onErrorOccurred(exp: Exception) {
                showMessage(exp.toString())
            }
        })
        mFpScanner.addCallback(UpdateUiLifecycle)

    }

    fun onBitMapAccept(bitmap: Bitmap){
        binding.cameraView.setTag(bitmap)
        refreshFingerWith(bitmap)
    }

    fun refreshFingerWith(bitmap: Bitmap){
        binding.fingerView.setImageBitmap(bitmap)
    }

    fun showMessage(sMsg: String?) {
        m_sbEdtText.append(sMsg)
        m_sbEdtText.append("\r\n")
        binding.edtDebug.setText(m_sbEdtText)
        binding.edtDebug.setSelection(
            m_sbEdtText.length,
            m_sbEdtText.length
        )
    }

    private val UpdateUiLifecycle: AbstractUVCCameraHandler.Callback =
        object : AbstractUVCCameraHandler.Callback {
            override fun onOpen() {
                showMessage("On Open")
            }

            override fun onStartCapture() {
                showMessage("On Capture FP")
            }

            override fun onStopCapture() {
                showMessage("On Stop FP")
            }

            override fun onClose() {
                showMessage("On Close FP")
            }

            override fun onError(e: Exception) {
                showMessage("On Error FP" + e.toString())
            }
        }
}