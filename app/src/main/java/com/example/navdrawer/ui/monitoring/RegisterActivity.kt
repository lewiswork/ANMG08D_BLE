package com.example.navdrawer.ui.monitoring

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import com.example.navdrawer.R

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val btnClose = findViewById<Button>(R.id.btnClose)
        btnClose.setOnClickListener(listenerClose)

    }


    private val listenerClose = View.OnClickListener {
        setResult(Activity.RESULT_OK, intent)
        finish()
    }
}