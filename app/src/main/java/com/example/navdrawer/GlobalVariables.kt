package com.example.navdrawer

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import java.util.*

class GlobalVariables {
    // companion object : 타 언어의 Static Class 와 같이 사용하기 위한 목적
    companion object {
        public lateinit var adapter: BluetoothAdapter       // Late Initialize : 변수 초기화를 나중으로 미룸
        public lateinit var selectedDevice: BluetoothDevice
        public  var sampleQueue: Queue<String> = LinkedList()
    }
}