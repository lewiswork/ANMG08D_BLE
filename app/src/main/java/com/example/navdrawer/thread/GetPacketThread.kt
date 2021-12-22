package com.example.navdrawer.thread

import android.util.Log
import com.example.navdrawer.GlobalVariables
import com.example.navdrawer.PacketClass
import com.example.navdrawer.PacketKind

enum class ExtractMode{ Header, Body }    // Header : STX~LEN, Body : Data~ETX

class GetPacketThread:Thread() {

    val STX : Byte = 0x02
    val ETX : Byte = 0x03

    val SZ_UNTIL_LEN = 6
    val IDX_DATA_START = 6

    var mmCurIdx : Int = 0
    var mmRawByteList = mutableListOf<Byte>()
    var mmDataList : ArrayList<Byte> = ArrayList()
    var mmExtractMode  = ExtractMode.Header

    override fun run() {
        super.run()

        var qEmpty = true

        var header : String = ""
        var dataLength : Int = 0
        //var dataList : ArrayList<Byte> = ArrayList<Byte>()
        var checksum : Byte = 0x00

        var packetClass : PacketClass? = null
        var packetKind : PacketKind? = null

        Log.d("ME", "Get packet thread started. ID : ${this.id}")

        while (GlobalVariables.rxPacketThreadOn) {
            try {
                //------------------------------------------------------------------------------//
                // rawByteQueue 데이터 -> byteList 로 이동
                //------------------------------------------------------------------------------//
                synchronized(this) { qEmpty = GlobalVariables.rawByteQueue.isEmpty() }
                if (!qEmpty) {
                    try {
                        synchronized(this) {
                            var len = GlobalVariables.rawByteQueue.count()

                            for (i in 0 until len) {
                                mmRawByteList.add(GlobalVariables.rawByteQueue.remove())
                            }
                            Log.d("ME LEN", len.toString())
                        }
                    } catch (ex: NoSuchElementException) {
                        Log.d("MEX", GlobalVariables.rawByteQueue.count().toString())
                        ex.printStackTrace()
                        //continue
                        break
                    }
                }
                //------------------------------------------------------------------------------//

                //------------------------------------------------------------------------------//
                // byteList 데이터 -> Packet 추출
                //------------------------------------------------------------------------------//
                    if (mmRawByteList.count() > 0) {

                        if (mmExtractMode == ExtractMode.Header) {
                            if (mmRawByteList.count() < SZ_UNTIL_LEN) continue

                            // STX
                            mmCurIdx++
                            if (mmRawByteList[0] != STX) {
                                Log.d("ME", "STX Error ! : ${mmRawByteList[0]}")
                                if (clearRawByteList()) continue    // Error 처리
                            }

                            // Check Header : if in range of A to Z
                            mmCurIdx+=2
                            if ((mmRawByteList[1] in 0x41..0x5a) && (mmRawByteList[2] in 0x41..0x5a)) {
                                Log.d("ME",
                                    "Packet Header : ${mmRawByteList[1].toChar()}${mmRawByteList[2].toChar()}")

                                val str1 = mmRawByteList[1].toChar().toString()
                                val str2 = mmRawByteList[2].toChar().toString()

                                packetClass = GlobalVariables.packetClass[str1]
                                packetKind = GlobalVariables.packetKind["$str1$str2"]

                                if (packetClass == null || packetKind == null){
                                    Log.d("ME", "Packet header error : $str1$str2")
                                    if (clearRawByteList()) continue    // Error 처리
                                }
                                Log.d("ME", "Packet Class : $packetClass")
                                Log.d("ME", "Packet Kind : $packetKind")

                            } else {
                                Log.d("ME", "Packet Header Error!!")
                                if (clearRawByteList()) continue    // Error 처리
                            }

                            // Check Length : if in range of '0' to '9'
                            mmCurIdx+=3
                            if ((mmRawByteList[3] in 0x30..0x39) && (mmRawByteList[4] in 0x30..0x39)
                                && (mmRawByteList[5] in 0x30..0x39)
                            ) {
                                dataLength =
                                    (mmRawByteList[3].toInt() - 0x30) * 100 + (mmRawByteList[4].toInt() - 0x30) * 10 + (mmRawByteList[5].toInt() - 0x30)
                                Log.d("ME", "Packet Length(Int) : $dataLength")
                                mmExtractMode = ExtractMode.Body
                            } else {
                                Log.d("ME", "Packet Length Error!!")
                                if (clearRawByteList()) continue    // Error 처리
                            }
                        } else if (mmExtractMode == ExtractMode.Body) {

                            if (mmRawByteList.count() < SZ_UNTIL_LEN + dataLength + 2) continue // Data, Checksum, ETX

                            // Data
                            mmCurIdx += dataLength
                            for (i in 0 until dataLength) {
                                mmDataList.add(mmRawByteList[IDX_DATA_START + i])
                            }

                            // Checksum(Valid Check)
                            mmCurIdx++
                            checksum = mmRawByteList[IDX_DATA_START + dataLength]

                            // Checksum Error 확인

                            // ETX
                            mmCurIdx++
                            if (mmRawByteList[IDX_DATA_START + dataLength + 1] != ETX) {
                                Log.d("ME", "ETX Error !")
                                if (clearRawByteList()) continue    // Error 처리
                            }
                            Log.d("ME", "Getting packet succeeded.")
                            if (clearRawByteList()) continue
                        }
                        //------------------------------------------------------------------------------//


//                        if (sb.contains('S')) {
//                            sidx = sb.indexOf('S')
//                        }
//
//                        if (sb.contains('Z')) {
//                            eidx = sb.indexOf('Z')
//                            pk = sb.substring(sidx, eidx + 1)
//
//                            if (sb.length > pk.length) {
//                                sidx = eidx + 1
//                                sb = StringBuilder(sb.substring(sidx, sb.length))
//                            } else {
//                                sb = StringBuilder("")
//                            }
//                            Log.d("MED", pk)
//                        } else {
//                            break
//                        }

//                        activity?.runOnUiThread {
//                            _binding?.tvMonitoring?.text = pk
//                        }
                    }

            } catch (e: java.io.IOException) {
                Log.d("MEX", e.message.toString())
                e.printStackTrace()
                break
                //continue
            }
        }
        Log.d("ME", "Get packet thread finished. ID : ${this.id}")
    }

    private fun clearRawByteList() : Boolean {
        for (i in 0 until mmCurIdx) mmRawByteList.removeAt(i)
        mmCurIdx = 0
        mmDataList.clear()
        mmExtractMode = ExtractMode.Header
        return true
    }
}