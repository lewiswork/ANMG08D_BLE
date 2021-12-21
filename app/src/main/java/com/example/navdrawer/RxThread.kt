package com.example.navdrawer

import android.util.Log

class RxThread : Thread(){

    private var mmTxBuffer: ByteArray = ByteArray(2048)
    //private var mmRxBuffer: ByteArray = ByteArray(8192)
    private var mmRxBuffer: ByteArray = ByteArray(2048)

//    override fun run() {
//        super.run()
//
//        var i=0;
//
//        Log.d("ME", "Receive thread started. ID : ${this.id}")
//        while (GlobalVariables.rxThreadOn) {
//        //while (true) {
//            Thread.sleep(500)
//            Log.d("Test", i++.toString())
//        }
//        Log.d("ME", "Receive thread finished. ID : ${this.id}")
//    }

    override fun run() {

        var sidx:Int=0
        var eidx:Int=0
        var str:String=""
        var pk:String=""
        var bytes : Int
        var readMessage : String

        Log.d("ME", "Receive thread started. ID : ${this.id}")
        while (GlobalVariables.rxThreadOn) {
            try {
                //Log.d("MEA", "Receive Thread")
                if (GlobalVariables.socket!!.isConnected) {
                    // Receive
                    bytes = GlobalVariables.inStream!!.read(mmRxBuffer)

                    if (bytes > 0) {
                        readMessage = kotlin.text.String(mmRxBuffer, 0, bytes)
                        synchronized(this) {
                            GlobalVariables.rStringQueue.add(readMessage)
                            Log.d("ME", "${readMessage}")
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