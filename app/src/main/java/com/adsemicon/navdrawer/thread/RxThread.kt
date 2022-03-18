package com.adsemicon.navdrawer.thread

import android.util.Log
import com.adsemicon.navdrawer.Global

class RxThread : Thread() {

    override fun run() {
        var bytes: Int
        var arrayCopy: ByteArray
        var rxBuffer: ByteArray = ByteArray(2048)

        Log.d("[ADS] ", "Receive thread started. ID : ${this.id}")
        while (com.adsemicon.navdrawer.Global.rxThreadOn) {
            try {
                if (com.adsemicon.navdrawer.Global.socket!!.isConnected) {
                    // Receive
                    bytes = com.adsemicon.navdrawer.Global.inStream!!.read(rxBuffer)

                    if (bytes > 0) {
                        //synchronized(this) {
                        synchronized(com.adsemicon.navdrawer.Global.rawRxBytesQueue) {
                            arrayCopy = rxBuffer.copyOf(bytes)
                            com.adsemicon.navdrawer.Global.rawRxBytesQueue.add(arrayCopy)
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