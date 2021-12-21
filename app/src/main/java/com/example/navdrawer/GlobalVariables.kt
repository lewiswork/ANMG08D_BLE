package com.example.navdrawer

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import com.example.navdrawer.thread.GetPacketThread
import com.example.navdrawer.thread.RxThread
import java.io.InputStream
import java.io.OutputStream
import java.util.*

class GlobalVariables {
    // companion object : 타 언어의 Static Class 와 같이 사용하기 위한 목적
    companion object {
        public lateinit var adapter: BluetoothAdapter       // Late Initialize : 변수 초기화를 나중으로 미룸
        public lateinit var selectedDevice: BluetoothDevice
        //public var rByteQueue: Queue<String> = LinkedList()
        public var rawByteQueue: Queue<Byte> = LinkedList()
        public var isBtConnected: Boolean = false           // BT 연결 상태

        public var inStream: InputStream? = null
        public var outStream: OutputStream? = null
        public var socket: BluetoothSocket? = null

        public var rxThreadOn = false
        public var rxPacketThreadOn = false
        //public var displayThreadOn = false

        public  var rxThread: RxThread?  = null
        public  var getPacketThread: GetPacketThread?  = null
    }
}