package com.example.attendsystem.RoomDB

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "employees")
data class Employee(
    @PrimaryKey val empID:String,
    val name:String,
    @ColumnInfo(name = "Index_Finger_Img") val Index_Finger_Img:ByteArray,
    @ColumnInfo(name = "Index_ISO_Temp") val Index_ISO_Temp:ByteArray,
    @ColumnInfo(name = "Thumb_Finger_Img") val Thumb_Finger_Img:ByteArray,
    @ColumnInfo(name = "Thumb_ISO_Temp") val Thumb_ISO_Temp:ByteArray,
):Serializable