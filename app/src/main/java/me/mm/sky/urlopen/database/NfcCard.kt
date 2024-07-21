package me.mm.sky.urlopen.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "nfc-card")
data class NfcCard(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    var name:String="None",
    var description:String="None",
    var url:String="https://www.baidu.com",
    var packageName:String="",
    var tags:List<String>

)
