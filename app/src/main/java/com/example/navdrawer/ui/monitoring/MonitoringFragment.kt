package com.example.navdrawer.ui.monitoring

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.navdrawer.GlobalVariables
import com.example.navdrawer.databinding.FragmentMonitoringBinding

class MonitoringFragment : Fragment() {
    private lateinit var monitoringViewModel: MonitoringViewModel
    private var _binding: FragmentMonitoringBinding? = null
    private val binding get() = _binding!!

    private lateinit var displayThread: DisplayThread
    private var mmDisplayThreadOn:Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        monitoringViewModel =
            ViewModelProvider(this).get(MonitoringViewModel::class.java)

        _binding = FragmentMonitoringBinding.inflate(inflater, container, false)
        val root: View = binding.root

//        val textView: TextView = binding.tvMonitoring
//        monitoringViewModel.text.observe(viewLifecycleOwner, Observer {
//            textView.text = it
//        })

        Log.d("ME", "Monitoring Fragment > onCreateView")

        if (GlobalVariables.isBtConnected) {
            _binding?.tvMonitoring?.text = "BT Connected"

            mmDisplayThreadOn = true
            displayThread = DisplayThread()
            displayThread.start()

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
                try {
                    //Log.d("MEA", "Display Thread")
                    synchronized(this) {
                        qEmpty = GlobalVariables.rStringQueue.isEmpty()
                        //qCount = GlobalVariables.sampleQueue.count()
                    }

                    if (!qEmpty) {
                        //if (qCount > 0) {
                        try {
                            synchronized(this) {
                                sb.append(GlobalVariables.rStringQueue.remove())
                            }

                        } catch (ex: NoSuchElementException) {
                            Log.d("MEX", GlobalVariables.rStringQueue.count().toString())
                            ex.printStackTrace()
                            //continue
                            break
                        }

                        while (sb.isNotEmpty()) {
                            if (sb.contains('S')) {
                                sidx = sb.indexOf('S')
                            }

                            if (sb.contains('Z')) {
                                eidx = sb.indexOf('Z')
                                pk = sb.substring(sidx, eidx + 1)

                                if (sb.length > pk.length) {
                                    sidx = eidx + 1
                                    sb = StringBuilder(sb.substring(sidx, sb.length))
                                } else {
                                    sb = StringBuilder("")
                                }
                                Log.d("MED", pk)
                            } else {
                                break
                            }

//                            this@MainActivity.runOnUiThread(java.lang.Runnable {
//                                tvReceiveMsg.text = pk
                            //})
                        }
                    }
                } catch (e: java.io.IOException) {
                    Log.d("MEX", "$sidx/$eidx")
                    e.printStackTrace()
                    break
                    //continue
                }
            }
            Log.d("ME", "Display thread finished. ID : ${this.id}")
        }
    }
}