package com.example.navdrawer

import android.os.Bundle
import android.util.Log
import android.view.Menu
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import com.example.navdrawer.adlib.ADLog
import com.example.navdrawer.databinding.ActivityMainBinding
import com.example.navdrawer.thread.GetPacketThread

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.appBarMain.toolbar)


        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)

        //-------------------------------------------------------------------------------------//
        // Fragment 추가 시 수정 필요
        //-------------------------------------------------------------------------------------//
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(setOf(
            R.id.nav_home,      // 기본 생성(3개 중 1)
            R.id.nav_connect,   // 기본 생성(3개 중 1) 후 명칭 변경
            R.id.nav_jig,       // 기본 생성(3개 중 1) 후 명칭 변경
            R.id.nav_monitoring // 추가
        ), drawerLayout)
        //-------------------------------------------------------------------------------------//

        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        //for (r in Global.regCon.registers) Log.d("[ADS] ", r.toString())
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onDestroy() {
        super.onDestroy()
        disconnectBt()
    }

    //---------------------------------------------------------------------------------------//
    // BT Disconnect 함수, Stream, Socket Close 및 Thread 종료
    //---------------------------------------------------------------------------------------//
    private fun disconnectBt() {

        if (Global.inStream != null) Global.inStream!!.close()
        if (Global.outStream != null) Global.outStream!!.close()
        if (Global.socket != null) Global.socket!!.close()

        Global.rxThreadOn = false
        Global.rxPacketThreadOn = false

        Global.rawRxBytesQueue.clear()
        Global.isBtConnected = false
    }
}