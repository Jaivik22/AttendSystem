package com.example.attendsystem.RoomDB

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Employee::class], version = 1)
abstract class OfficeData: RoomDatabase() {
    abstract fun employeeDao():EmployeeDAO

companion object {
    @Volatile
    private var INSTANCE: OfficeData? = null

    fun getDatabase(context: Context): OfficeData {
        return INSTANCE ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                OfficeData::class.java,
                "OfficeData"
            ).build()
            INSTANCE = instance
            instance
        }
    }
}

}