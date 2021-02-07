package com.example.filesystemtest2.fragment

import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import com.example.filesystemtest2.database.item
import com.example.filesystemtest2.databinding.FragmentListBinding
import com.example.filesystemtest2.database.itemAdapter
import io.realm.Realm
import io.realm.kotlin.where
import java.io.BufferedInputStream

class ListFragment : Fragment() {
    private var _binding : FragmentListBinding? = null
    private val binding get() = _binding!!
    private lateinit var realm: Realm
    private val args: ListFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        realm = Realm.getDefaultInstance() //realmのオープン処理
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentListBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //選んだカテゴリの画像リストを表示する
        val category = args.category
        val items = realm.where<item>().equalTo("category", category).findAll()
        //画像の読込
        val imageList = mutableListOf<Bitmap>()
        for(i in items) {
            try { BufferedInputStream(context?.openFileInput(i.name)).use { bufferedInputStream ->
                val itemImage = BitmapFactory.decodeStream(bufferedInputStream)
                imageList.add(itemImage)
                bufferedInputStream.close()
            }
            }catch(e : Exception){ println("エラー発生")}
        }

        //アルゴリズムの修正が必要
        binding.RecyclerView.apply {
            layoutManager =
                when {
                    resources.configuration.orientation
                            == Configuration.ORIENTATION_PORTRAIT
                    -> GridLayoutManager(requireContext(), 2)
                    else
                    -> GridLayoutManager(requireContext(), 4)
                }
            adapter = itemAdapter(context, imageList).apply{
                //画面遷移
                setOnItemClickListener { position:Int ->
                    val action = items[position]?.let { ListFragmentDirections.actionListFragmentToDetailFragment2( it.name) }
                    if (action != null) findNavController().navigate(action)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDestroy() {
        super.onDestroy()
        realm.close() //realmのクローズ処理
    }
}