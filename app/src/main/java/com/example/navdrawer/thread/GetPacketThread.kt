package com.example.navdrawer.thread

import android.util.Log
import com.example.navdrawer.GlobalVariables

class GetPacketThread:Thread() {

    val STX = 0x02
    val ETX = 0x03

    override fun run() {
        super.run()

        var pk: String = ""
        var sb: StringBuilder = StringBuilder()
        var sidx: Int = 0
        var eidx: Int = 0
        var qEmpty: Boolean = true
        //var qCount: Int = -1

        var byteList = mutableListOf<Byte>()
        var qList = mutableListOf<Byte>()

        var dataLength = 0

        Log.d("ME", "Get packet thread started. ID : ${this.id}")

        while (GlobalVariables.rxPacketThreadOn) {
            try {

                synchronized(this) {
                    qEmpty = GlobalVariables.rawByteQueue.isEmpty()
                }

                if (!qEmpty) {

                    try {
                        synchronized(this) {
                            var len = GlobalVariables.rawByteQueue.count()
                            for (i in 0 until len) {
                                //sb.append(GlobalVariables.rawByteQueue.remove())
                                byteList.add(GlobalVariables.rawByteQueue.remove())
                            }
                            Log.d("ME LEN", len.toString())
                        }

//                        for (i in 0 until  byteList.count()) {
//                            //sb.append(GlobalVariables.rawByteQueue.remove())
//                            Log.d("MEA", byteList[i].toString())
//                        }

                    } catch (ex: NoSuchElementException) {
                        Log.d("MEX", GlobalVariables.rawByteQueue.count().toString())
                        ex.printStackTrace()
                        //continue
                        break
                    }

                    if (byteList.count() > 0) {

                        if (byteList.count() < 6) continue

//                        for (i in 0 until byteList.count()) {
//                            if (byteList[i].equals(STX)) sidx = i
//                            Log.d("ME", "Start idx is ${i.toString()}")
//                        }

                        // Check Header : if in range of A to Z
                        if ((byteList[1] in 0x41..0x5a)  && (byteList[2] in 0x41..0x5a)) {
                            Log.d("ME",
                                "Packet Header : ${byteList[1].toChar()}${byteList[2].toChar()}")
                        }
                        else {
                            Log.d("ME", "Packet Header Error!!")
                            // Error 처리(byteList Clear)
                            continue
                        }

                        // Check Length : if in range of '0' to '9'
                        if ((byteList[3] in 0x30..0x39) && (byteList[4] in 0x30..0x39)
                            && (byteList[5] in 0x30..0x39)) {
                            Log.d("ME",
                                "Packet Length(Raw) : ${byteList[3].toChar()}${byteList[4].toChar()}${byteList[5].toChar()}")
                            dataLength =
                                (byteList[3].toInt() - 0x30) * 100 + (byteList[4].toInt() - 0x30) * 10 + (byteList[5].toInt() - 0x30)
                            Log.d("ME", "Packet Length(Int) : ${dataLength.toString()}")
                        }
                        else {
                            Log.d("ME", "Packet Length Error!!")
                            // Error 처리(byteList Clear)
                            continue
                        }


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
                }
            } catch (e: java.io.IOException) {
                Log.d("MEX", "$sidx/$eidx")
                e.printStackTrace()
                break
                //continue
            }
        }
        Log.d("ME", "Get packet thread finished. ID : ${this.id}")
    }
}