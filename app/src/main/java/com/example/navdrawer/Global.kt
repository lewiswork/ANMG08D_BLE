package com.example.navdrawer

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import com.example.navdrawer.monitor.Monitoring
import com.example.navdrawer.packet.RPacket
import com.example.navdrawer.register.RegisterController
import com.example.navdrawer.thread.GetPacketThread
import com.example.navdrawer.thread.RxThread
import java.io.InputStream
import java.io.OutputStream
import java.util.*

enum class PacketCategory{ Rom, Monitoring, Register, Hardware, Test }
enum class PacketKind{
    HwRead, HwWrite,
    MonSet, MonTouch, MonPercent,
    RegSingleRead, RegSingleWrite
}

class Global {
    // companion object : 타 언어의 Static Class 와 같이 사용하기 위한 목적
    companion object {
        val monitoring = Monitoring()

        // Bluetooth 관련
        lateinit var adapter: BluetoothAdapter       // Late Initialize : 변수 초기화를 나중으로 미룸
        lateinit var selectedDevice: BluetoothDevice
        var rawRxBytesQueue: Queue<ByteArray> = LinkedList()
        var isBtConnected: Boolean = false           // BT 연결 상태

        var inStream: InputStream? = null
        var outStream: OutputStream? = null
        var socket: BluetoothSocket? = null

        var rxThreadOn = false
        var rxPacketThreadOn = false

        var rxThread: RxThread? = null
        var getPacketThread: GetPacketThread? = null

        var romQueue: Queue<RPacket> = LinkedList()
        var monQueue: Queue<RPacket> = LinkedList()
        var hwQueue: Queue<RPacket> = LinkedList()
        var regQueue: Queue<RPacket> = LinkedList()
        var testQueue: Queue<RPacket> = LinkedList()

        var hwStat: Byte = 0x00
        var hwStatPrev: Byte = 0x00
        var waitForStopMon: Boolean = false

        var regCon: RegisterController = RegisterController()
    }
}