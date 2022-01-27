package com.example.navdrawer.function

import android.util.Log
import com.example.navdrawer.Global
import com.example.navdrawer.PacketCategory
import com.example.navdrawer.PacketKind
import java.io.OutputStream
import java.lang.Exception

class Packet {
    companion object {

        const val STX: Byte = 0x02
        const val ETX: Byte = 0x03

        val packetCategory = mapOf(
            "E" to PacketCategory.Rom,
            "M" to PacketCategory.Monitoring,
            "R" to PacketCategory.Register,
            "H" to PacketCategory.Hardware,
            "T" to PacketCategory.Test
        )

        val packetKind = mapOf(
            "HW" to PacketKind.HwWrite,
            "HR" to PacketKind.HwRead,

            "MT" to PacketKind.MonTouch,
            "MP" to PacketKind.MonPercent
        )

        private val listTxPacket: ArrayList<Byte> = ArrayList()

//        //--------------------------------------------------------------------------------------//
//        // Make packet of 1-byte data
//        // 1-byte 데이터 전송용 Packet 생성 함수
//        //--------------------------------------------------------------------------------------//
//        fun make(kind: PacketKind) {
//            listTxPacket.clear()
//
//            // Set Start of Packet(STX)
//            listTxPacket.add(STX)
//            // Set Header
//            setHeader(kind)
//            // Set Size as 0
//            setSize(0)
//            // Set checksum as 0
//            listTxPacket.add(0)
//            // Set End of Packet(ETX)
//            listTxPacket.add(ETX)
//        }
//        //--------------------------------------------------------------------------------------//
//
//        //--------------------------------------------------------------------------------------//
//        // Make packet with 1-byte data
//        // 1-byte 데이터 전송용 Packet 생성
//        //--------------------------------------------------------------------------------------//
//        fun make(kind: PacketKind, b: Byte) {
//            listTxPacket.clear()
//
//            // Set Start of Packet(STX)
//            listTxPacket.add(STX)
//            // Set Header
//            setHeader(kind)
//            // Set Size
//            setSize(1)
//            // Set Data
//            listTxPacket.add(b)
//            // Set checksum
//            setChecksum(makeChecksum(b))
//            // Set End of Packet(ETX)
//            listTxPacket.add(ETX)
//        }
//        //--------------------------------------------------------------------------------------//

        private fun setHeader(kind: PacketKind) {
            val str: String = packetKind.entries.find { it.value == kind }!!.key
            val ba = str.toByteArray()
            if (str.length == 2) {
                listTxPacket.add(ba[0])
                listTxPacket.add(ba[1])
            } else {
                throw Exception("Error while setting packet header.")
            }
        }

        private fun setChecksum(checksum: Byte) {
            listTxPacket.add(checksum);
        }

        private fun setSize(size: Int) {
            if (size < 256) {
                val str = String.format("%03d", size)
                val ca = str.toCharArray()

                for (c in ca) {
                    listTxPacket.add(c.toByte())
                }
            } else {
                throw Exception("Data size error while making packet.")
            }
        }

        fun makeChecksum(data: Byte): Byte {
            var calcVal: UInt = 0u

            calcVal = data.toUInt().inv()
            calcVal = calcVal and 0x000000ff.toUInt()
            calcVal++

            return calcVal.toByte()
        }

        fun makeChecksum(buf: ByteArray): Byte {
            var calcVal: UInt = 0u

            for (data in buf) {
                calcVal += data.toUInt()
            }
            calcVal = calcVal.inv()
            calcVal = calcVal and 0x000000ff.toUInt()
            calcVal++

            return calcVal.toByte()
        }

        fun sendPacket(out: OutputStream?) {
            val ba: ByteArray = listTxPacket.toByteArray()
            Global.outStream!!.write(ba)
        }

        //--------------------------------------------------------------------------------------//
        // Make packet without data
        // 데이터 없는 Packet 생성/전송
        //--------------------------------------------------------------------------------------//
        fun send(os: OutputStream?, kind: PacketKind) {
            listTxPacket.clear()

            // Set Start of Packet(STX)
            listTxPacket.add(STX)
            // Set Header
            setHeader(kind)
            // Set Size as 0
            setSize(0)
            // Set checksum as 0
            listTxPacket.add(0)
            // Set End of Packet(ETX)
            listTxPacket.add(ETX)

            val ba: ByteArray = listTxPacket.toByteArray()
            os!!.write(ba)

//            var str = ""
//            for (b in ba)
//            {
//                str += String.format("%02X", b) + " "
//            }
//            Log.d("[ADS] ", "Packet sent : $str")
        }

        //--------------------------------------------------------------------------------------//
        // Make packet with 1-byte data
        // 1-byte 데이터 Packet 생성/전송
        //--------------------------------------------------------------------------------------//
        fun send(os: OutputStream?, kind: PacketKind, b: Byte) {
            listTxPacket.clear()

            // Set Start of Packet(STX)
            listTxPacket.add(STX)
            // Set Header
            setHeader(kind)
            // Set Size
            setSize(1)
            // Set Data
            listTxPacket.add(b)
            // Set checksum
            setChecksum(makeChecksum(b))
            // Set End of Packet(ETX)
            listTxPacket.add(ETX)

            val ba: ByteArray = listTxPacket.toByteArray()
            os!!.write(ba)

//            var str = ""
//            for (b in ba) str += String.format("%02X", b) + " "
//            Log.d("[ADS] ", "Packet sent : $str")
        }
    }
}

