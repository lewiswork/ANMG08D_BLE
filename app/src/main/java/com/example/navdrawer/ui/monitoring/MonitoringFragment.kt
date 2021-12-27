package com.example.navdrawer.ui.monitoring

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.navdrawer.Global
import com.example.navdrawer.databinding.FragmentMonitoringBinding

class MonitoringFragment : Fragment() {
    private lateinit var monitoringViewModel: MonitoringViewModel
    private var _binding: FragmentMonitoringBinding? = null
    private val binding get() = _binding!!

    private lateinit var mmDisplayThread: DisplayThread
    private var mmDisplayThreadOn:Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        monitoringViewModel =
            ViewModelProvider(this).get(MonitoringViewModel::class.java)

        _binding = FragmentMonitoringBinding.inflate(inflater, container, false)
        val root: View = binding.root

        Log.d("ME", "Monitoring Fragment > onCreateView")

        if (Global.isBtConnected) {
            _binding?.tvMonitoring?.text = "BT Connected"

            mmDisplayThreadOn = true
            mmDisplayThread = DisplayThread()
            mmDisplayThread.start()

        } else {
            _binding?.tvMonitoring?.text = "BT Not Connected"
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        mmDisplayThreadOn = false
        Log.d("ME", "Monitoring Fragment > onDestroyView")
    }

    //---------------------------------------------------------------------------------------//
    // Display 처리용 Inner Class
    //---------------------------------------------------------------------------------------//
    inner class DisplayThread : Thread() {
        override fun run() {
            var pk: String = ""
            var sb: StringBuilder = StringBuilder()
            var sidx: Int = 0
            var eidx: Int = 0
            var qEmpty: Boolean = true
            //var qCount: Int = -1

            Log.d("ME", "Display thread started. ID : ${this.id}")
            while (mmDisplayThreadOn) {

                var str = java.lang.StringBuilder()

                synchronized(this) {
                    for (i in Global.monitoring.mmChData.indices) {
                        if (i == Global.monitoring.DM_CH_IDX)
                            str.append("CH DM : ${Global.monitoring.mmChData[i].touch}")
                        else
                            str.append("CH ${i + 1} : ${Global.monitoring.mmChData[i].touch}\n")
                    }
                }
                activity?.runOnUiThread {
                    _binding?.tvMonitoring?.text = str.toString()
                }

                Thread.sleep(10)
            }
            Log.d("ME", "Display thread finished. ID : ${this.id}")
        }
    }
}