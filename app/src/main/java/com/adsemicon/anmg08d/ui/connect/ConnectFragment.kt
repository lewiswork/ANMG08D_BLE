package com.adsemicon.anmg08d.ui.connect

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
import com.adsemicon.anmg08d.thread.RxThread
import com.adsemicon.anmg08d.databinding.FragmentConnectBinding
import com.adsemicon.anmg08d.packet.Packet
import com.adsemicon.anmg08d.thread.GetPacketThread
import kotlin.experimental.and

class ConnectFragment : Fragment() {

    private lateinit var connectViewModel: ConnectViewModel
    private var mmBinding: FragmentConnectBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = mmBinding!!
    private val CONNECT_ACTIVYTY = 0

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
            com.adsemicon.anmg08d.Global.adapter = BluetoothAdapter.getDefaultAdapter()
        }catch (ex:Exception){
            Toast.makeText(this@ConnectFragment.context, "Error occurred while getting BT adapter", Toast.LENGTH_SHORT)
                .show()
        }

        //------------------------------------------------------------------//
        // 각 구성요소의 Listener 등록
        //------------------------------------------------------------------//
        mmBinding?.btnConnect?.setOnClickListener(listenerConnect)              // Connect
        mmBinding?.btnDisconnect?.setOnClickListener(listenerDisconnect)        // Disconnect
        //------------------------------------------------------------------//

        DisplayBtStatus()

        return root
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CONNECT_ACTIVYTY){
            if (resultCode == RESULT_OK){
                val device = com.adsemicon.anmg08d.Global.selectedDevice

                // Get Socket and Connect using UUID
                com.adsemicon.anmg08d.Global.socket = device.createRfcommSocketToServiceRecord(device.uuids[0].uuid)
                com.adsemicon.anmg08d.Global.socket!!.connect()

                // Get Input/Output Stream using socket
                com.adsemicon.anmg08d.Global.inStream = com.adsemicon.anmg08d.Global.socket!!.inputStream
                com.adsemicon.anmg08d.Global.outStream = com.adsemicon.anmg08d.Global.socket!!.outputStream

                // Receive Thread 시작
                try {
                    com.adsemicon.anmg08d.Global.rxThreadOn =true
                    com.adsemicon.anmg08d.Global.rxThread = RxThread()
                    com.adsemicon.anmg08d.Global.rxThread!!.start()

                    com.adsemicon.anmg08d.Global.rxPacketThreadOn =true
                    //Global.getPacketThread = GetPacketThread()
                    com.adsemicon.anmg08d.Global.getPacketThread = GetPacketThread(context!!)
                    com.adsemicon.anmg08d.Global.getPacketThread!!.start()

                } catch (ex: Exception) {
                    Toast.makeText(this@ConnectFragment.context, "Error occurred while starting threads.", Toast.LENGTH_LONG)
                    .show()
                }

                com.adsemicon.anmg08d.Global.isBtConnected = true
                DisplayBtStatus()

                Toast.makeText(this@ConnectFragment.context, "Bluetooth device connected.", Toast.LENGTH_LONG)
                    .show()

                Thread.sleep(100)
                Packet.send(com.adsemicon.anmg08d.Global.outStream, com.adsemicon.anmg08d.PacketKind.HwRead) // Send packet
            }else if (resultCode == RESULT_CANCELED) {
                //tvStatus.text = "Connection canceled."
            }
        }
    }

    private fun DisplayBtStatus() {
        if (com.adsemicon.anmg08d.Global.isBtConnected) {
            mmBinding?.btnConnect?.isEnabled = false
            mmBinding?.btnDisconnect?.isEnabled = true
            mmBinding?.tvStatus?.text = "Status : Connected"

            mmBinding?.tvDeviceName?.append(com.adsemicon.anmg08d.Global.selectedDevice.name)
            mmBinding?.tvMac?.append(com.adsemicon.anmg08d.Global.selectedDevice.address)
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

        if ((com.adsemicon.anmg08d.Global.hwStat and 0x06) == 0x06.toByte()) {
            Packet.send(com.adsemicon.anmg08d.Global.outStream, com.adsemicon.anmg08d.PacketKind.MonSet, 0x00)  // Stop All Monitoring
            Log.d("[ADS]", "Monitoring stopped.")
            Thread.sleep(10) // ok
        }
        com.adsemicon.anmg08d.Global.hwStat = 0x00
        Packet.send(com.adsemicon.anmg08d.Global.outStream, com.adsemicon.anmg08d.PacketKind.HwWrite, com.adsemicon.anmg08d.Global.hwStat) // Send packet

        if (com.adsemicon.anmg08d.Global.inStream != null) com.adsemicon.anmg08d.Global.inStream!!.close()
        if (com.adsemicon.anmg08d.Global.outStream != null) com.adsemicon.anmg08d.Global.outStream!!.close()
        if (com.adsemicon.anmg08d.Global.socket != null) com.adsemicon.anmg08d.Global.socket!!.close()

        com.adsemicon.anmg08d.Global.rxThreadOn = false
        com.adsemicon.anmg08d.Global.rxPacketThreadOn = false

        mmBinding?.tvStatus?.text = "Status : Disconnected"

        com.adsemicon.anmg08d.Global.rawRxBytesQueue.clear()
        com.adsemicon.anmg08d.Global.isBtConnected = false

        com.adsemicon.anmg08d.Global.hwStat = 0
    }

    //---------------------------------------------------------------------------------------//
    // btnConnect 의 OnClickListener
    //---------------------------------------------------------------------------------------//
    private val listenerConnect = View.OnClickListener {
        if (com.adsemicon.anmg08d.Global.adapter == null) {
            Toast.makeText(this@ConnectFragment.context, "Bluetooth Not Supported", Toast.LENGTH_SHORT)
                .show()
        } else {
            if (com.adsemicon.anmg08d.Global.adapter.isEnabled) {
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