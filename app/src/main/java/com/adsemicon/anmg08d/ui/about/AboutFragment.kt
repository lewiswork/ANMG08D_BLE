package com.adsemicon.anmg08d.ui.about

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.adsemicon.anmg08d.databinding.FragmentAboutBinding


class AboutFragment : Fragment() {
    private var _binding: FragmentAboutBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("[ADS] ", "AboutFragment > onCreate")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentAboutBinding.inflate(inflater, container, false)
        val root: View = binding.root
        Log.d("[ADS] ", "AboutFragment > onCreateView")

        val applicationInfo = requireContext().applicationInfo
        val stringId = applicationInfo.labelRes
        val appName:String
        if (stringId == 0) {
            appName = applicationInfo.nonLocalizedLabel.toString()
        }
        else {
            appName = this.getString(stringId)
        }

        val pInfo = requireContext().packageManager.getPackageInfo(context!!.packageName, 0)
        val version = pInfo.versionName

        val stringInfo = "$appName V$version"
        binding.tvAppInfo.text = stringInfo

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        Log.d("[ADS] ", "AboutFragment > onDestroyView")
    }

}