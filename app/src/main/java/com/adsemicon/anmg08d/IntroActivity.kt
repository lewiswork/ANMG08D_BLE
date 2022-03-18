package com.adsemicon.anmg08d

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.Window
import android.view.WindowManager
import android.widget.TextView
import androidx.core.content.ContentProviderCompat.requireContext


class IntroActivity : AppCompatActivity() {

    lateinit var tvAppInfo:TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_intro)

        var handler = Handler()
        handler.postDelayed({
            var intent = Intent(this, com.adsemicon.anmg08d.MainActivity::class.java)
            startActivity(intent)
        }, 2500)

        tvAppInfo = findViewById(R.id.tvAppInfoIntro)

        val applicationInfo = applicationContext.applicationInfo
        val stringId = applicationInfo.labelRes
        val appName:String
        if (stringId == 0) {
            appName = applicationInfo.nonLocalizedLabel.toString()
        }
        else {
            appName = this.getString(stringId)
        }

        val pInfo = applicationContext.packageManager.getPackageInfo(applicationContext.packageName, 0)
        val version = pInfo.versionName

        val stringInfo = "$appName V$version"
        tvAppInfo.text = stringInfo

        Log.d("[ADS] ", "IntroActivity > onCreate")
    }

    override fun onPause() {
        super.onPause()
        Log.d("[ADS] ", "IntroActivity > onPause")
        finish()
    }
}