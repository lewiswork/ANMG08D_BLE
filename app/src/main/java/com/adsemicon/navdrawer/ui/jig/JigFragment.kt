package com.adsemicon.navdrawer.ui.jig

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.adsemicon.navdrawer.Global
import com.adsemicon.navdrawer.databinding.FragmentJigBinding
import com.adsemicon.navdrawer.PacketKind
import com.adsemicon.navdrawer.packet.RPacket
import com.adsemicon.navdrawer.packet.Packet
import java.lang.Exception
import java.util.*
import kotlin.experimental.and
import kotlin.experimental.or


class JigFragment : Fragment() {

    private lateinit var jigViewModel: JigViewModel
    private var _binding: FragmentJigBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    private lateinit var mmJigThread: JigFragment.JigThread
    private var mmJigThreadOn:Boolean = false

    // Hardware Read Packet 용 Timer
    var tick=false
    var timer : Timer? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        jigViewModel = ViewModelProvider(this)[JigViewModel::class.java]
        _binding = FragmentJigBinding.inflate(inflater, container, false)
        val root: View = binding.root

        setListeners()          // Listener 등록

        Log.d("[ADS] ", "Jig Fragment > onCreateView")
        return root
    }

    override fun onPause() {
        super.onPause()
        timer!!.cancel()

        Log.d("[ADS] ", "Jig Fragment > onPause > Timer stopped : $timer")
    }

    override fun onResume() {
        super.onResume()

        checkConnections()        // BT 연결상태 별 초기화 처리
        timer = kotlin.concurrent.timer(period = 1000) {
            tick = true
        }
        Log.d("[ADS] ", "Jig Fragment > onResume > Timer started : $timer")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        mmJigThreadOn = false
        Log.d("[ADS] ", "Jig Fragment > onDestroyView")
    }

    private fun checkConnections() {
        if (com.adsemicon.navdrawer.Global.isBtConnected) {
            mmJigThreadOn = true
            mmJigThread = JigThread()
            mmJigThread.start()

            setControlEnabled(true)

        } else {
            setControlEnabled(false)
        }
    }

    private fun setControlEnabled(flag:Boolean) {
        binding.swVdd.isEnabled = flag
        binding.swI2c.isEnabled = flag
    }

    private fun setListeners() {
        binding.swVdd.setOnClickListener(listenerForRelays)
        binding.swI2c.setOnClickListener(listenerForRelays)
        binding.btnClearRelays.setOnClickListener(listenerForRelays)
    }

    private val listenerForRelays = View.OnClickListener {
        var mask: Byte = 0x00

        try {
            when (it) {
                binding.swVdd, binding.swI2c -> {
                    if (binding.swVdd.isChecked) mask = mask or 0x02
                    if (binding.swI2c.isChecked) mask = mask or 0x04
                    com.adsemicon.navdrawer.Global.hwStat = mask
                }
                binding.btnClearRelays -> {
                    com.adsemicon.navdrawer.Global.hwStat = 0x00
                    binding.swVdd.isChecked = false
                    binding.swI2c.isChecked = false
                }
            }

            if (com.adsemicon.navdrawer.Global.hwStatPrev == 0x06.toByte() && com.adsemicon.navdrawer.Global.hwStat != 0x06.toByte()) {
                Packet.send(com.adsemicon.navdrawer.Global.outStream, com.adsemicon.navdrawer.PacketKind.MonSet, 0x00)  // Stop All Monitoring
                Log.d("[ADS]", "Monitoring stopped.")
                //Thread.sleep(100) // ok
                Thread.sleep(10) // ok
            }
            Packet.send(com.adsemicon.navdrawer.Global.outStream, com.adsemicon.navdrawer.PacketKind.HwWrite, com.adsemicon.navdrawer.Global.hwStat) // Send packet
            com.adsemicon.navdrawer.Global.hwStatPrev = com.adsemicon.navdrawer.Global.hwStat

            //binding.textView3.text = Global.hwStat.toString()    // for debugging
        } catch (ex: Exception) {
            Log.d("[ADS]", "Sending packet error! / ${ex.message}}")
        }
    }


    //---------------------------------------------------------------------------------------//
    // Jig 처리용 Inner Class
    //---------------------------------------------------------------------------------------//
    inner class JigThread : Thread() {
        override fun run() {
            var qEmpty :Boolean
            var packet : RPacket
            
            Log.d("[ADS] ", "Jig thread started. ID : ${this.id}")

            while (mmJigThreadOn) {
                //------------------------------------------------------------------------------//
                // Timer 처리
                //------------------------------------------------------------------------------//
                if (tick){
                    Packet.send(com.adsemicon.navdrawer.Global.outStream, com.adsemicon.navdrawer.PacketKind.HwRead) // Send packet
                    tick = false
                }

                //------------------------------------------------------------------------------//
                // Packet 처리
                //------------------------------------------------------------------------------//
                synchronized(com.adsemicon.navdrawer.Global.hwQueue) { qEmpty = com.adsemicon.navdrawer.Global.hwQueue.isEmpty() }

                if (!qEmpty) {
                    try {
                        synchronized(com.adsemicon.navdrawer.Global.hwQueue) { packet = com.adsemicon.navdrawer.Global.hwQueue.remove() }

                        when (packet.kind) {
                            com.adsemicon.navdrawer.PacketKind.HwRead -> {
                                activity?.runOnUiThread {
                                    displayRelayStatus()
                                }
                            }
                        }
                    } catch (ex: NoSuchElementException) {
                        com.adsemicon.navdrawer.Global.errLog.printError(ex)
                        Log.d("[ADS/ERR] ", ex.toString())
                        continue
                    } catch (ex: Exception) {
                        com.adsemicon.navdrawer.Global.errLog.printError(ex)
                        Log.d("[ADS/ERR] ", ex.message.toString())
                        Log.d("[ADS/ERR] ", ex.printStackTrace().toString())
                        break
                    }
                }
                //------------------------------------------------------------------------------//

                Thread.sleep(10)
            }
            Log.d("[ADS] ", "Jig thread finished. ID : ${this.id}")
        }
    }

    private fun displayRelayStatus() {
        binding.swVdd.isChecked = (com.adsemicon.navdrawer.Global.hwStat and 0x02) == 0x02.toByte()
        binding.swI2c.isChecked = (com.adsemicon.navdrawer.Global.hwStat and 0x04) == 0x04.toByte()
    }
}