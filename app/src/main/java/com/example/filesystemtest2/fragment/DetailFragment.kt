package com.example.filesystemtest2.fragment

import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.filesystemtest2.R
import com.example.filesystemtest2.database.item
import com.example.filesystemtest2.databinding.FragmentDetailBinding
import com.example.filesystemtest2.databinding.FragmentListBinding
import io.realm.Realm
import io.realm.kotlin.where
import java.io.BufferedInputStream

class DetailFragment : Fragment() {
    private var _binding : FragmentDetailBinding? = null
    private val binding get() = _binding!!
    private lateinit var realm: Realm
    private val args: DetailFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        realm = Realm.getDefaultInstance() //realmのオープン処理
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDetailBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //選んだカテゴリの画像リストを表示する
        val image = args.image
        realm.executeTransaction{db:Realm->
            val item = db.where<item>().equalTo("name",image).findFirst()
            binding.multiText.setText(item!!.detail)
        }
        val bufferedInputStream = BufferedInputStream(context?.openFileInput(image))
        val bitmap = BitmapFactory.decodeStream(bufferedInputStream)
        bufferedInputStream.close()
        Glide.with(requireContext())
            .load(bitmap)
            .error(android.R.drawable.ic_btn_speak_now)
            .into(binding.imageView) //Glide

        binding.fab2.setOnClickListener{
            realm.executeTransaction { db: Realm ->
                val item = db.where<item>().equalTo("name",image).findFirst()
                item?.detail = binding.multiText.text.toString()
                Toast.makeText(requireContext(),"更新完了", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDestroy(){
        super.onDestroy()
        realm.close() //realmのクローズ処理
    }
}