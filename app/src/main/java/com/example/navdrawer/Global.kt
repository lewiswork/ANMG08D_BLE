package com.example.navdrawer

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.util.Log
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
        var rawRxBytesQueue: Queue<ByteArray> = LinkedList()
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


        //fun verifyChecksum(buf:ArrayList<Byte>, checksum:Byte):Boolean {
        fun verifyChecksum(buf:ByteArray, checksum:Byte):Boolean {
            var calcVal: UInt = 0u
            var result = false

            for (data in buf) {
                calcVal += data.toUInt()
            }
            calcVal = calcVal.inv()
            calcVal = calcVal and 0x000000ff.toUInt()
            calcVal++

            //------------------------------------------------------------------------//
            // Kotlin 에서 Byte 는 Signed(Unsigned 는 UByte)
            // UInt Type 인 calcVal 을 Signed Type 으로 변환(.toByte)
            // Signed Type 으로 변환된 계산값과 Signed Type Checksum 수신값 비교
            //------------------------------------------------------------------------//
            result = calcVal.toByte() == checksum

            // 두 변수를 모두 Unsigned Type 으로 변환 후 비교하여도 결과 동일
            //result = calcVal.toUByte() == checksum.toUByte()
            //------------------------------------------------------------------------//

            if (!result){
                Log.d("ME", "Checksum fail / Rx Val : ${checksum},Calc Val : ${calcVal.toByte()}")
            }

            return result
        }

        fun byteToBooleanArray(input :Byte, size:Int):BooleanArray {
            var arr = BooleanArray(size)
            var mask: Int = 1

            val value = input.toInt()

            for (i in arr.indices) {
                arr[i] = (value and mask) > 0
                mask = mask shl 1
            }
            return arr
        }
    }
}