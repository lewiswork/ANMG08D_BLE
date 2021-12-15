package com.example.navdrawer.ui.connect

import android.app.Activity
import android.bluetooth.BluetoothDevice
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.example.navdrawer.GlobalVariables
import com.example.navdrawer.R
import com.example.navdrawer.databinding.ActivityConnectBinding

//import com.example.navdrawer.databinding.ActivityConnectBinding

class ConnectActivity : AppCompatActivity() {

    private  val mmNames = arrayListOf<String>()
    private  val mmMacs = arrayListOf<String>()
    private  val mmDevices = arrayListOf<BluetoothDevice>()

    private var mmBinding: ActivityConnectBinding? = null
    private val binding get() = mmBinding!!

//    val mmTvTitle = findViewById<TextView>(R.id.tvTitle)
//    val mmBtnCancel = findViewById<Button>(R.id.btnCancel)
//    val mmLvDevices = findViewById<ListView>(R.id.lvDevices)

    private lateinit var mmInfalter:LayoutInflater
    private var mmContainer:ViewGroup? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_connect)
//
//        //mmBinding = ActivityConnectBinding.inflate(layoutInflater)
//        //mmBinding?.tvTitle?.text = "Clicked"
//        mmTvTitle = findViewById<TextView>(R.id.tvTitle)
//        mmTvTitle.text = "Entered"

        //mmBinding = ActivityConnectBinding.inflate(inflater, container, false)

//        //--------------------------------------------------------------------------//
//        // Listener 등록
//        //--------------------------------------------------------------------------//
        val btnCancel = findViewById<Button>(R.id.btnCancel)
        btnCancel.setOnClickListener(listenerCancel)

        val lvDevices = findViewById<ListView>(R.id.lvDevices)
        lvDevices.onItemClickListener = listenerItemClick
//        //--------------------------------------------------------------------------//
//
        getPairedDevices()
    }

//    override fun onCreateView(name: String, context: Context, attrs: AttributeSet): View? {
//
////        mmInfalter = inflater
////        mmContainer = container
//       // val view = inflater.inflate(R.layout.my_fragment, null)
//
//
//        mmBinding = ActivityConnectBinding.inflate(layoutInflater)
//        val root: View? = mmBinding?.root
//        //mmBinding = ActivityConnectBinding.inflate(inflater, container, false)
//
////        //--------------------------------------------------------------------------//
////        // Listener 등록
////        //--------------------------------------------------------------------------//
//        mmBinding?.btnCancel?.setOnClickListener(listenerCancel)
//    //    mmBinding?.lvDevices?.onItemClickListener = listenerItemClick
////        //--------------------------------------------------------------------------//
////
//        //getPairedDevices()
//
//        return root
//    }

    //--------------------------------------------------------------------------//
    // btnCancel 의 OnClick Listener
    //--------------------------------------------------------------------------//
    private val listenerCancel = View.OnClickListener {
        setResult(Activity.RESULT_CANCELED, intent)
        finish()
    }

    //--------------------------------------------------------------------------//
    // ListView 의 OnItemClickListener
    //--------------------------------------------------------------------------//
    private val listenerItemClick =  AdapterView.OnItemClickListener { parent, view, position, id ->
        GlobalVariables.selectedDevice = mmDevices[position]
        setResult(android.app.Activity.RESULT_OK, intent)
        finish()
    }

    //--------------------------------------------------------------------------//
    // Get Paired Device Information and Display on ListView
    //--------------------------------------------------------------------------//
    private fun getPairedDevices() {
        // Get Paired Device Information
        val pairedDevices = GlobalVariables.adapter.bondedDevices
        if (pairedDevices.size > 0) {
            for (device in pairedDevices) {
                mmNames.add(device.name)        // ListView 표시를 위해 사용
                mmMacs.add(device.address)      // ListView 표시를 위해 사용
                mmDevices.add(device)           // 최종 Device 선택 시 사용
            }
        }

        // ListView 에 Device List Display
        val dataList = ArrayList<HashMap<String, Any>>()

        for (i in mmNames.indices) {
            val map = HashMap<String, Any>()
            map["name"] = mmNames[i]
            map["mac"] = mmMacs[i]
            dataList.add(map)
        }
        var keys = arrayOf("name", "mac")
        val ids = intArrayOf(R.id.tvDeviceName, R.id.tvDeviceMac)
        val adapter1 = SimpleAdapter(this, dataList, R.layout.bt_devices_row, keys, ids)

        val lvDevices = findViewById<ListView>(R.id.lvDevices)
        lvDevices.adapter = adapter1
    }
}