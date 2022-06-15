package com.adsemicon.anmg08d.thread

import android.util.Log
import com.adsemicon.anmg08d.GlobalVariables

class RxThread : Thread() {

    override fun run() {
        var bytes: Int
        var arrayCopy: ByteArray
        var rxBuffer: ByteArray = ByteArray(2048)

        Log.d("[ADS] ", "Receive thread started. ID : ${this.id}")
        while (GlobalVariables.rxThreadOn) {
            try {
                if (GlobalVariables.socket!!.isConnected) {
                    // Receive
                    bytes = GlobalVariables.inStream!!.read(rxBuffer)

                    if (bytes > 0) {
                        synchronized(GlobalVariables.rxRawBytesQueue) {
                            arrayCopy = rxBuffer.copyOf(bytes)
                            GlobalVariables.rxRawBytesQueue.add(arrayCopy)
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