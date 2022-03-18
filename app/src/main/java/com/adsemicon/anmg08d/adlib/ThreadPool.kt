package com.adsemicon.anmg08d.adlib

import android.util.Log

class ThreadPool {
    companion object {
        var threadID : Int = 0
        var pool : MutableMap<Int, ADThread> = mutableMapOf()

        public fun add(td : ADThread) : Int {
            pool[++threadID] = td
            Log.d("DBG", "[ThreadPool] ThreadID : $threadID")
            return threadID
        }

        public fun remove(id : Int) {
            pool[id]!!.stop()
            pool.remove(id)
        }

        public fun close() {
            for((idx, td) in pool) td.stop()
            pool.clear()
        }

    }
}