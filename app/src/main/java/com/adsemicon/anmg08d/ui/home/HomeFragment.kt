package com.adsemicon.anmg08d.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.adsemicon.anmg08d.databinding.FragmentHomeBinding
import com.adsemicon.anmg08d.ui.connect.ConnectFragment
import java.lang.Exception


class HomeFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel
    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textHome
        homeViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })

//        binding.btnConnectFrag.setOnClickListener {
//            val intent = Intent(context, ConnectFragment::class.java)
//            startActivity(intent)
//        }

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        binding.btnConnectFrag.setOnClickListener {
//            val nextFrag = ConnectFragment()
//try {
//    requireActivity().supportFragmentManager.beginTransaction()
//        .remove(this@HomeFragment)
//        .replace((requireView().parent as ViewGroup).id, nextFrag, "findThisFragment")
//        .addToBackStack(null)
//        .commit()
//}catch (ex:Exception){
//
//}
//        }
//
//        btn_goToFragment2.setOnClickListener {
//            var fr = getFragmentManager()?.beginTransaction()
//            fr?.replace(R.id.fragment, Fragment_Two())
//            fr?.commit()
//        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}