package com.example.navdrawer

import android.os.Bundle
import android.os.Environment
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
import androidx.core.content.ContextCompat
import com.example.navdrawer.adlib.ADLog
import com.example.navdrawer.databinding.ActivityMainBinding
import com.example.navdrawer.function.log.MonitoringLog
import com.example.navdrawer.function.log.SystemLog
import com.example.navdrawer.monitor.Monitoring
import com.example.navdrawer.thread.GetPacketThread
import java.io.File

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
            R.id.nav_home,          // 기본 생성(3개 중 1)
            R.id.nav_connect,       // 기본 생성(3개 중 1) 후 명칭 변경
            R.id.nav_jig,           // 기본 생성(3개 중 1) 후 명칭 변경
            R.id.nav_monitoring,    // 추가
            R.id.nav_settings       // 추가
        ), drawerLayout)
        //-------------------------------------------------------------------------------------//

        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        //Log.d("[ADS] ", Global.regCon.registers.toString())
        //Log.d("[ADS] ", Global.touchLog.str)
//        Log.d("[ADS] ", "path : ${Global.packetLog.file}")

        // Log 및 Monitoring 객체 생성
//        val externalStorageVolumes: Array<out File> =
//            ContextCompat.getExternalFilesDirs(applicationContext, null)
//        Global.basePath = externalStorageVolumes[0]
//

        Global.packetLog = SystemLog(applicationContext, "system", "packet.txt")
        Global.errLog = SystemLog(applicationContext, "system", "error.txt", "ERR", true)

        // Monitoring Logs
        Global.touchLog = MonitoringLog(applicationContext,"monitoring", "touch.txt")
        Global.percentLog = MonitoringLog(applicationContext,"monitoring", "percent.txt")
        Global.monitoring = Monitoring()   // Touch/Percent Log 객체 생성 이후에 생성

        Log.d("[ADS] ", "path : ${Global.packetLog.file}")

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