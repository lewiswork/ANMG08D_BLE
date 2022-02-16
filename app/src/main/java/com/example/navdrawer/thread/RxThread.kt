package com.example.navdrawer.thread

import android.util.Log
import com.example.navdrawer.Global

class RxThread : Thread() {

    override fun run() {
        var bytes: Int
        var arrayCopy: ByteArray
        var rxBuffer: ByteArray = ByteArray(2048)

        Log.d("[ADS] ", "Receive thread started. ID : ${this.id}")
        while (Global.rxThreadOn) {
            try {
                if (Global.socket!!.isConnected) {
                    // Receive
                    bytes = Global.inStream!!.read(rxBuffer)

                    if (bytes > 0) {
                        //synchronized(this) {
                        synchronized(Global.rawRxBytesQueue) {
                            arrayCopy = rxBuffer.copyOf(bytes)
                            Global.rawRxBytesQueue.add(arrayCopy)
                        }
                    }
                }
            } catch (e: java.io.IOException) {
                e.printStackTrace()
                break
            }
        }
        Log.d("[ADS] ", "Receive thread finished. ID : ${this.id}")
    }
}