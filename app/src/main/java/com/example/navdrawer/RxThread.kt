package com.example.navdrawer

import android.util.Log

class RxThread : Thread(){
    override fun run() {
        super.run()

        var i=0;

        Log.d("ME", "Receive thread started. ID : ${this.id}")
        while (GlobalVariables.rxThreadOn) {
        //while (true) {
            Thread.sleep(500)
            Log.d("Test", i++.toString())
        }
        Log.d("ME", "Receive thread finished. ID : ${this.id}")
    }
}