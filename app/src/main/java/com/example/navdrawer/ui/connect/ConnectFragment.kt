package com.example.navdrawer.ui.connect

import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.navdrawer.GlobalVariables
import com.example.navdrawer.RxThread
import com.example.navdrawer.databinding.FragmentConnectBinding


import java.io.InputStream
import java.io.OutputStream

class ConnectFragment : Fragment() {

    private lateinit var connectViewModel: ConnectViewModel
    private var mmBinding: FragmentConnectBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = mmBinding!!
    private val CONNECT_ACTIVYTY = 0

//    private  var mmInStream: InputStream? = null
//    private  var mmOutStream: OutputStream? = null
//    private  var mmSocket: BluetoothSocket? = null

//    private  var mmRxThread: ReceiveThread?  = null
//    private  var mmDisplayThread: DisplayThread? = null

    private var mmTxBuffer: ByteArray = ByteArray(2048)
    //private var mmRxBuffer: ByteArray = ByteArray(8192)
    private var mmRxBuffer: ByteArray = ByteArray(2048)

//    private var mmRunRxThread = false
//    private var mmRunDisplayThread = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        connectViewModel =
            ViewModelProvider(this).get(ConnectViewModel::class.java)

        mmBinding = FragmentConnectBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textGallery
        connectViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })

        try {
            GlobalVariables.adapter = BluetoothAdapter.getDefaultAdapter()
        }catch (ex:Exception){
            Toast.makeText(this@ConnectFragment.context, "Error occurred while getting BT adapter", Toast.LENGTH_SHORT)
                .show()
        }

        //------------------------------------------------------------------//
        // 각 구성요소의 Listener 등록
        //------------------------------------------------------------------//
        mmBinding?.btnConnect?.setOnClickListener(listenerConnect)               // Connect
        mmBinding?.btnDisconnect?.setOnClickListener(listenerDisconnect)        // Disconnect
//        btnSend.setOnClickListener(listenerSend)                    // Send
//        btnSendHrCmd.setOnClickListener(listenerSendHrCmd)          // Send HrCmd
//        btnClear.setOnClickListener { tvReceiveMsg.text = null }    // Clear(Received Message)
        //------------------------------------------------------------------//

        DisplayBtStatus()

        return root
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CONNECT_ACTIVYTY){
            if (resultCode == RESULT_OK){
                val device = GlobalVariables.selectedDevice

                // Get Socket and Connect using UUID
                GlobalVariables.socket = device.createRfcommSocketToServiceRecord(device.uuids[0].uuid)
                GlobalVariables.socket!!.connect()

                // Get Input/Output Stream using socket
                GlobalVariables.inStream = GlobalVariables.socket!!.inputStream
                GlobalVariables.outStream = GlobalVariables.socket!!.outputStream

                // Receive Thread 시작
                try {
                    GlobalVariables.rxThreadOn =true
                    //mmRxThread = ReceiveThread()
                    GlobalVariables.mmRxThread = RxThread()
                    GlobalVariables.mmRxThread!!.start()

//                    GlobalVariables.displayThreadOn = true
//                    mmDisplayThread = DisplayThread()
//                    mmDisplayThread!!.start()
                } catch (ex: Exception) {
                    Toast.makeText(this@ConnectFragment.context, "Error occurred while starting threads.", Toast.LENGTH_LONG)
                    .show()
                }

                GlobalVariables.isBtConnected = true
                DisplayBtStatus()

                Toast.makeText(this@ConnectFragment.context, "Bluetooth device connected.", Toast.LENGTH_LONG)
                    .show()
            }else if (resultCode == RESULT_CANCELED) {
                //tvStatus.text = "Connection canceled."
            }
        }
    }

    private fun DisplayBtStatus() {
        if (GlobalVariables.isBtConnected) {
            mmBinding?.btnConnect?.isEnabled = false
            mmBinding?.btnDisconnect?.isEnabled = true
            mmBinding?.tvStatus?.text = "Status : Connected"

            mmBinding?.tvDeviceName?.append(GlobalVariables.selectedDevice.name)
            mmBinding?.tvMac?.append(GlobalVariables.selectedDevice.address)
        }
        else
        {
            mmBinding?.btnConnect?.isEnabled = true
            mmBinding?.btnDisconnect?.isEnabled = false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mmBinding = null
        //DisconnectBt()
    }

    //---------------------------------------------------------------------------------------//
    // BT Disconnect 함수, Stream, Socket Close 및 Thread 종료
    //---------------------------------------------------------------------------------------//
    private fun DisconnectBt() {

        if (GlobalVariables.inStream != null) GlobalVariables.inStream!!.close()
        if (GlobalVariables.outStream != null) GlobalVariables.outStream!!.close()
        if (GlobalVariables.socket != null) GlobalVariables.socket!!.close()

        GlobalVariables.rxThreadOn = false
        GlobalVariables.displayThreadOn = false
        mmBinding?.tvStatus?.text = "Status : Disconnected"

        GlobalVariables.rStringQueue.clear()
        GlobalVariables.isBtConnected = false
    }

    //---------------------------------------------------------------------------------------//
    // btnConnect 의 OnClickListener
    //---------------------------------------------------------------------------------------//
    private val listenerConnect = View.OnClickListener {
        if (GlobalVariables.adapter == null) {
            Toast.makeText(this@ConnectFragment.context, "Bluetooth Not Supported", Toast.LENGTH_SHORT)
                .show()
        } else {
            if (GlobalVariables.adapter.isEnabled) {
                //val intent = Intent(this, ConnectActivity::class.java)
                val intent = Intent(this@ConnectFragment.context, ConnectActivity::class.java)

                try {
                    startActivityForResult(intent, CONNECT_ACTIVYTY)
                } catch (ex: Exception) {
                    //tvReceiveMsg.text = ex.message
                }
            } else {
                /*---------------------------------------------------------------------------
                // Bluetooth 가 Disable 되어 있는 경우 메시지 표시
                // 향 후, 사용자에게 BT 연결 작업 요청 코드 추가 예정
                //-------------------------------------------------------------------------*/
                Toast.makeText(this@ConnectFragment.context, "Bluetooth is Disabled", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    //---------------------------------------------------------------------------------------//
    // btnDisconnect 의 OnClickListener
    //---------------------------------------------------------------------------------------//
    private val listenerDisconnect = View.OnClickListener {

        DisconnectBt()

        //GlobalVariables.rStringQueue.clear()

        mmBinding?.tvDeviceName?.text = "Device : "
        mmBinding?.tvMac?.text = "MAC : "

        mmBinding?.btnConnect?.isEnabled = true
        mmBinding?.btnDisconnect?.isEnabled = false
    }


    //---------------------------------------------------------------------------------------//
    // Bluetooth Receive Thread 처리용 Inner Class
    //---------------------------------------------------------------------------------------//
    inner class ReceiveThread : Thread() {
        override fun run() {

            var sidx:Int=0
            var eidx:Int=0
            var str:String=""
            var pk:String=""
            var bytes : Int
            var readMessage : String

            Log.d("ME", "Receive thread started. ID : ${this.id}")
            while (GlobalVariables.rxThreadOn) {
                try {
                    //Log.d("MEA", "Receive Thread")
                    if (GlobalVariables.socket!!.isConnected) {
                        // Receive
                        bytes = GlobalVariables.inStream!!.read(mmRxBuffer)

                        if (bytes > 0) {
                            readMessage = kotlin.text.String(mmRxBuffer, 0, bytes)
                            synchronized(this) { GlobalVariables.rStringQueue.add(readMessage) }
                        }
                    }
                } catch (e: java.io.IOException) {
                    e.printStackTrace()
                    break
                }
            }
            Log.d("ME", "Receive thread finished. ID : ${this.id}")
        }
    }

    inner class DisplayThread : Thread() {
        override fun run() {
            var pk: String = ""
            var sb: StringBuilder = StringBuilder()
            var sidx: Int = 0
            var eidx: Int = 0
            var qEmpty: Boolean = true
            //var qCount: Int = -1

            Log.d("ME", "Display thread started. ID : ${this.id}")
            while (GlobalVariables.displayThreadOn) {
                try {
                    //Log.d("MEA", "Display Thread")
                    synchronized(this) {
                        qEmpty = GlobalVariables.rStringQueue.isEmpty()
                        //qCount = GlobalVariables.sampleQueue.count()
                    }

                    if (!qEmpty) {
                        //if (qCount > 0) {
                        try {
                            synchronized(this) {
                                sb.append(GlobalVariables.rStringQueue.remove())
                            }

                        } catch (ex: NoSuchElementException) {
                            Log.d("MEX", GlobalVariables.rStringQueue.count().toString())
                            ex.printStackTrace()
                            //continue
                            break
                        }

                        while (sb.isNotEmpty()) {
                            if (sb.contains('S')) {
                                sidx = sb.indexOf('S')
                            }

                            if (sb.contains('Z')) {
                                eidx = sb.indexOf('Z')
                                pk = sb.substring(sidx, eidx + 1)

                                if (sb.length > pk.length) {
                                    sidx = eidx + 1
                                    sb = StringBuilder(sb.substring(sidx, sb.length))
                                } else {
                                    sb = StringBuilder("")
                                }
                                Log.d("MED", pk)
                            } else {
                                break
                            }

//                            this@MainActivity.runOnUiThread(java.lang.Runnable {
//                                tvReceiveMsg.text = pk
//                            })
                        }
                    }
                } catch (e: java.io.IOException) {
                    Log.d("MEX", "$sidx/$eidx")
                    e.printStackTrace()
                    break
                    //continue
                }
            }
            Log.d("ME", "Display thread finished. ID : ${this.id}")
        }
    }
}