package com.example.filesystemtest2.database

import android.content.Context
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.filesystemtest2.R
import java.io.FileOutputStream

class itemAdapter(
    private val context: Context,
    private val images : List<Bitmap> //引数として受け取ったリスト
): RecyclerView.Adapter<itemAdapter.ViewHolder>(){

    //クリック処理を追加
    private  var listener: ((Int) -> Unit)? = null
    fun setOnItemClickListener(listener:(Int) -> Unit){
        this.listener = listener
    }

    class ViewHolder(view: View):RecyclerView.ViewHolder(view) {
        val image: ImageView = view.findViewById(R.id.image)//表示したい部品
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.card_layout,parent,false)
        return ViewHolder(view)
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var imageResource = images[position] //リストを展開
        //Glide
        //ライブラリを使わなくても画像は表示できますが、メモリ管理などがシビア(小手先では解決不可能)で、表示が不安定なので使っています
        Glide.with(context)
            .load(imageResource)
            .error(android.R.drawable.ic_btn_speak_now)
            .into(holder.image)
        holder.itemView.setOnClickListener{
            listener?.invoke(position)      //クリック処理
        }
    }

    override fun getItemCount(): Int = images.size

}
