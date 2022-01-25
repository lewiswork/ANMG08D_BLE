package com.example.navdrawer.function

import android.util.Log
import com.example.navdrawer.Global
import com.example.navdrawer.PacketCategory
import com.example.navdrawer.PacketKind
import java.io.OutputStream
import java.lang.Exception

class Packet {
    companion object {

        const val STX : Byte = 0x02
        const val ETX : Byte = 0x03

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

        //--------------------------------------------------------------------------------------//
        // Make packet of 1-byte data
        // 1-byte 데이터 전송용 Packet 생성 함수
        //--------------------------------------------------------------------------------------//
        fun make(kind: PacketKind, list: ArrayList<Byte>, b: Byte) {
            list.clear()

            // Set Start of Packet(STX)
            list.add(STX)
            // Set Header
            setHeader(list, kind)
            // Set Size
            setSize(list, 1)
            // Set Data
            list.add(b)
            // Set checksum
            setChecksum(list, makeChecksum(b))
            // Set End of Packet(ETX)
            list.add(ETX)
        }
        //--------------------------------------------------------------------------------------//

        private fun setHeader(list: ArrayList<Byte>, kind: PacketKind) {
            val str : String = packetKind.entries.find{it.value == kind}!!.key
            val ba = str.toByteArray()
            if (str.length == 2) {
                list.add(ba[0])
                list.add(ba[1])
            }
            else{
                throw Exception("Error while setting packet header.")
            }
        }

        private fun setChecksum(list: ArrayList<Byte>, checksum: Byte) {
            list.add(checksum);
        }

        private fun setSize(list: ArrayList<Byte>, size: Int) {
            if (size < 256) {
                val str = String.format("%03d", size)
                val ca = str.toCharArray()

                for (c in ca) {
                    list.add(c.toByte())
                }
            } else {
                throw Exception("Data size error while making packet.")
            }
        }

        fun makeChecksum(data :Byte):Byte {
            var calcVal: UInt = 0u

            calcVal = data.toUInt().inv()
            calcVal = calcVal and 0x000000ff.toUInt()
            calcVal++

            return calcVal.toByte()
        }

        fun makeChecksum(buf :ByteArray):Byte {
            var calcVal: UInt = 0u

            for (data in buf) {
                calcVal += data.toUInt()
            }
            calcVal = calcVal.inv()
            calcVal = calcVal and 0x000000ff.toUInt()
            calcVal++

            return calcVal.toByte()
        }

        fun sendPacket(out: OutputStream?, list: ArrayList<Byte>) {
            val ba: ByteArray = list.toByteArray()
            Global.outStream!!.write(ba)
        }
    }
}

