package com.adsemicon.anmg08d.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SimpleAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.adsemicon.anmg08d.R
import com.adsemicon.anmg08d.databinding.FragmentHomeBinding
import com.adsemicon.anmg08d.ui.connect.ConnectFragment
import java.lang.Exception


class HomeFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel
    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!


    val data1 = arrayOf(
        "1. Connect", "2. Jig", "3. Monitoring", "4. Settings", "5. About"
    )

    val data2 = arrayOf(
        "Bluetooth 연결",
        "Jig 설정(Relay On/Off 등)",
        "Touch Data Monitoring 및 Register 설정",
        "App. 설정(Log On/Off 등)",
        "App. 정보"
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        homeViewModel =
            ViewModelProvider(this)[HomeViewModel::class.java]

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var str = "\n좌측 상단 Navigation 메뉴를 사용하세요.\n"
        var str1 = "\n1. Connect\n    Bluetooth 연결\n"
        var str2 = "2. Jig\n    Relay On/Off\n"
        var str3 = "3. Monitoring\n    Touch Data Monitoring 및 Register 설정\n"
        var str4 = "4. Settings\n    App. 설정\n"
        var str5 = "5. About\n    App. 정보\n"

        binding.tvHomeTitle.text = str
//        binding.tvHome.text = str1
//        binding.tvHome2.text = str2
//        binding.tvHome3.text = str3
//        binding.tvHome4.text = str4
//        binding.tvHome5.text = str5

        //
        val dataList = ArrayList<HashMap<String, Any>>()
        for (i in data1.indices) {
            val map = HashMap<String, Any>()
            map["data1"] = data1[i]
            map["data2"] = data2[i]
            dataList.add(map)
        }

        //
        val keys = arrayOf("data1", "data2")

        //
        val ids = intArrayOf(R.id.tvMenuName, R.id.tvMenuDescription)

        val adapter1 = SimpleAdapter(requireContext(), dataList, R.layout.row_home, keys, ids)
        binding.lvMenuInfo.adapter = adapter1
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}