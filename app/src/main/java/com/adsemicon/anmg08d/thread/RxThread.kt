package com.adsemicon.anmg08d.thread

import android.util.Log

class RxThread : Thread() {

    override fun run() {
        var bytes: Int
        var arrayCopy: ByteArray
        var rxBuffer: ByteArray = ByteArray(2048)

        Log.d("[ADS] ", "Receive thread started. ID : ${this.id}")
        while (com.adsemicon.anmg08d.Global.rxThreadOn) {
            try {
                if (com.adsemicon.anmg08d.Global.socket!!.isConnected) {
                    // Receive
                    bytes = com.adsemicon.anmg08d.Global.inStream!!.read(rxBuffer)

                    if (bytes > 0) {
                        //synchronized(this) {
                        synchronized(com.adsemicon.anmg08d.Global.rxRawBytesQueue) {
                            arrayCopy = rxBuffer.copyOf(bytes)
                            com.adsemicon.anmg08d.Global.rxRawBytesQueue.add(arrayCopy)
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