package com.example.filesystemtest2.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import com.example.filesystemtest2.R
import com.example.filesystemtest2.databinding.FragmentMainBinding

private val category = arrayOf("A","B","C","D","E")

class MainFragment : Fragment() {
    private var _binding : FragmentMainBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMainBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //カテゴリーをリスト表示する
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, category)
        binding.categoryList.adapter = adapter
        binding.categoryList.setOnItemClickListener{parent,view,position,id->  //ボタンを押されたとき
            val item = (view.findViewById<TextView>(android.R.id.text1)).text.toString() //listviewの文字を取得
            val action = MainFragmentDirections.actionMainFragmentToListFragment2(item) //listviewの文字をListFragment2へ投げる
            findNavController().navigate(action) //画面遷移するおまじない
        }
    }

    override fun onDestroy(){
        super.onDestroy()
        _binding = null
    }
}