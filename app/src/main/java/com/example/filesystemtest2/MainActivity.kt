package com.example.filesystemtest2


import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.filesystemtest2.database.item
import com.example.filesystemtest2.databinding.ActivityMainBinding
import io.realm.Realm
import io.realm.kotlin.createObject
import io.realm.kotlin.where
import java.io.ByteArrayOutputStream
import java.io.InputStream

private val category = arrayOf("A","B","C","D","E") //カテゴリを定義
private var CATEGORY_CODE: Int = 42                 //カテゴリの変数 42は適当な数値を入れているだけです

//MainActivity フラグメントの表示と登録ボタンの処理
class MainActivity : AppCompatActivity()  {
    private lateinit var binding: ActivityMainBinding //ビューバインディング
    private lateinit var realm: Realm //realmのインスタンス

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater) //ビューバインディング
        setContentView(binding.root) //ビューバインディング
        realm = Realm.getDefaultInstance() //realmのオープン処理


        binding.button.setOnClickListener {

            //ボタンを押すと、アラートダイアログが表示されます。
            CATEGORY_CODE = 0  //初期選択 category[0] = "A"
            AlertDialog.Builder(this).apply {
                setTitle("SELECT")
                setSingleChoiceItems(category, 0) { _, i -> //初期選択 category[0] = "A"
                    CATEGORY_CODE = i  // 選択した項目を保持
                }
                setPositiveButton("OK") { _, _ -> //OKボタンを押したときの処理
                    if (CATEGORY_CODE < category.size + 1) {
                        intent() //Strage Access Framework(以下SAF)をインテントで呼び出します
                                 //SAFとは、名前の通り、Androidのストレージに簡単にアクセスするための機能です
                    }
                }
                setNegativeButton("Cancel", null) //Cancelボタンを押したときの処理
            }.show() //以上の内容でアラートダイアログを表示する
        }
    }

    //SAF　参考：https://akira-watson.com/android/gallery.html
    private fun intent() { //SAFの定義
        val intent = Intent() //インテントのインスタンス
        intent.type = "image/*" //画像のみを表示するオプション
        intent.action = Intent.ACTION_OPEN_DOCUMENT //ファイルを選択して、変数にするためのオプション
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true) //複数ファイルを選択するオプション
        this.startActivityForResult(intent, CATEGORY_CODE) //以上の内容でSAFを終了し、onActivityResultへ結果を返します
    }

    //写真が選択された後の動き
    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)
        if (resultCode != RESULT_OK) { //インテントが正常に送れたかどうか
            return //だめなら終了
        }
        when (requestCode) {
            CATEGORY_CODE -> {   //requestCode = CATEGORY_CODEであれば、以下の処理を行う
                val uri = resultData?.data //一枚選択時はresultDataクラスのdataに画像が格納されている
                if (uri != null) { //一枚選択時なら
                    //一枚選択時の動作
                    val inputStream = contentResolver?.openInputStream(uri) //resultData.dataはuri型で格納されているので、inputStreamへ変換する
                                                                            //uriを変換するときの選択肢として、①file型　②inputStream型があるが、
                                                                            //ディレクトリ指定が不要な②inputStream型を使うほうが楽なので使いました
                                                                            //参考:https://akira-watson.com/android/fileoutputstream.html
                    if (inputStream != null) saveImage( inputStream, 999) //saveImageメソッドを呼び出す(intはfilesystemtestの名残で残っており、不要です)
                } else { //一枚選択時でないなら
                    //複数枚選択時の動作
                    val clipData = resultData?.clipData //複数枚選択時はresultDataクラスのclipdataに画像が格納されている
                    val clipItemCount = clipData?.itemCount?.minus(1) //エラーになるので、数字を１減らす
                                                                            //原因はよくわからない
                    for (i in 0..clipItemCount!!) { //各画像のUriを取得する
                        val item = clipData.getItemAt(i).uri
                        val inputStream = contentResolver?.openInputStream(item) //一枚選択地と同様の処理を行う
                        if (inputStream != null) saveImage( inputStream, i)
                    }
                }
                Toast.makeText(this, "保存完了", Toast.LENGTH_LONG).show()
            }
        }
    }

  fun saveImage( inputStream:InputStream, int:Int) { //画像を保存可能なbyte型へ変換するためのメソッド
      try {
          ByteArrayOutputStream().use { byteArrOutputStream -> //.use{byte...-> : javaでいうところのtry...with resourceです
                                                               //つまり、byteArrayOutputStreamが使い終わったら自動的に閉じます
                                                               //byteArrayStreamインスタンスは使いまわさないのが良いらしいのでここに定義しました
                                                               //参考:https://hhelibex.hatenablog.jp/entry/20091027/1256621926
              val image = BitmapFactory.decodeStream(inputStream) //inputStreamをbitmapへ変換します
              image.compress(Bitmap.CompressFormat.JPEG, 70, byteArrOutputStream) //bitmapをjpeg(圧縮形式)へ変換します
              val byte = byteArrOutputStream.toByteArray() //jpegをbyte型へ変換します
                                                           //uri→inputStream→bitmap→jpeg→byte型　※保存時はbyte型、表示はbitmap型まで変換してください※
              createDatabase(byte) //createDatabaseメソッドを呼び出します
              inputStream.close() //明示的に閉じる
          }
      }catch(e:Exception){
          println("エラー発生")
      }
  }

    private fun createDatabase(byte:ByteArray){ //realmへ保存します
        realm.executeTransaction { db: Realm ->
            val maxId = db.where<item>().max("id")
            val nextId : Long = (maxId?.toLong() ?: 0L) + 1L
            val item = db.createObject<item>(nextId)
            item.category = category[CATEGORY_CODE]
            item.image = byte
            item.detail = ""
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        realm.close()  //realmのクローズ処理
    }
}