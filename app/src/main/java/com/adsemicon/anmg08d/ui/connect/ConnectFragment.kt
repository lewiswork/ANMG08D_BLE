package com.adsemicon.anmg08d.ui.connect

import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
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
import com.adsemicon.anmg08d.GlobalVariables
import com.adsemicon.anmg08d.databinding.FragmentConnectBinding
import com.adsemicon.anmg08d.packet.Packet
import kotlin.experimental.and


class ConnectFragment : Fragment() {

    private lateinit var connectViewModel: ConnectViewModel
    private var mmBinding: FragmentConnectBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = mmBinding!!

    private val REQ_CODE_CONNECT_ACTIVITY = 0
    private val REQ_CODE_BT_EN = 1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {

        Log.d("[ADS] ", "ConnectFragment > onCreateView")

        connectViewModel =
            ViewModelProvider(this).get(ConnectViewModel::class.java)

        mmBinding = FragmentConnectBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textGallery
        connectViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })

        try {
            GlobalVariables.btAdapter = BluetoothAdapter.getDefaultAdapter()

            //------------------------------------------------------------------//
            // 각 구성요소의 Listener 등록
            //------------------------------------------------------------------//
            binding.btnConnect.setOnClickListener(listenerConnect)              // Connect
            binding.btnDisconnect.setOnClickListener(listenerDisconnect)        // Disconnect
            //------------------------------------------------------------------//

            displayBtStatus()

        }catch (ex:Exception){
            Toast.makeText(this@ConnectFragment.context, "BT adapter 추출 실패", Toast.LENGTH_SHORT)
                .show()
            binding.btnConnect.isEnabled=false
        }

//        //------------------------------------------------------------------//
//        // 각 구성요소의 Listener 등록
//        //------------------------------------------------------------------//
//        binding.btnConnect.setOnClickListener(listenerConnect)              // Connect
//        binding.btnDisconnect.setOnClickListener(listenerDisconnect)        // Disconnect
//        //------------------------------------------------------------------//
//
//        DisplayBtStatus()

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()

        Log.d("[ADS] ", "ConnectFragment > onDestroyView")
        mmBinding = null
    }

    override fun onResume() {
        super.onResume()

        Log.d("[ADS] ", "ConnectFragment > onResume")

        if (requireActivity().packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            //Log.d("[ADS] ", "BLE NOT supported.")
            //binding.tvStatus.text = "BLE supported."  // 미지원 시 처리 Code 구현 예정
        } //else {
            //Log.d("[ADS] ", "BLE supported.")
            //binding.tvStatus.text = "BLE NOT supported."
        //}
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQ_CODE_CONNECT_ACTIVITY){
            if (resultCode == RESULT_OK){
//                val device = GlobalVariables.selectedDevice
//
//                GlobalVariables.socket = device.createRfcommSocketToServiceRecord(device.uuids[0].uuid)
//                GlobalVariables.socket!!.connect()
//
//                // Get Input/Output Stream using socket
//                GlobalVariables.inStream = GlobalVariables.socket!!.inputStream
//                GlobalVariables.outStream = GlobalVariables.socket!!.outputStream
//
//                // Received Thread 시작
//                try {
//                    GlobalVariables.rxThreadOn =true
//                    GlobalVariables.rxThread = RxThread()
//                    GlobalVariables.rxThread!!.start()
//
//                    GlobalVariables.rxPacketThreadOn =true
//                    GlobalVariables.getPacketThread = GetPacketThread(requireContext())
//                    GlobalVariables.getPacketThread!!.start()
//                } catch (ex: Exception) {
//                    Toast.makeText(this@ConnectFragment.context,
//                        "Error occurred while starting threads.",
//                        Toast.LENGTH_LONG)
//                        .show()
//                }
//                GlobalVariables.isBtConnected = true
//                DisplayBtStatus()
//
//                Toast.makeText(this@ConnectFragment.context, "Bluetooth device connected.", Toast.LENGTH_LONG)
//                    .show()
//
//                Thread.sleep(100)
//                Packet.send(GlobalVariables.outStream, com.adsemicon.anmg08d.PacketKind.HwRead) // Send packet
            }else if (resultCode == RESULT_CANCELED) {
                //tvStatus.text = "Connection canceled."
            }
        }else if (requestCode == REQ_CODE_BT_EN) {
            if (resultCode == RESULT_OK){
                Toast.makeText(this@ConnectFragment.context, "Bluetooth 기능이 활성화되었습니다.", Toast.LENGTH_LONG)
                    .show()
                showConnectActivity()
            }
            else{
                var msg = "Bluetooth Enable 후 App 사용이 가능합니다."
                Toast.makeText(this@ConnectFragment.context, msg, Toast.LENGTH_LONG)
                    .show()
            }
        }
    }

    private fun displayBtStatus() {
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

    //---------------------------------------------------------------------------------------//
    // BT Disconnect 함수, Stream, Socket Close 및 Thread 종료
    //---------------------------------------------------------------------------------------//
    private fun disconnectBt() {
        if ((GlobalVariables.hwStat and 0x06) == 0x06.toByte()) {
            Packet.send(GlobalVariables.outStream, com.adsemicon.anmg08d.PacketKind.MonSet, 0x00)  // Stop All Monitoring
            Log.d("[ADS]", "Monitoring stopped.")
            Thread.sleep(10) // ok
        }
        GlobalVariables.hwStat = 0x00
        Packet.send(GlobalVariables.outStream, com.adsemicon.anmg08d.PacketKind.HwWrite, GlobalVariables.hwStat) // Send packet

        if (GlobalVariables.inStream != null) GlobalVariables.inStream!!.close()
        if (GlobalVariables.outStream != null) GlobalVariables.outStream!!.close()
        if (GlobalVariables.socket != null) GlobalVariables.socket!!.close()

        GlobalVariables.rxThreadOn = false
        GlobalVariables.rxPacketThreadOn = false

        mmBinding?.tvStatus?.text = "Status : Disconnected"

        GlobalVariables.rxRawBytesQueue.clear()
        GlobalVariables.isBtConnected = false
        GlobalVariables.hwStat = 0
    }

    //---------------------------------------------------------------------------------------//
    // btnConnect 의 OnClickListener
    //---------------------------------------------------------------------------------------//
    private val listenerConnect = View.OnClickListener {
        if (GlobalVariables.btAdapter == null) {
            Toast.makeText(this@ConnectFragment.context, "Bluetooth Not Supported", Toast.LENGTH_SHORT)
                .show()
        } else {
            if (GlobalVariables.btAdapter.isEnabled) {
                //Log.d("[ADS] ", "BT 활성화 상태에서 Connect Activity 자동 진입")
                showConnectActivity()

//                try {
//                    startActivityForResult(intent, REQ_CODE_CONNECT_ACTIVYTY)
//                } catch (ex: Exception) {
//                    //tvReceiveMsg.text = ex.message
//                }
            } else {
                /*---------------------------------------------------------------------------
                // Bluetooth 가 Disable 되어 있는 경우 사용자에게 Enable 처리 요청
                //-------------------------------------------------------------------------*/
                //  Bluetooth 비활성 시 사용자에게 요청
                val eintent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(eintent, REQ_CODE_BT_EN)
            }
        }
    }

    private fun ConnectFragment.showConnectActivity() {
        val intent = Intent(this@ConnectFragment.context, ConnectActivity::class.java)
        startActivityForResult(intent, REQ_CODE_CONNECT_ACTIVITY)
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