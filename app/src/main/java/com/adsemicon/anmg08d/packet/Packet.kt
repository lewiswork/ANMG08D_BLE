package com.adsemicon.anmg08d.packet

import android.util.Log
import com.adsemicon.anmg08d.function.log.PacketType
import java.io.OutputStream
import java.lang.Exception
import java.lang.StringBuilder

class Packet {
    companion object {

        const val STX: Byte = 0x02
        const val ETX: Byte = 0x03
        const val IDX_DATA_START : Int = 6



        val packetCategory = mapOf(
            "E" to com.adsemicon.anmg08d.PacketCategory.Rom,
            "M" to com.adsemicon.anmg08d.PacketCategory.Monitoring,
            "R" to com.adsemicon.anmg08d.PacketCategory.Register,
            "H" to com.adsemicon.anmg08d.PacketCategory.Hardware,
            "T" to com.adsemicon.anmg08d.PacketCategory.Test
        )

        val packetKind = mapOf(
            "HW" to com.adsemicon.anmg08d.PacketKind.HwWrite,
            "HR" to com.adsemicon.anmg08d.PacketKind.HwRead,

            "MS" to com.adsemicon.anmg08d.PacketKind.MonSet,
            "MT" to com.adsemicon.anmg08d.PacketKind.MonTouch,
            "MP" to com.adsemicon.anmg08d.PacketKind.MonPercent,

            "RX" to com.adsemicon.anmg08d.PacketKind.RegSingleRead,
            "RY" to com.adsemicon.anmg08d.PacketKind.RegSingleWrite,
            "RI" to com.adsemicon.anmg08d.PacketKind.RegSwReset
        )

        private val listTxPacket: ArrayList<Byte> = ArrayList()

        private fun setHeader(kind: com.adsemicon.anmg08d.PacketKind) {
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
        fun send(os: OutputStream?, kind: com.adsemicon.anmg08d.PacketKind) {
            listTxPacket.clear()

            listTxPacket.add(STX)   // Set Start of Packet(STX)
            setHeader(kind) // Set Header
            setSize(0)  // Set Size as 0
            listTxPacket.add(0) // Set checksum as 0
            listTxPacket.add(ETX)   // Set End of Packet(ETX)

            val ba: ByteArray = listTxPacket.toByteArray()
            os!!.write(ba)

            logTxPacket(ba)
        }

        //--------------------------------------------------------------------------------------//
        // 1-byte 데이터 Packet 생성/전송
        //--------------------------------------------------------------------------------------//
        fun send(os: OutputStream?, kind: com.adsemicon.anmg08d.PacketKind, b: Byte) {
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

            logTxPacket(ba)
        }

        //--------------------------------------------------------------------------------------//
        // 2-byte 이상 데이터 Packet 생성/전송, Data Length 미지정(Byte Array Size = Data Length 인 경우)
        //--------------------------------------------------------------------------------------//
        fun send(os: OutputStream?, kind: com.adsemicon.anmg08d.PacketKind, ba: ByteArray) {
            listTxPacket.clear()

            listTxPacket.add(STX)               // Set Start of Packet(STX)
            setHeader(kind)                 // Set Header
            setSize(ba.size)    // Set Size
            for (b in ba) listTxPacket.add(b)    // Set Data
            setChecksum(makeChecksum(ba))   // Set checksum
            listTxPacket.add(ETX)   // Set End of Packet(ETX)

            val ba: ByteArray = listTxPacket.toByteArray()
            os!!.write(ba)

            logTxPacket(ba)
        }

        //--------------------------------------------------------------------------------------//
        // 2-byte 이상 데이터 Packet 생성/전송, Data Length 지정(Byte Array Size != Data Length 인 경우)
        //--------------------------------------------------------------------------------------//
        fun send(os: OutputStream?, kind: com.adsemicon.anmg08d.PacketKind, ba: ByteArray, dataLength:Int) {
            var i: Int
            listTxPacket.clear()

            listTxPacket.add(STX)   // Set Start of Packet(STX)
            setHeader(kind) // Set Header
            setSize(dataLength) // Set Size
            for (i in 0 until dataLength) listTxPacket.add(ba[i])    // Set Data
            setChecksum(makeChecksum(ba, dataLength))   // Set checksum
            listTxPacket.add(ETX)   // Set End of Packet(ETX)

            val ba: ByteArray = listTxPacket.toByteArray()
            os!!.write(ba)

            logTxPacket(ba)
        }

        private fun logTxPacket(ba: ByteArray) {
            var logStr: StringBuilder = StringBuilder("")

            for (i in ba.indices) {
                if (i == 0) {
                    // STX
                    logStr.append("STX ")
                } else if (i in 1 until IDX_DATA_START) {
                    // Header, Length
                    if (i == 3) logStr.append(" ")
                    logStr.append(ba[i].toChar())
                } else if (i >= IDX_DATA_START && i < ba.size - 1) {
                    logStr.append(" ")
                    // Data, Checksum
                    logStr.append(String.format("%02X", ba[i]))
                } else if (i == ba.size - 1) {
                    // ETX
                    logStr.append(" ETX")
                }
            }

            com.adsemicon.anmg08d.GlobalVariables.packetLog.printPacket(PacketType.TX, logStr.toString())  // 임시, 향 후 Library 적용 예정
            Log.d("[ADS] ", "[PK TX] $logStr")
        }
    }
}
