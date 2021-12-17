package com.example.navdrawer.ui.monitoring

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.Observer
import com.example.navdrawer.R
import com.example.navdrawer.databinding.FragmentMonitoringBinding

class MonitoringFragment : Fragment() {
    private lateinit var monitoringViewModel: MonitoringViewModel
    private var _binding: FragmentMonitoringBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        monitoringViewModel =
            ViewModelProvider(this).get(MonitoringViewModel::class.java)

        _binding = FragmentMonitoringBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.tvMonitoring
        monitoringViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}