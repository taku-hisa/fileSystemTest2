package com.example.filesystemtest2

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.lifecycleScope
import com.example.filesystemtest2.database.item
import com.example.filesystemtest2.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar
import io.realm.Realm
import io.realm.kotlin.createObject
import io.realm.kotlin.where
import kotlinx.coroutines.*
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*

private val category = arrayOf("A","B","C","D","E") //カテゴリを定義
private var CATEGORY_CODE: Int = 42                 //カテゴリの変数

class MainActivity : AppCompatActivity()  {
    private lateinit var binding: ActivityMainBinding   //ビューバインディング
    private lateinit var realm: Realm

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        realm = Realm.getDefaultInstance() //realmのオープン処理

        //ギャラリーを開く
        binding.button.setOnClickListener {
            CATEGORY_CODE = 0 //初期選択="A"
            AlertDialog.Builder(this).apply {
                setTitle("SELECT")
                setSingleChoiceItems(category, 0) { _, i -> //初期選択="A"
                    CATEGORY_CODE = i  // 選択した項目を保持
                }
                setPositiveButton("OK") { _, _ ->
                    if (CATEGORY_CODE < category.size + 1) {
                        intent()

                    }
                }
                setNegativeButton("Cancel", null)
            }.show()
        }
    }

    private fun intent() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_OPEN_DOCUMENT
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        this.startActivityForResult(intent, CATEGORY_CODE)
    }

    //写真が選択された後の動き
    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)
        if (resultCode != RESULT_OK) {
            return
        }
        when (requestCode) {
            CATEGORY_CODE -> {
                try { ByteArrayOutputStream().use { byteArrOutputStream ->
                    val uri = resultData?.data
                    // onDestroyのときに処理が破棄される？
                    //GlobalScope.launch(Dispatchers.Default) {
                        if (uri != null) {
                            //一枚選択時の動作
                            val inputStream = contentResolver?.openInputStream(uri)
                            if (inputStream != null) saveImage(byteArrOutputStream, inputStream, 999)
                        } else {
                            //複数枚選択時の動作
                            val clipData = resultData?.clipData
                            val clipItemCount = clipData?.itemCount?.minus(1) //エラーになるので、数字を１減らす。
                            for (i in 0..clipItemCount!!) {
                                val item = clipData.getItemAt(i).uri
                                val inputStream = contentResolver?.openInputStream(item)
                                if (inputStream != null) saveImage(byteArrOutputStream, inputStream, i)
                            }
                        }
                    //}
                    Toast.makeText(this, "保存完了", Toast.LENGTH_LONG).show()
                }
                } catch (e: Exception) {
                    println("エラー発生1")
                }
            }
        }
    }

  fun saveImage(byteArrOutputStream:ByteArrayOutputStream, inputStream:InputStream, int:Int) {
        val date = Date()
        val sdf = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault())
        val name = "${sdf.format(date)}_${int}.jpg"
        val image = BitmapFactory.decodeStream(inputStream)

        try { openFileOutput(name, Context.MODE_PRIVATE).use { outStream ->
            image.compress(Bitmap.CompressFormat.JPEG, 100, outStream)
            outStream.write(byteArrOutputStream.toByteArray())
            inputStream.close() //明示的に閉じる
            createDatabase(name)
        }
        }catch(e : Exception){ println("エラー発生2") }
    }

    private fun createDatabase(name:String){
        realm.executeTransaction { db: Realm ->
            val maxId = db.where<item>().max("id")
            val nextId : Long = (maxId?.toLong() ?: 0L) + 1L
            val item = db.createObject<item>(nextId)
            item.category = category[CATEGORY_CODE]
            item.name = name
            item.detail = ""
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        realm.close()  //realmのクローズ処理
    }
}