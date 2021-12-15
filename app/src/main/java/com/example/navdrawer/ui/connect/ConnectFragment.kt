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
import com.example.navdrawer.databinding.FragmentConnectBinding
import kotlinx.coroutines.Dispatchers.Main

import java.io.InputStream
import java.io.OutputStream

class ConnectFragment : Fragment() {

    private lateinit var connectViewModel: ConnectViewModel
    private var mmBinding: FragmentConnectBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    //private val binding get() = _binding!!
    private val binding get() = mmBinding!!

    private lateinit var mmInStream: InputStream
    private lateinit var mmOutStream: OutputStream
    private lateinit var mmSocket: BluetoothSocket

    private var mmTxBuffer: ByteArray = ByteArray(2048)
    //private var mmRxBuffer: ByteArray = ByteArray(8192)
    //private var mmRxBuffer: ByteArray = ByteArray(2048)
    private var mmRxBuffer: ByteArray = ByteArray(8192)

    private lateinit var mmRxThread: ReceiveThread
    private lateinit var mmDisplayThread: DisplayThread

    private var mmIsRunningRxThread = false
    private var mmIsRunningDisplayThread = false
    private val CONNECT_ACTIVYTY = 0

    private lateinit var mmInfalter:LayoutInflater
    private var mmContainer:ViewGroup? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mmInfalter = inflater
        mmContainer = container

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
//            Toast.makeText(applicationContext, "Error occurred when getting BT adapter", Toast.LENGTH_SHORT)
//                .show()
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

        return root
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CONNECT_ACTIVYTY){
            if (resultCode == RESULT_OK){
                val device = GlobalVariables.Companion.selectedDevice

                // Get Socket and Connect using UUID
                mmSocket = device.createRfcommSocketToServiceRecord(device.uuids[0].uuid)
                mmSocket.connect()

                // Get Input/Output Stream using socket
                mmInStream = mmSocket.inputStream
                mmOutStream = mmSocket.outputStream

                // Receive Thread 시작
                try {
                    mmIsRunningRxThread =true
                    mmRxThread = ReceiveThread()
                    mmRxThread.start()

                    mmIsRunningDisplayThread = true
                    mmDisplayThread = DisplayThread()
                    mmDisplayThread.start()
                } catch (ex: Exception) {
//                    tvReceiveMsg.text = ex.message
                }
              //  tvStatus.text = "Receive thread started."
                //btnSend.isEnabled = true
                mmBinding?.btnConnect?.isEnabled = false
                mmBinding?.btnDisconnect?.isEnabled = true
                //tvReceiveMsg.text = ""

                mmBinding?.tvDeviceName?.append(device.name)
                mmBinding?.tvMac?.append(device.address)

                //tvStatus.text = "Connected."
            }else if (resultCode == RESULT_CANCELED) {
                //tvStatus.text = "Connection canceled."
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mmBinding = null
        DisconnectBt()
    }

    //---------------------------------------------------------------------------------------//
    // BT Disconnect 함수, Stream, Socket Close 및 Thread 종료
    //---------------------------------------------------------------------------------------//
    private fun DisconnectBt() {

        mmInStream.close()
        mmOutStream.close()
        mmSocket.close()

        mmIsRunningRxThread = false
        mmIsRunningDisplayThread = false

        GlobalVariables.sampleQueue.clear()
    }

    //---------------------------------------------------------------------------------------//
    // btnConnect 의 OnClickListener
    //---------------------------------------------------------------------------------------//
    private val listenerConnect = View.OnClickListener {
        if (GlobalVariables.adapter == null) {
//            Toast.makeText(this, "Bluetooth Not Supported", Toast.LENGTH_SHORT)
//                .show()
        } else {
            if (GlobalVariables.adapter.isEnabled) {
                //val intent = Intent(this, ConnectActivity::class.java)
                val intent = Intent(this@ConnectFragment.context, ConnectActivity::class.java)

//                activity?.let {
//                    val intent = Intent(it, ConnectActivity::class.java)
//                    try {
//                        it.startActivity(intent)
//                    } catch (ex: Exception) {
//                        //tvReceiveMsg.text = ex.message
//                        Log.d("Error", ex.message.toString())
//                    }
//                }

                try {
                    startActivityForResult(intent, CONNECT_ACTIVYTY)
                } catch (ex: Exception) {
                    //tvReceiveMsg.text = ex.message
                }
            } else {
//                Toast.makeText(this, "Bluetooth is Disabled", Toast.LENGTH_SHORT)
//                    .show()
            }
        }
    }

    //---------------------------------------------------------------------------------------//
    // btnDisconnect 의 OnClickListener
    //---------------------------------------------------------------------------------------//
    private val listenerDisconnect = View.OnClickListener {

        DisconnectBt()

        GlobalVariables.sampleQueue.clear()

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
            while (mmIsRunningRxThread) {
                try {
                    //Log.d("MEA", "Receive Thread")
                    if (mmSocket.isConnected) {
                        // Receive
                        bytes = mmInStream.read(mmRxBuffer)

                        if (bytes > 0) {

                            readMessage = kotlin.text.String(mmRxBuffer, 0, bytes)

                            synchronized(this) {
                                GlobalVariables.sampleQueue.add(readMessage)
                            }
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
            while (mmIsRunningDisplayThread) {
                try {
                    //Log.d("MEA", "Display Thread")
                    synchronized(this) {
                        qEmpty = GlobalVariables.sampleQueue.isEmpty()
                        //qCount = GlobalVariables.sampleQueue.count()
                    }

                    if (!qEmpty) {
                        //if (qCount > 0) {
                        try {
                            synchronized(this) {
                                sb.append(GlobalVariables.sampleQueue.remove())
                            }

                        } catch (ex: NoSuchElementException) {
                            Log.d("MEX", GlobalVariables.sampleQueue.count().toString())
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