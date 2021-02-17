package com.example.filesystemtest2.fragment

import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.VectorDrawable
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
import java.io.FileInputStream
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.ByteBuffer
import java.util.*

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
        val category = args.category //safeargsの機能で、category:Stringを受け取る
        val items = realm.where<item>().equalTo("category", category).findAll() //categoryのitemを検索する
        val bitmapList = mutableListOf<Bitmap>() //リストの初期化
        //画像の読込
        for(i in items) {
            val bitmap =BitmapFactory.decodeByteArray(i.image,0,i.image.size) //検索結果からitem内のimage(画像)を復元する
                                                                                     //byte型→bitmap ※保存時はbyte型、表示はbitmap型まで変換してください※
            bitmapList.add(bitmap)//リストへ追加する
        }

        binding.RecyclerView.apply {
            layoutManager =
                when {
                    resources.configuration.orientation
                            == Configuration.ORIENTATION_PORTRAIT
                    -> GridLayoutManager(requireContext(), 2)
                    else
                    -> GridLayoutManager(requireContext(), 4)
                }
            adapter = itemAdapter(context, bitmapList).apply{ //adapterへ画像リストを投げる
                //画面遷移
                setOnItemClickListener { position:Int ->
                    val action = items[position]?.let { ListFragmentDirections.actionListFragmentToDetailFragment2( it.id) }
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