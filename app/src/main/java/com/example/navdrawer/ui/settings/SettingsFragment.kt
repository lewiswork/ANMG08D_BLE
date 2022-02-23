package com.example.navdrawer.ui.settings

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.navdrawer.Global
import com.example.navdrawer.PacketKind
import com.example.navdrawer.databinding.FragmentSettingsBinding
import com.example.navdrawer.packet.Packet
import java.lang.Exception
import kotlin.experimental.or

class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("[ADS] ", "SettingsFragment > onCreate")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        val root: View = binding.root
        setListeners()          // Listener 등록
        Log.d("[ADS] ", "SettingsFragment > onCreateView")
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        Log.d("[ADS] ", "SettingsFragment > onDestroyView")
    }

    private fun setListeners() {
        binding.swSystemLog.setOnClickListener(listenerSwitches)
        binding.swMonitoringLog.setOnClickListener(listenerSwitches)
    }

    private fun setControlStatus() {
        binding.swMonitoringLog.setOnClickListener(listenerSwitches)
    }

    private val listenerSwitches = View.OnClickListener {

        try {
            when (it) {
                binding.swSystemLog -> {
                    Log.d("[ADS] ", "Sw status : ${binding.swSystemLog.isChecked}")
                }
                binding.swMonitoringLog -> {
                    Log.d("[ADS] ", "Sw status : ${binding.swMonitoringLog.isChecked}")
                }
            }
        } catch (ex: Exception) {
            Log.d("[ADS]", "${ex.message}")
        }
    }
}