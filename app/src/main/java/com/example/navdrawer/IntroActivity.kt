package com.example.navdrawer

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log

class IntroActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

        var handler = Handler()
        handler.postDelayed({
            var intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }, 2000)

        Log.d("[ADS] ", "IntroActivity > onCreate")
    }

    override fun onPause() {
        super.onPause()
        Log.d("[ADS] ", "IntroActivity > onPause")
        finish()
    }
}