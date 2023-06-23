package com.example.testing_kiosk.activities

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.PendingIntent
import android.content.*
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.*
import android.os.storage.StorageManager
import android.provider.DocumentsContract
import android.text.InputType
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.testing_kiosk.R
import com.example.testing_kiosk.databinding.ActivityMainBinding
import com.example.testing_kiosk.models.DataBean
import com.example.testing_kiosk.print_sdk.PrintCmd
import com.example.testing_kiosk.print_sdk.UsbDriver
import com.example.testing_kiosk.print_sdk.UtilsTools.ReadTxtFile
import com.example.testing_kiosk.print_sdk.UtilsTools.getFromRaw
import com.example.testing_kiosk.print_task.PrintRunnable
import com.example.testing_kiosk.utils.GetPathFromUri
import kotlinx.coroutines.*
import java.io.IOException
import java.io.InputStream
import java.lang.ref.WeakReference
import java.lang.reflect.Array
import java.lang.reflect.InvocationTargetException
import java.text.SimpleDateFormat
import java.util.*

var m_printerQueueList: Queue<DataBean> = LinkedList()

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    var mUsbDriver: UsbDriver? = null
    val m_nStr_001 =
        arrayOf("PrintSelfcheck", "Example01", "PrintDrawable", "PrintBase64", "AutomaticOpen")
    val m_nStr_002 = arrayOf("GetStatus", "GetDevices", "GetCashbox")
    var m_sbEdtText = StringBuilder("")
    var m_sdfDate = SimpleDateFormat("HH:mm:ss ")
    var dataBean: DataBean = DataBean(0, "")
    var mUsbDevice: UsbDevice? = null

    val FILE_SELECT_CODE = 0
    val FILE_SELECT_TXT = 1
    private val REQUEST_EXTERNAL_STORAGE = 1
    var builder: AlertDialog.Builder? = null
    var ACTION_USB_PERMISSION = "com.usb.sample.USB_PERMISSION"
    private val PERMISSIONS_STORAGE = arrayOf(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.READ_PHONE_STATE,
        Environment.DIRECTORY_DOWNLOADS,
        ACTION_USB_PERMISSION
    )
    var txt: InputStream? = null
    var base64Data: String? = null
    var drawable: Drawable? = null
    var logo: Drawable? = null
    private lateinit var alertDialog1: AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
        initAction()
        initAlertDialog()
        runThread()
    }

    private var mUsbReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (UsbManager.ACTION_USB_DEVICE_DETACHED == action) {
                val device =
                    intent.getParcelableExtra<Parcelable>(UsbManager.EXTRA_DEVICE) as UsbDevice?
                if (device!!.productId == 8211 && device.vendorId == 1305
                    || device.productId == 8213 && device.vendorId == 1305
                ) {
                    mUsbDriver!!.closeUsbDevice(device)
                }
            } else if (ACTION_USB_PERMISSION == action) synchronized(this) {
                val device =
                    intent.getParcelableExtra<Parcelable>(UsbManager.EXTRA_DEVICE) as UsbDevice?
                if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                    if (device!!.productId == 8211 && device.vendorId == 1305
                        || device.productId == 8213 && device.vendorId == 1305
                    ) {
                    }
                } else {
//                    Toast.makeText(MainActivity.this, "permission denied for device",
                    showMessage("permission denied for device")
                }
            }
        }
    }

    private fun init() {

        val adapter001: ArrayAdapter<String> =
            ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, m_nStr_001)
        adapter001.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinner001.adapter = adapter001

        val spinner: Spinner = binding.spinner002
        val adapter002: ArrayAdapter<String> =
            ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, m_nStr_002)
        adapter002.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter002


        mUsbDriver = UsbDriver(getSystemService(Context.USB_SERVICE) as UsbManager, this)
        var permissionIntent : PendingIntent?
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissionIntent = PendingIntent.getBroadcast(this, 0, Intent(ACTION_USB_PERMISSION),  PendingIntent.FLAG_MUTABLE)
        } else {
            permissionIntent = PendingIntent.getBroadcast(this, 0, Intent(ACTION_USB_PERMISSION),  PendingIntent.FLAG_ONE_SHOT)
        }
        mUsbDriver!!.setPermissionIntent(permissionIntent)

        // Broadcast listen for new devices
        val filter = IntentFilter()
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
        filter.addAction(ACTION_USB_PERMISSION)
        this.registerReceiver(mUsbReceiver, filter)

    }

    fun runThread() {
        Thread(PrintRunnable(binding)).start()
    }

    private fun initAlertDialog() {
        builder = AlertDialog.Builder(this@MainActivity)
        txt = resources.openRawResource(R.raw.txt)
        base64Data = getFromRaw(txt)
        drawable = ContextCompat.getDrawable(this, R.mipmap.timg)
        logo = ContextCompat.getDrawable(this, R.mipmap.logo)
        alertDialog1 = AlertDialog.Builder(this@MainActivity)
            .setTitle("Continuous print") //标题
            .setIcon(R.mipmap.ic_launcher) //图标
            .setNeutralButton("Stop", DialogInterface.OnClickListener { dialogInterface, i ->

                //添加普通按钮
                dataBean.m_iFunID = 6
                m_printerQueueList.add(dataBean)
                binding.btnPrint1.setEnabled(true)
                binding.btnGetStatus.setEnabled(true)
                binding.btnPrint3.setEnabled(true)
                binding.btnPrint4.setEnabled(true)
                binding.btnPrint4.setEnabled(true)
                binding.checkHex.setEnabled(true)
                binding.btnPrint8.setEnabled(true)
                val button: Button = alertDialog1.getButton(AlertDialog.BUTTON_NEUTRAL)
                if (null == button) {
                    Log.i("carter", "button is null")
                } else {
                    button.text = "Stop"
                }
            }).create()
        alertDialog1.setCanceledOnTouchOutside(false)
    }


    private fun initAction() {

        binding.btnPrint1.setOnClickListener {
            mUsbDriver!!.write(PrintCmd.PrintSelfcheck())
        }
        binding.btnPrint3.setOnClickListener {
            dataBean.m_iFunID = 3
            m_printerQueueList.add(dataBean)
        }
        binding.btnPrint4.setOnClickListener {
            dataBean.m_iFunID = 5
            m_printerQueueList.add(dataBean)
            binding.btnPrint1.setEnabled(false)
            binding.btnGetStatus.setEnabled(false)
            binding.btnPrint3.setEnabled(false)
            binding.btnPrint4.setEnabled(false)
            binding.btnPrint6.setEnabled(false)
            binding.checkHex.setEnabled(false)
            binding.btnPrint8.setEnabled(false)
        }
        binding.btnPrint6.setOnClickListener {
            showFileTXTChooser()
        }
        binding.btnPrint8.setOnClickListener {
//            if (binding.checkHex.isChecked) {
//                dataBean.m_iFunID = 8
//                m_printerQueueList.add(dataBean)
//            } else {
//                dataBean.m_iFunID = 7
//                m_printerQueueList.add(dataBean)
//            }
            val str: String = binding.editText5.text.toString()
            mUsbDriver!!.write(PrintCmd.SetClean()) // Inisialisasi, bersihkan cache
            mUsbDriver!!.write(PrintCmd.SetReadZKmode(0))
            mUsbDriver!!.write(PrintCmd.PrintString(str, 0))
            mUsbDriver!!.write(PrintCmd.PrintFeedline(5)) // Cetak kertas 2 baris
            if (binding.checkCut.isChecked()) {
                mUsbDriver!!.write(PrintCmd.PrintCutpaper(0))
            }
        }
        binding.btnGetStatus.setOnClickListener {
            val value: String = binding.spinner002.getSelectedItem().toString()
            if (value == "GetDevices") {
                checkDevices()
            } else {
                val iDriverCheck = usbDriverCheck()
                if (iDriverCheck == -1) {
                    showMessage("Printer not connected!")
                    return@setOnClickListener
                }
                if (iDriverCheck == 1) {
                    showMessage("Printer unauthorized!")
                    return@setOnClickListener
                }
                if (value == "GetStatus") {
                    dataBean.m_iFunID = 2
                    m_printerQueueList.add(dataBean)
                } else if (value == "GetCashbox") {
                    dataBean.m_iFunID = 9
                    m_printerQueueList.add(dataBean)
                }
            }
        }
        binding.editText2.setText("")
        binding.editText3.setText("")
        binding.editText3.setOnClickListener {
            showFileChooser()
            dataBean.m_iFunID = 3
            m_printerQueueList.add(dataBean)
        }
        binding.editText3.inputType = InputType.TYPE_NULL
        binding.editText5.setText("Coba printtttttttttt")
    }


    @Throws(IOException::class)
    fun checkDevices() {
        var strValue = ""
        var iIndex = 0
        val manager = getSystemService(USB_SERVICE) as UsbManager
        val deviceList = manager.deviceList
        val deviceIterator: Iterator<UsbDevice> = deviceList.values.iterator()
        while (deviceIterator.hasNext()) {
            val device = deviceIterator.next()
            iIndex++
            strValue =
                """$strValue$iIndex DeviceClass:${device.deviceClass}; DeviceId:${device.deviceId}; DeviceName:${device.deviceName}; VendorId:${device.vendorId}; 
                ProductId:${device.productId}; InterfaceCount:${device.interfaceCount}; describeContents:${device.describeContents()};
                DeviceProtocol:${device.deviceProtocol};DeviceSubclass:${device.deviceSubclass};
                """
            strValue = "$strValue****************\r\n"
        }
        if (strValue == "") {
            strValue = "No USB device."
        }
        showMessage(strValue)
    }

    private fun showFileTXTChooser() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "text/plain"
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        try {
            startActivityForResult(
                Intent.createChooser(intent, "Select a txt file"),
                FILE_SELECT_TXT
            )
        } catch (ex: ActivityNotFoundException) {
            showMessage("Please install a File Manager.")
        }
    }

    fun showFileChooser() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "*/*"
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        try {
            startActivityForResult(
                Intent.createChooser(intent, "Select a bmp file"),
                FILE_SELECT_CODE
            )
        } catch (ex: ActivityNotFoundException) {
            showMessage("Please install a File Manager.")
        }
    }

    fun usbDriverCheck(): Int {
        var iResult = -1
        try {
            if (!mUsbDriver!!.isUsbPermission) {
                val manager = getSystemService(USB_SERVICE) as UsbManager
                val deviceList = manager.deviceList

                val deviceIterator: Iterator<UsbDevice> = deviceList.values.iterator()
                while (deviceIterator.hasNext()) {
                    val device = deviceIterator.next()
                    if (device.productId == 8211 && device.vendorId == 1305
                        || device.productId == 8213 && device.vendorId == 1305
                    ) {
                        mUsbDevice = device
                        showMessage("DeviceClass:" + device.deviceClass + "|| Vendor Id " + device.vendorId + "|| DeviceName:" + device.deviceName)
                    }
                }
                if (mUsbDevice != null) {
                    iResult = 1
                    showMessage("M USB Device Not Null")
                    if (mUsbDriver!!.usbAttached(mUsbDevice)) {
                        if (mUsbDriver!!.openUsbDevice(mUsbDevice)) {
                            showMessage("M USB Device openUsbDevice")
                            iResult = 0
                        } else {
                            showMessage("M USB Device can't openUsbDevice" + mUsbDriver!!.openUsbDevice(mUsbDevice).toString())
                        }
                    }else {
                        showMessage("M USB Device isn't attach")
                    }
                } else {
                    showMessage("M USB Device Null")
                }
            } else {
                showMessage("M USB Device Haven't Permission")
                if (!mUsbDriver!!.isConnected) {
                    if (mUsbDriver!!.openUsbDevice(mUsbDevice)) iResult = 0
                } else {
                    iResult = 0
                }
            }
        } catch (e: Exception) {
            showMessage(e.message)
        }
        return iResult
    }

    fun showMessage(sMsg: String?) {
        m_sbEdtText.append(m_sdfDate.format(Date()))
        m_sbEdtText.append(sMsg)
        m_sbEdtText.append("\r\n")
        binding.editText2.setText(m_sbEdtText)
        binding.editText2.setSelection(
            m_sbEdtText.length,
            m_sbEdtText.length
        )
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            FILE_SELECT_CODE -> if (resultCode == RESULT_OK) {
                val context = applicationContext
                val uri = data!!.data
                var file: String? = GetPathFromUri.getPath(context, uri!!)
                if ("File".equals(uri!!.scheme, ignoreCase = true) || file != null) { //
                    println(file)
                    binding.editText3.setText(file) //uri.getPath().replace(":","/")uri.getPath()"/storage/emulated/0/pic/2222.bmp"
                    verifyStoragePermissions()
                } else {
                    file = getStoragePath(context, true)
                    val docId = DocumentsContract.getDocumentId(uri)
                    val split = docId.split(":").toTypedArray()
                    file = file + "/" + split[1] //"/" + split[0] +
                    binding.editText3.setText(file) //uri.getPath().replace(":","/")uri.getPath()"/storage/emulated/0/pic/2222.bmp"
                    verifyStoragePermissions()
                }
            }
            FILE_SELECT_TXT -> if (resultCode == RESULT_OK) {
                val context = applicationContext
                val uri = data!!.data
                var file: String? = GetPathFromUri.getPath(context, uri!!)
                if ("File".equals(uri.scheme, ignoreCase = true) || file != null) { //
                    val readTxt: String = ReadTxtFile(file)
                    binding.editText5.setText(readTxt) //uri.getPath().replace(":","/")uri.getPath()"/storage/emulated/0/pic/2222.bmp"
                    verifyStoragePermissions()
                } else {
                    file = getStoragePath(context, true)
                    val docId = DocumentsContract.getDocumentId(uri)
                    val split = docId.split(":").toTypedArray()
                    file = file + "/" + split[1]
                    val readTxt: String = ReadTxtFile(file)
                    binding.editText5.setText(readTxt)
                    verifyStoragePermissions()
                }
            }
        }
    }


    fun verifyStoragePermissions() {
        // Check if we have write permission
        val permission: Int = ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) //缺少什么权限就写什么权限
        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                this,
                PERMISSIONS_STORAGE,
                REQUEST_EXTERNAL_STORAGE
            )
        }
    }

    fun getStoragePath(mContext: Context, is_removale: Boolean): String? {
        val mStorageManager = mContext.getSystemService(STORAGE_SERVICE) as StorageManager
        var storageVolumeClazz: Class<*>? = null
        try {
            storageVolumeClazz = Class.forName("android.os.storage.StorageVolume")
            val getVolumeList = mStorageManager.javaClass.getMethod("getVolumeList")
            val getPath = storageVolumeClazz.getMethod("getPath")
            val isRemovable = storageVolumeClazz.getMethod("isRemovable")
            val result = getVolumeList.invoke(mStorageManager)
            val length = Array.getLength(result)
            for (i in 0 until length) {
                val storageVolumeElement = Array.get(result, i)
                val path = getPath.invoke(storageVolumeElement) as String
                val removable = isRemovable.invoke(storageVolumeElement) as Boolean
                if (is_removale == removable) {
                    return path
                }
            }
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
        } catch (e: NoSuchMethodException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        }
        return null
    }

    internal class MyHandler(activity: Activity) : Handler() {
        var mWeakReference: WeakReference<Activity>

        init {
            mWeakReference = WeakReference(activity)
        }

        override fun handleMessage(msg: Message) {
            val activity = mWeakReference.get()
            // 处理从子线程发送过来的消息
            val arg1 = msg.arg1 //获取消息携带的属性值
            val arg2 = msg.arg2
            val what = msg.what
            val result = msg.obj
            when (what) {
                0 -> {}
                3, 4 -> MainActivity().showMessage(result.toString())
                else -> {}
            }
        }
    }

}