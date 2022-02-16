package com.example.navdrawer.ui.monitoring

import android.annotation.SuppressLint
import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.example.navdrawer.Global
import com.example.navdrawer.R
import kotlin.experimental.and
import com.example.navdrawer.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {

    lateinit var btnClose:Button
    lateinit var tvStatusReg:TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // View Binding
        //binding = ActivityRegisterBinding.inflate(layoutInflater)
        //setContentView(binding.root)

        //binding.btnClose.setOnClickListener(listenerClose)
        btnClose = findViewById<Button>(R.id.btnClose)
        tvStatusReg = findViewById<Button>(R.id.tvStatusReg)

        btnClose.setOnClickListener(listenerClose)
    }

    override fun onResume() {
        super.onResume()

        setControlEnabled()        // BT 연결상태 별 초기화 처리
        Log.d("[ADS] ", "Monitoring Fragment > RegisterActivity > onResume")
    }


    private val listenerClose = View.OnClickListener {
        setResult(RESULT_OK, intent)
        finish()
    }

    private fun setControlEnabled() {
        if (Global.isBtConnected) {
            if ((Global.hwStat and 0x06) != 0x06.toByte()) {
                tvStatusReg.text = "Relays are off."
            } else {
                tvStatusReg.text = "BT connected and relays are on."
            }
        } else {
            tvStatusReg.text = "BT disconnected."
        }
    }


}