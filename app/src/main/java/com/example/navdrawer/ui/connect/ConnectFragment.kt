package com.example.navdrawer.ui.connect

import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import android.bluetooth.BluetoothAdapter
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
import com.example.navdrawer.Global
import com.example.navdrawer.PacketKind
import com.example.navdrawer.thread.RxThread
import com.example.navdrawer.databinding.FragmentConnectBinding
import com.example.navdrawer.function.Packet
import com.example.navdrawer.thread.GetPacketThread
import kotlin.experimental.and

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
            Global.adapter = BluetoothAdapter.getDefaultAdapter()
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
                val device = Global.selectedDevice

                // Get Socket and Connect using UUID
                Global.socket = device.createRfcommSocketToServiceRecord(device.uuids[0].uuid)
                Global.socket!!.connect()

                // Get Input/Output Stream using socket
                Global.inStream = Global.socket!!.inputStream
                Global.outStream = Global.socket!!.outputStream

                // Receive Thread 시작
                try {
                    Global.rxThreadOn =true
                    Global.rxThread = RxThread()
                    Global.rxThread!!.start()

                    Global.rxPacketThreadOn =true
                    Global.getPacketThread = GetPacketThread()
                    Global.getPacketThread!!.start()

                } catch (ex: Exception) {
                    Toast.makeText(this@ConnectFragment.context, "Error occurred while starting threads.", Toast.LENGTH_LONG)
                    .show()
                }

                Global.isBtConnected = true
                DisplayBtStatus()

                Toast.makeText(this@ConnectFragment.context, "Bluetooth device connected.", Toast.LENGTH_LONG)
                    .show()
            }else if (resultCode == RESULT_CANCELED) {
                //tvStatus.text = "Connection canceled."
            }
        }
    }

    private fun DisplayBtStatus() {
        if (Global.isBtConnected) {
            mmBinding?.btnConnect?.isEnabled = false
            mmBinding?.btnDisconnect?.isEnabled = true
            mmBinding?.tvStatus?.text = "Status : Connected"

            mmBinding?.tvDeviceName?.append(Global.selectedDevice.name)
            mmBinding?.tvMac?.append(Global.selectedDevice.address)
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
    private fun disconnectBt() {

        // Clear Relays

        if ((Global.hwStat and 0x06) == 0x06.toByte()) {
            Packet.send(Global.outStream, PacketKind.MonSet, 0x00)  // Stop All Monitoring
            Log.d("[ADS]", "Monitoring stopped.")
            Thread.sleep(10) // ok
        }
        Global.hwStat = 0x00
        Packet.send(Global.outStream, PacketKind.HwWrite, Global.hwStat) // Send packet

        if (Global.inStream != null) Global.inStream!!.close()
        if (Global.outStream != null) Global.outStream!!.close()
        if (Global.socket != null) Global.socket!!.close()

        Global.rxThreadOn = false
        Global.rxPacketThreadOn = false

        mmBinding?.tvStatus?.text = "Status : Disconnected"

        Global.rawRxBytesQueue.clear()
        Global.isBtConnected = false

        Global.hwStat = 0
    }

    //---------------------------------------------------------------------------------------//
    // btnConnect 의 OnClickListener
    //---------------------------------------------------------------------------------------//
    private val listenerConnect = View.OnClickListener {
        if (Global.adapter == null) {
            Toast.makeText(this@ConnectFragment.context, "Bluetooth Not Supported", Toast.LENGTH_SHORT)
                .show()
        } else {
            if (Global.adapter.isEnabled) {
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

        disconnectBt()

        mmBinding?.tvDeviceName?.text = "Device : "
        mmBinding?.tvMac?.text = "MAC : "

        mmBinding?.btnConnect?.isEnabled = true
        mmBinding?.btnDisconnect?.isEnabled = false
    }
}