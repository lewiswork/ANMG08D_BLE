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

            "MS" to PacketKind.MonSet,
            "MT" to PacketKind.MonTouch,
            "MP" to PacketKind.MonPercent
        )

        private val listTxPacket: ArrayList<Byte> = ArrayList()

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
                    listTxPacket.add(c.code.toByte())
                }
            } else {
                throw Exception("Data size error while making packet.")
            }
        }

//        fun makeChecksum(data: Byte): Byte {
//            var calcVal: UInt = 0u
//            calcVal = data.toUInt().inv()
//            calcVal = calcVal and 0x000000ff.toUInt()
//            calcVal++
//            return calcVal.toByte()
//        }

        fun makeChecksum(b: Byte): Byte {
            var calcVal = b.toUInt().inv()
            calcVal = calcVal and 0x000000ff.toUInt()
            calcVal++
            return calcVal.toByte()
        }

        fun makeChecksum(ba: ByteArray): Byte {
            var calcVal: UInt = 0u

            for (b in ba) {
                calcVal += b.toUInt()
            }
            calcVal = calcVal.inv()
            calcVal = calcVal and 0x000000ff.toUInt()
            calcVal++

            return calcVal.toByte()
        }

        fun makeChecksum(ba: ByteArray, length:Int): Byte {
            var i:Int
            var calcVal: UInt = 0u

            for (i in 0 until length) {
                calcVal += ba[i].toUInt()
            }
            calcVal = calcVal.inv()
            calcVal = calcVal and 0x000000ff.toUInt()
            calcVal++

            return calcVal.toByte()
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

            var str = ""
            for (b in ba)
            {
                str += String.format("%02X", b) + " "
            }
            Log.d("[ADS] ", "Packet sent : $str")
        }

        //--------------------------------------------------------------------------------------//
        // 1-byte 데이터 Packet 생성/전송
        //--------------------------------------------------------------------------------------//
        fun send(os: OutputStream?, kind: PacketKind, b: Byte) {
            listTxPacket.clear()

            // make packet
            listTxPacket.add(STX)           // Set Start of Packet(STX)
            setHeader(kind)                 // Set Header
            setSize(1)                      // Set Size
            listTxPacket.add(b)             // Set Data
            setChecksum(makeChecksum(b))    // Set checksum
            listTxPacket.add(ETX)           // Set End of Packet(ETX)

            // send packet
            val ba: ByteArray = listTxPacket.toByteArray()
            os!!.write(ba)

            // log
            var str = ""
            for (item in ba) str += String.format("%02X", item) + " "
            Log.d("[ADS] ", "Packet sent : $str")
        }

        //--------------------------------------------------------------------------------------//
        // 2-byte 이상 데이터 Packet 생성/전송, Data Length 미지정(Byte Array Size = Data Length 인 경우)
        //--------------------------------------------------------------------------------------//
        fun send(os: OutputStream?, kind: PacketKind, ba: ByteArray) {
            listTxPacket.clear()

            // Set Start of Packet(STX)
            listTxPacket.add(STX)

            // Set Header
            setHeader(kind)

            // Set Size
            setSize(ba.size)

            // Set Data
            for (b in ba)
                listTxPacket.add(b)

            // Set checksum
            setChecksum(makeChecksum(ba))

            // Set End of Packet(ETX)
            listTxPacket.add(ETX)

            val ba: ByteArray = listTxPacket.toByteArray()
            os!!.write(ba)

            var str = ""
            for (item in ba) str += String.format("%02X", item) + " "
            Log.d("[ADS] ", "Packet sent : $str")
        }

        //--------------------------------------------------------------------------------------//
        // 2-byte 이상 데이터 Packet 생성/전송, Data Length 지정(Byte Array Size != Data Length 인 경우)
        //--------------------------------------------------------------------------------------//
        fun send(os: OutputStream?, kind: PacketKind, ba: ByteArray, dataLength:Int) {
            var i:Int

            listTxPacket.clear()

            // Set Start of Packet(STX)
            listTxPacket.add(STX)

            // Set Header
            setHeader(kind)

            // Set Size
            setSize(dataLength)

            // Set Data
            for (i in 0 until dataLength)
                listTxPacket.add(ba[i])

            // Set checksum
            setChecksum(makeChecksum(ba, dataLength))

            // Set End of Packet(ETX)
            listTxPacket.add(ETX)

            val ba: ByteArray = listTxPacket.toByteArray()
            os!!.write(ba)

            var str = ""
            for (item in ba) str += String.format("%02X", item) + " "
            Log.d("[ADS] ", "Packet sent : $str")
        }
    }
}

