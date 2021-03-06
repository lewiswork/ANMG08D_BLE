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
import com.adsemicon.anmg08d.PacketKind
import com.adsemicon.anmg08d.databinding.FragmentConnectBinding
import com.adsemicon.anmg08d.packet.Packet
import com.adsemicon.anmg08d.thread.GetPacketThread
import com.adsemicon.anmg08d.ui.connect.Constants.Companion.CLIENT_CHARACTERISTIC_CONFIG
import com.adsemicon.anmg08d.ui.connect.Constants.Companion.UUID_CHAR_RW_NOTIFY
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

    //private var bleGatt: BluetoothGatt? = null      // BLE Gatt

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

            // ble manager
            val bleManager: BluetoothManager = requireContext().getSystemService( BLUETOOTH_SERVICE ) as BluetoothManager

            // set ble adapter
            GlobalVariables.btAdapter= bleManager.adapter

            //------------------------------------------------------------------//
            // ??? ??????????????? Listener ??????
            //------------------------------------------------------------------//
            binding.btnConnect.setOnClickListener(listenerConnect)              // Connect
            binding.btnDisconnect.setOnClickListener(listenerDisconnect)        // Disconnect

            //binding.button.setOnClickListener (listenerSend)
            //------------------------------------------------------------------//

            displayBtStatus()

        }catch (ex:Exception){
            Toast.makeText(this@ConnectFragment.context, "BT adapter ?????? ??????", Toast.LENGTH_SHORT)
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
        } else {
            Log.d("[ADS] ", "BLE NOT supported.")
        }
    }

    fun bleConnectionProc() {
        activity?.runOnUiThread {
//            binding.tvDeviceName.text = GlobalVariables.selectedDevice.name
//            binding.tvMac.text = GlobalVariables.selectedDevice.address


//        // Get Input/Output Stream using socket
//        GlobalVariables.inStream = GlobalVariables.socket!!.inputStream
//        GlobalVariables.outStream = GlobalVariables.socket!!.outputStream

//            // Packet ?????? Thread ??????(RxThread ?????????, 2??? ??????)
            try {
                //          GlobalVariables.rxThreadOn = true
//            GlobalVariables.rxThread = RxThread()
//            GlobalVariables.rxThread!!.start()

                GlobalVariables.getPacketThreadOn = true
                GlobalVariables.getPacketThread = GetPacketThread(requireContext())
                GlobalVariables.getPacketThread!!.start()
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

            //Thread.sleep(100)
            //Packet.send(PacketKind.HwRead) // Send packet
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
                Log.d("[ADS] ", "[SVC] " + service.uuid.toString())

                for (char in service.characteristics) {
                    Log.d("[ADS] ", "  [CHAR]" + char.uuid.toString())

                    for (desc in char.descriptors) {
                        Log.d("[ADS] ", "    [DESC]" + desc.uuid.toString())
                    }
                }
            }

            val uuidService = UUID.fromString(UUID_SERVICE)
            val uuidRw = UUID.fromString(UUID_CHAR_RW_NOTIFY)

            GlobalVariables.btCh =
                GlobalVariables.bleGatt.getService(uuidService)?.getCharacteristic(uuidRw)!!

            //var result = gatt.setCharacteristicNotification(ch, true)
            //gatt.setCharacteristicNotification(GlobalVariables.btCh, true)
            GlobalVariables.bleGatt.setCharacteristicNotification(GlobalVariables.btCh, true)

            // log for successful discovery
            //Log.d("[ADS] ", result.toString())
            Log.d("[ADS] ", "Services discovery is successfully.")

            val descriptor: BluetoothGattDescriptor? = GlobalVariables.btCh?.getDescriptor(
                UUID.fromString(CLIENT_CHARACTERISTIC_CONFIG)
            )
            descriptor?.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            gatt!!.writeDescriptor(descriptor)
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic,
        ) {
            super.onCharacteristicChanged(gatt, characteristic)

            //activity?.runOnUiThread(Runnable {
                //Log.d("[ADS] ", "onCharacteristicChanged: ?????? ?????? ????????????")
                //Log.d("[ADS] ", "RX : " + characteristic.getStringValue(0))
//            GlobalVariables.rxRawBytesQueue.add(characteristic.getStringValue(0).toByteArray())
                //var str = characteristic.getStringValue(0)    // String Type ?????? ??????(toByteArray ???, Encoding ?????? ??????

//            var ba = characteristic.value   // byte[] type ?????? return
//            GlobalVariables.rxRawBytesQueue.add(ba)
            synchronized(GlobalVariables.rxRawBytesQueue) {
                GlobalVariables.rxRawBytesQueue.add(characteristic.value)
            }
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int,
        ) {
            super.onCharacteristicWrite(gatt, characteristic, status)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                //Log.d("[ADS] ", "Characteristic written successfully")
            } else {
                //Log.e("[ADS] ", "Characteristic write successfully, status: $status")
                disconnectGattServer()
            }
        }

//        override fun onCharacteristicRead(
//            gatt: BluetoothGatt?,
//            characteristic: BluetoothGattCharacteristic,
//            status: Int,
//        ) {
//            super.onCharacteristicRead(gatt, characteristic, status)
//            if (status == BluetoothGatt.GATT_SUCCESS) {
//                Log.d("[ADS] ", "Characteristic read successfully")
//                readCharacteristic(characteristic)
//            } else {
//                Log.e("[ADS] ", "Characteristic read unsuccessful, status: $status")
//                // Trying to read from the Time Characteristic? It doesnt have the property or permissions
//                // set to allow this. Normally this would be an error and you would want to:
//                // disconnectGattServer();
//            }
//        }
//
//        /**
//         * Log the value of the characteristic
//         * @param characteristic
//         */
//        private fun readCharacteristic(characteristic: BluetoothGattCharacteristic) {
//            val msg = characteristic.getStringValue(0)
//            Log.d("[ADS] ", "read: $msg")
//        }
    }

    /**
     * Disconnect Gatt Server
     */
    fun disconnectGattServer() {
        Log.d("[ADS] ", "Closing Gatt connection")
        // disconnect and close the gatt
        if (GlobalVariables.bleGatt != null) {
            GlobalVariables.bleGatt?.disconnect()
            GlobalVariables.bleGatt?.close()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQ_CODE_CONNECT_ACTIVITY){
            if (resultCode == RESULT_OK){
                val device = GlobalVariables.selectedDevice
                //bleGatt = device?.connectGatt(context, false, gattClientCallback)
                GlobalVariables.bleGatt = device?.connectGatt(context, false, gattClientCallback)

            }else if (resultCode == RESULT_CANCELED) {
                //binding.tvStatus.text = "Connection canceled."
            }
        }else if (requestCode == REQ_CODE_BT_EN) {
            if (resultCode == RESULT_OK){
                Toast.makeText(this@ConnectFragment.context, "Bluetooth ????????? ????????????????????????.", Toast.LENGTH_LONG)
                    .show()
                showConnectActivity()
            }
            else{
                var msg = "Bluetooth Enable ??? App ????????? ???????????????."
                Toast.makeText(this@ConnectFragment.context, msg, Toast.LENGTH_LONG)
                    .show()
            }
        }
    }

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
    // BT Disconnect ??????, Stream, Socket Close ??? Thread ??????
    //---------------------------------------------------------------------------------------//
    private fun disconnectBt() {
//        if ((GlobalVariables.hwStat and 0x06) == 0x06.toByte()) {
//            Packet.send(GlobalVariables.outStream, PacketKind.MonSet, 0x00)  // Stop All Monitoring
//            Log.d("[ADS]", "Monitoring stopped.")
//            Thread.sleep(10) // ok
//        }

        GlobalVariables.hwStat = 0x00
        //Packet.send(GlobalVariables.outStream, PacketKind.HwWrite, GlobalVariables.hwStat) // Send packet

//        if (GlobalVariables.inStream != null) GlobalVariables.inStream!!.close()
//        if (GlobalVariables.outStream != null) GlobalVariables.outStream!!.close()
//        if (GlobalVariables.socket != null) GlobalVariables.socket!!.close()

        GlobalVariables.rxThreadOn = false
        GlobalVariables.getPacketThreadOn = false

        //mmBinding?.tvStatus?.text = "Status : Disconnected"
        binding.tvStatus.text = "Status : Disconnected"

        GlobalVariables.rxRawBytesQueue.clear()
        GlobalVariables.isBtConnected = false
        GlobalVariables.hwStat = 0
    }

    //---------------------------------------------------------------------------------------//
    // btnConnect ??? OnClickListener
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
                // Bluetooth ??? Disable ?????? ?????? ?????? ??????????????? Enable ?????? ??????
                //-------------------------------------------------------------------------*/
                //  Bluetooth ????????? ??? ??????????????? ??????
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
    // btnDisconnect ??? OnClickListener
    //---------------------------------------------------------------------------------------//
    private val listenerDisconnect = View.OnClickListener {
        disconnectGattServer()
        disconnectBt()

        binding.tvDeviceName.text = "Device : "
        binding.tvMac.text = "MAC : "

        binding.btnConnect.isEnabled = true
        binding.btnDisconnect.isEnabled = false
    }

//    private val listenerSend = View.OnClickListener {
//
//        val uuidService = UUID.fromString(UUID_SERVICE)
//        val uuidRw = UUID.fromString(UUID_CHAR_RW_NOTIFY)
//        val ch = GlobalVariables.bleGatt.getService(uuidService).getCharacteristic(uuidRw)
//        var ba = ByteArray(8)
//
//        ba[0] = 0x02
//        ba[1] = 'H'.code.toByte()
//        ba[2] = 'R'.code.toByte()
//        ba[3] = '0'.code.toByte()
//        ba[4] = '0'.code.toByte()
//        ba[5] = '0'.code.toByte()
//        //ba[6] = 0x02
//        ba[6] = 0x00
//        ba[7] = 0x03
//
//        ch.setValue(ba)
//        GlobalVariables.bleGatt.writeCharacteristic(ch)
//    }
}