package com.example.navdrawer.thread

import android.util.Log
import com.example.navdrawer.Global

class RxThread : Thread(){

    private var mmTxBuffer: ByteArray = ByteArray(2048)
    private var mmRxBuffer: ByteArray = ByteArray(2048)

    override fun run() {

        var bytes : Int
        var readMessage : String
        var arrayCopy = byteArrayOf()

        Log.d("ME", "Receive thread started. ID : ${this.id}")
        while (Global.rxThreadOn) {
            try {
                if (Global.socket!!.isConnected) {
                    // Receive
                    bytes = Global.inStream!!.read(mmRxBuffer)

                    if (bytes > 0) {
                        //readMessage = kotlin.text.String(mmRxBuffer, 0, bytes)
                        synchronized(this) {
                            //for (i in 0 until bytes) {
                                //Global.rawByteQueue.add(mmRxBuffer[i])
                                //Log.d("ME", mmRxBuffer[i].toString())
                            //}
                            arrayCopy = mmRxBuffer.copyOf(bytes)
                            Global.rawByteQueue.add(arrayCopy)
                        }
                    }
                }
            } catch (e: java.io.IOException) {
                e.printStackTrace()
                break
            }
        }
        Log.d("ME", "Receive thread finished. ID : ${this.id}")
    }
}