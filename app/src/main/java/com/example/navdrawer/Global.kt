package com.example.navdrawer

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import com.example.navdrawer.data.Monitoring
import com.example.navdrawer.data.Packet
import com.example.navdrawer.thread.GetPacketThread
import com.example.navdrawer.thread.RxThread
import java.io.InputStream
import java.io.OutputStream
import java.util.*

enum class PacketCategory{ Rom, Monitoring, Register, Hardware, Test }
enum class PacketKind{
    MonTouch, MonPercent
}

class Global {
    // companion object : 타 언어의 Static Class 와 같이 사용하기 위한 목적
    companion object {
        // Bluetooth 관련
        lateinit var adapter: BluetoothAdapter       // Late Initialize : 변수 초기화를 나중으로 미룸
        lateinit var selectedDevice: BluetoothDevice
        var rawByteQueue: Queue<ByteArray> = LinkedList()
        var isBtConnected: Boolean = false           // BT 연결 상태

        var inStream: InputStream? = null
        var outStream: OutputStream? = null
        var socket: BluetoothSocket? = null

        var rxThreadOn = false
        var rxPacketThreadOn = false

        var rxThread: RxThread? = null
        var getPacketThread: GetPacketThread? = null

        var romQueue : Queue<Packet> = LinkedList()
        var monQueue : Queue<Packet> = LinkedList()
        var hwQueue : Queue<Packet> = LinkedList()
        var regQueue : Queue<Packet> = LinkedList()
        var testQueue : Queue<Packet> = LinkedList()

        val packetCategory = mapOf(
            "E" to PacketCategory.Rom,
            "M" to PacketCategory.Monitoring,
            "R" to PacketCategory.Register,
            "H" to PacketCategory.Hardware,
            "T" to PacketCategory.Test
        )

        val packetKind = mapOf(
            "MT" to PacketKind.MonTouch,
            "MP" to PacketKind.MonPercent
        )

        val monitoring = Monitoring()



        fun verifyChecksum(buf:ArrayList<Byte>, checksum:Byte):Boolean {
            var result: UInt = 0u

            for (data in buf) {
                result += data.toUInt()
            }
            result = result.inv()
            result = result and 0x000000ff.toUInt()
            result++

            return result.toByte() == checksum
        }
    }
}