package com.adsemicon.anmg08d.ui.connect

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.adsemicon.anmg08d.GlobalVariables
import com.adsemicon.anmg08d.R
import com.adsemicon.anmg08d.databinding.ActivityConnectBinding

class ConnectActivity : AppCompatActivity() {

    private val REQ_CODE_ACCESS_FINE_LOCATION = 0

    private val mmNames = arrayListOf<String>()
    private val mmMacs = arrayListOf<String>()
    private val mmDevices = arrayListOf<BluetoothDevice>()
    var scanResults: ArrayList<BluetoothDevice>? = ArrayList()

    private var _binding: ActivityConnectBinding? = null
    private val binding get() = _binding!!

    private val bleScanner = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            //Log.d("DeviceListActivity", "onScanResult()")
            super.onScanResult(callbackType, result)
            val device = result.device
            Log.d("[ADS] ", "Device found: ${device.address} - ${device.name ?: "Unknown"}")

            addScanResult(result)
        }

        override fun onScanFailed(_error: Int) {
            Log.e("[ADS]", "BLE scan failed with code $_error")
        }
    }

    private val bluetoothLeScanner: BluetoothLeScanner
        get()  {
            val bluetoothManager = this.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            val bluetoothAdapter: BluetoothAdapter = bluetoothManager.adapter
            return bluetoothAdapter.bluetoothLeScanner
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_connect)

        //--------------------------------------------------------------------------//
        // Listener ??????
        //--------------------------------------------------------------------------//
        val btnCancel = findViewById<Button>(R.id.btnCancel)
        btnCancel.setOnClickListener(listenerCancel)

        val lvDevices = findViewById<ListView>(R.id.lvDevices)
        lvDevices.onItemClickListener = listenerItemClick
        //--------------------------------------------------------------------------//

        //getPairedDevices()
    }


    /**
     * Add scan result
     */
    private fun addScanResult(result: ScanResult) {
        // get scanned device
        val device = result.device
        // get scanned device MAC address
        val deviceAddress = device.address
        val deviceName = device.name

        if (device.name == null) return

        // ?????? ??????
        if (scanResults!!.isNotEmpty()) {
            for (dev in scanResults!!) {
                //if (dev.name == null || dev.address == deviceAddress) return
                if (dev.address == deviceAddress) return
            }
        }
        // add arrayList
        scanResults?.add(device)
        mmNames.add(device.name)        // ListView ????????? ?????? ??????
        mmMacs.add(device.address)      // ListView ????????? ?????? ??????
        mmDevices.add(device)           // ?????? Device ?????? ??? ??????

        // ListView ??? Device List Display
        val dataList = ArrayList<HashMap<String, Any>>()

        for (i in mmNames.indices) {
            val map = HashMap<String, Any>()
            map["name"] = mmNames[i]
            map["mac"] = mmMacs[i]
            dataList.add(map)
        }

        var keys = arrayOf("name", "mac")
        val ids = intArrayOf(R.id.tvDeviceName, R.id.tvDeviceMac)
        val adapter1 = SimpleAdapter(this, dataList, R.layout.row_bt_devices, keys, ids)

        val lvDevices = findViewById<ListView>(R.id.lvDevices)
        lvDevices.adapter = adapter1

        // status text UI update
        //statusTxt.set("add scanned device: $deviceAddress")
        Log.d("[ADS] ", "add scanned device: $deviceName / $deviceAddress")
        // scanlist update ?????????
        //_listUpdate.value = Event(true)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            REQ_CODE_ACCESS_FINE_LOCATION -> {
                if (grantResults.isEmpty()) {
                    throw RuntimeException("Empty permission result")
                }
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d( "[ADS] ", "ACCESS_FINE_LOCATION granted.")
//                    Log.d("[ADS] ", "Proceed next sequence.2")

                    bluetoothLeScanner.startScan(bleScanner)

                } else {
                    Log.d( "[ADS] ", "ACCESS_FINE_LOCATION declined.")
                    //Log.d("[ADS] ", "exit activity.")
                    Toast.makeText(applicationContext, "???????????? ?????? ?????? ??? App ????????? ???????????????.", Toast.LENGTH_LONG)
                        .show()
                }
            }
        }
    }

    override fun onStart() {
        Log.d("[ADS] ", "Connect Fragment > Connect Activity > onStart()")

        super.onStart()

       //  enable location
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.d("[ADS] ", "ACCESS_FINE_LOCATION disabled.")

            // ?????? ????????? ??? ??????????????? ??????
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQ_CODE_ACCESS_FINE_LOCATION
            )
        }else   {
            // ?????? ?????? ??? ?????? Sequence ??????
            Log.d("[ADS] ", "ACCESS_FINE_LOCATION enabled.")
            //Log.d("[ADS] ", "Proceed next sequence")

            bluetoothLeScanner.startScan(bleScanner)
        }
    }

    override fun onStop() {
        Log.d("[ADS] ", "Connect Fragment > Connect Activity > onStop()")
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Log.d("[ADS] ", "ACCESS_FINE_LOCATION is not permitted.")
            return
        }
        bluetoothLeScanner.stopScan(bleScanner)
        Log.d("[ADS] ", "BLUETOOTH SCAN stopped.")
        super.onStop()
    }

    //--------------------------------------------------------------------------//
    // btnCancel ??? OnClick Listener
    //--------------------------------------------------------------------------//
    private val listenerCancel = View.OnClickListener {
        setResult(Activity.RESULT_CANCELED, intent)
        finish()
    }

    //--------------------------------------------------------------------------//
    // ListView ??? OnItemClickListener
    //--------------------------------------------------------------------------//
    private val listenerItemClick =  AdapterView.OnItemClickListener { parent, view, position, id ->
        GlobalVariables.selectedDevice = mmDevices[position]
        //GlobalVariables.selectedDevice= scanResults[position]
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    //--------------------------------------------------------------------------//
    // Get Paired Device Information and Display on ListView
    //--------------------------------------------------------------------------//
    private fun getPairedDevices() {
        // Get Paired Device Information
        val pairedDevices = GlobalVariables.btAdapter.bondedDevices
        if (pairedDevices.size > 0) {
            for (device in pairedDevices) {
                mmNames.add(device.name)        // ListView ????????? ?????? ??????
                mmMacs.add(device.address)      // ListView ????????? ?????? ??????
                mmDevices.add(device)           // ?????? Device ?????? ??? ??????
            }
        }

        // ListView ??? Device List Display
        val dataList = ArrayList<HashMap<String, Any>>()

        for (i in mmNames.indices) {
            val map = HashMap<String, Any>()
            map["name"] = mmNames[i]
            map["mac"] = mmMacs[i]
            dataList.add(map)
        }

        var keys = arrayOf("name", "mac")
        val ids = intArrayOf(R.id.tvDeviceName, R.id.tvDeviceMac)
        val adapter1 = SimpleAdapter(this, dataList, R.layout.row_bt_devices, keys, ids)

        val lvDevices = findViewById<ListView>(R.id.lvDevices)
        lvDevices.adapter = adapter1
    }
}