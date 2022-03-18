package com.adsemicon.navdrawer.adlib

import android.util.Log

open class ADThread  {
    private var isRunning : Boolean = false
    var threadId : Int = 0
        private set
    private lateinit var thread : RunThread

    inner class RunThread : Thread() {
        override fun run() {
            while(isRunning) {
                doWork()
            }
        }
    }

    init {
        thread = RunThread()
        isRunning = true
        threadId = ThreadPool.add(this)
        thread.start()
    }

    open fun doWork() {

    }

    fun stop() {
        isRunning = false
        Log.d("DBG", "[${this.javaClass.name}] ThreadID : ${threadId} stop")
    }
}