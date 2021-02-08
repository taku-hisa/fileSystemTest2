package com.example.filesystemtest2.database

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.nio.ByteBuffer

open class item : RealmObject()  {
    @PrimaryKey
    var id : Long = 0 //判別ID
    var category : String = "" //画像のカテゴリ
    var image : ByteArray = byteArrayOf() //画像の名前
    var detail : String = "" //画像の詳細
}