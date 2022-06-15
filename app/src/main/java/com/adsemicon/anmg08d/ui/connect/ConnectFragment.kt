package com.adsemicon.anmg08d.ui.connect

import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import android.bluetooth.*
import android.content.Context.BLUETOOTH_SERVICE
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
import com.adsemicon.anmg08d.ui.connect.Constants.Companion.CLIENT_CHARACTERISTIC_CONFIG
//import com.adsemicon.anmg08d.ui.connect.Constants.Companion.UUID_CHAR_NOTIFICATION
import com.adsemicon.anmg08d.ui.connect.Constants.Companion.UUID_CHAR_RW
import com.adsemicon.anmg08d.ui.connect.Constants.Companion.UUID_SERVICE
import java.util.*


class ConnectFragment : Fragment() {

    private lateinit var connectViewModel: ConnectViewModel
    private var mmBinding: FragmentConnectBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = mmBinding!!

    private val REQ_CODE_CONNECT_ACTIVITY = 0
    private val REQ_CODE_BT_EN = 1

    //ble adapter
    //private var bleAdapter: BluetoothAdapter? = null


    private var bleGatt: BluetoothGatt? = null      // BLE Gatt



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
            //GlobalVariables.btAdapter = BluetoothAdapter.getDefaultAdapter()

            // ble manager
            val bleManager: BluetoothManager = requireContext().getSystemService( BLUETOOTH_SERVICE ) as BluetoothManager
            // set ble adapter
            GlobalVariables.btAdapter= bleManager.adapter

            //------------------------------------------------------------------//
            // 각 구성요소의 Listener 등록
            //------------------------------------------------------------------//
            binding.btnConnect.setOnClickListener(listenerConnect)              // Connect
            binding.btnDisconnect.setOnClickListener(listenerDisconnect)        // Disconnect

            binding.button.setOnClickListener (listenerSend)
            //------------------------------------------------------------------//

            displayBtStatus()

        }catch (ex:Exception){
            Toast.makeText(this@ConnectFragment.context, "BT adapter 추출 실패", Toast.LENGTH_SHORT)
                .show()
            binding.btnConnect.isEnabled=false
        }

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
            Log.d("[ADS] ", "BLE supported.")
            //binding.tvStatus.text = "BLE supported."  // 미지원 시 처리 Code 구현 예정
        } else {
            Log.d("[ADS] ", "BLE NOT supported.")
            //binding.tvStatus.text = "BLE NOT supported."
        }
    }

    fun bleConnectionProc() {

        activity?.runOnUiThread {
//            binding.tvDeviceName.text = GlobalVariables.selectedDevice.name
//            binding.tvMac.text = GlobalVariables.selectedDevice.address


//        // Get Input/Output Stream using socket
//        GlobalVariables.inStream = GlobalVariables.socket!!.inputStream
//        GlobalVariables.outStream = GlobalVariables.socket!!.outputStream

            // Received Thread 시작
            try {
//            GlobalVariables.rxThreadOn = true
//            GlobalVariables.rxThread = RxThread()
//            GlobalVariables.rxThread!!.start()

//            GlobalVariables.rxPacketThreadOn = true
//            GlobalVariables.getPacketThread = GetPacketThread(requireContext())
//            GlobalVariables.getPacketThread!!.start()
            } catch (ex: Exception) {
                Toast.makeText(this@ConnectFragment.context,
                    "Error occurred while starting threads.",
                    Toast.LENGTH_LONG)
                    .show()
            }
            GlobalVariables.isBtConnected = true
            displayBtStatus()

            Toast.makeText(this@ConnectFragment.context,
                "Bluetooth device connected.",
                Toast.LENGTH_LONG)
                .show()

//        Thread.sleep(100)
//        Packet.send(GlobalVariables.outStream, PacketKind.HwRead) // Send packet

        }
    }

    /**
     * BLE gattClientCallback
     */
    private val gattClientCallback: BluetoothGattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            if( status == BluetoothGatt.GATT_FAILURE ) {
                disconnectGattServer()
                return
            } else if( status != BluetoothGatt.GATT_SUCCESS ) {
                disconnectGattServer()
                return
            }

            if( newState == BluetoothProfile.STATE_CONNECTED ) {
                // update the connection status message
                Log.d("[ADS] ", "Connected to the GATT server")
                gatt.discoverServices()

                bleConnectionProc()
            } else if ( newState == BluetoothProfile.STATE_DISCONNECTED ) {
                disconnectGattServer()
            }
        }


        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)

            // check if the discovery failed
            if (status != BluetoothGatt.GATT_SUCCESS) {
                Log.e("[ADS] ", "Device service discovery failed, status: $status")
                return
            }

            // find discovered characteristics
            for (service in gatt!!.services) {
                Log.d("[ADS] ", "[SVC] "+ service.uuid.toString())

                for (char in service.characteristics) {
                    Log.d("[ADS] ", "  [CHAR]" + char.uuid.toString())

                    for (desc in char.descriptors){
                        Log.d("[ADS] ", "    [DESC]" + desc.uuid.toString())
                    }
                }
            }

            val uuidService = UUID.fromString(UUID_SERVICE)
            val uuidRw = UUID.fromString(UUID_CHAR_RW)
            val ch = bleGatt?.getService(uuidService)?.getCharacteristic(uuidRw)

            //var result = gatt.setCharacteristicNotification(ch, true)
            gatt.setCharacteristicNotification(ch, true)


            // log for successful discovery
            //Log.d("[ADS] ", result.toString())
            Log.d("[ADS] ", "Services discovery is successfully.")

            val descriptor:BluetoothGattDescriptor? = ch?.getDescriptor(
                UUID.fromString(CLIENT_CHARACTERISTIC_CONFIG)
            )
            descriptor?.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            gatt!!.writeDescriptor(descriptor);
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic,
        ) {
            super.onCharacteristicChanged(gatt, characteristic)

            //activity?.runOnUiThread(Runnable {
            //Log.d("[ADS] ", "onCharacteristicChanged: 변화 감지 했습니다")
            Log.d("[ADS] ", "RX : " + characteristic.getStringValue(0))
            //})
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int,
        ) {
            super.onCharacteristicWrite(gatt, characteristic, status)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d("[ADS] ", "Characteristic written successfully")
            } else {
                Log.e("[ADS] ", "Characteristic write successfully, status: $status")
                disconnectGattServer()
            }
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic,
            status: Int,
        ) {
            super.onCharacteristicRead(gatt, characteristic, status)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d("[ADS] ", "Characteristic read successfully")
                readCharacteristic(characteristic)
            } else {
                Log.e("[ADS] ", "Characteristic read unsuccessful, status: $status")
                // Trying to read from the Time Characteristic? It doesnt have the property or permissions
                // set to allow this. Normally this would be an error and you would want to:
                // disconnectGattServer();
            }
        }

        /**
         * Log the value of the characteristic
         * @param characteristic
         */
        private fun readCharacteristic(characteristic: BluetoothGattCharacteristic) {
            val msg = characteristic.getStringValue(0)
            Log.d("[ADS] ", "read: $msg")
        }
    }

    /**
     * Disconnect Gatt Server
     */
    fun disconnectGattServer() {
        Log.d("[ADS] ", "Closing Gatt connection")
        // disconnect and close the gatt
        if (bleGatt != null) {
            bleGatt!!.disconnect()
            bleGatt!!.close()
        }
    }
//
//    fun write(){
//        val cmdCharacteristic = BluetoothUtils.findCommandCharacteristic(bleGatt!!)
//        // disconnect if the characteristic is not found
//        if (cmdCharacteristic == null) {
//            Log.e("[ADS] ", "Unable to find cmd characteristic")
//            disconnectGattServer()
//            return
//        }
//        val cmdBytes = ByteArray(2)
//        cmdBytes[0] = 1
//        cmdBytes[1] = 2
//        cmdCharacteristic.value = cmdBytes
//        val success: Boolean = bleGatt!!.writeCharacteristic(cmdCharacteristic)
//        // check the result
//        if( !success ) {
//            Log.e("[ADS] ", "Failed to write command")
//        }
//    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQ_CODE_CONNECT_ACTIVITY){
            if (resultCode == RESULT_OK){
                val device = GlobalVariables.selectedDevice
                bleGatt = device?.connectGatt(context, false, gattClientCallback)
            }else if (resultCode == RESULT_CANCELED) {
                //binding.tvStatus.text = "Connection canceled."
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

//    private fun displayBtStatus() {
//        if (GlobalVariables.isBtConnected) {
//            mmBinding?.btnConnect?.isEnabled = false
//            mmBinding?.btnDisconnect?.isEnabled = true
//            mmBinding?.tvStatus?.text = "Status : Connected"
//
//            mmBinding?.tvDeviceName?.append(GlobalVariables.selectedDevice.name)
//            mmBinding?.tvMac?.append(GlobalVariables.selectedDevice.address)
//        }
//        else
//        {
//            mmBinding?.btnConnect?.isEnabled = true
//            mmBinding?.btnDisconnect?.isEnabled = false
//        }
//    }

    private fun displayBtStatus() {
        if (GlobalVariables.isBtConnected) {
            binding.btnConnect.isEnabled = false
            binding.btnDisconnect.isEnabled = true
            binding.tvStatus.text = "Status : Connected"

            binding.tvDeviceName.append(GlobalVariables.selectedDevice.name)
            binding.tvMac.append(GlobalVariables.selectedDevice.address)
        } else {
            binding.btnConnect.isEnabled = true
            binding.btnDisconnect.isEnabled = false
        }
    }

    //---------------------------------------------------------------------------------------//
    // BT Disconnect 함수, Stream, Socket Close 및 Thread 종료
    //---------------------------------------------------------------------------------------//
    private fun disconnectBt() {
//        if ((GlobalVariables.hwStat and 0x06) == 0x06.toByte()) {
//            Packet.send(GlobalVariables.outStream, PacketKind.MonSet, 0x00)  // Stop All Monitoring
//            Log.d("[ADS]", "Monitoring stopped.")
//            Thread.sleep(10) // ok
//        }
        GlobalVariables.hwStat = 0x00
        //Packet.send(GlobalVariables.outStream, PacketKind.HwWrite, GlobalVariables.hwStat) // Send packet

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
                showConnectActivity()
            } else {
                /*---------------------------------------------------------------------------
                // Bluetooth 가 Disable 되어 있는 경우 사용자에게 Enable 처리 요청
                //-------------------------------------------------------------------------*/
                //  Bluetooth 비활성 시 사용자에게 요청
                val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(intent, REQ_CODE_BT_EN)
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
        //disconnectBt()
        disconnectGattServer()
        disconnectBt()

        binding.tvDeviceName.text = "Device : "
        binding.tvMac.text = "MAC : "

        binding.btnConnect.isEnabled = true
        binding.btnDisconnect.isEnabled = false
    }

    private val listenerSend = View.OnClickListener {

        val uuidService = UUID.fromString(UUID_SERVICE)
        val uuidRw = UUID.fromString(UUID_CHAR_RW)

        val ch = bleGatt!!.getService(uuidService).getCharacteristic(uuidRw)

        var ba = ByteArray(9)

        ba[0] = 0x02
        ba[1] = 'H'.code.toByte()
        ba[2] = 'W'.code.toByte()
        ba[3] = '0'.code.toByte()
        ba[4] = '0'.code.toByte()
        ba[5] = '1'.code.toByte()
        ba[6] = 0x02
        ba[7] = 0xfe.toByte()
        ba[8] = 0x03
        //ch.setValue("OPQ");

        ch.setValue(ba)
        bleGatt?.writeCharacteristic(ch)
    }
}