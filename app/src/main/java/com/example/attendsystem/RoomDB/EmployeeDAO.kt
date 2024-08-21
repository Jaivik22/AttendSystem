package com.example.attendsystem.RoomDB

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.attendsystem.DataClass.EmployeeIdAndName

@Dao
interface EmployeeDAO {

    @Query("SELECT * FROM employees")
    suspend fun getAll():List<Employee>

    @Query("SELECT Index_ISO_Temp FROM employees WHERE empID = :empID")
    suspend fun getIndexISO(empID: String): ByteArray

    @Query("SELECT Thumb_ISO_Temp FROM employees WHERE empID = :empID")
    suspend fun getThumbISO(empID: String): ByteArray

    @Query("SELECT empID FROM employees")
    suspend fun getempIDs():List<String>

    @Insert
    suspend fun insertEmployee(employee: Employee)

    @Query("DELETE FROM employees")
    suspend fun deleteAllEmployees()

    @Delete
    suspend fun deleteEmployee(employee: Employee)

    @Query("SELECT empID, name FROM employees")
    suspend fun getEmpIdAndName(): List<EmployeeIdAndName>

    @Query("UPDATE employees SET Index_Finger_Img = :Index_Finger_Img, Index_ISO_Temp = :Index_ISO_Temp,Thumb_Finger_Img=:Thumb_Finger_Img,Thumb_ISO_Temp=:Thumb_ISO_Temp WHERE empID = :empID")
    suspend fun updateBiometrics(Index_Finger_Img: ByteArray, Index_ISO_Temp: ByteArray,Thumb_Finger_Img:ByteArray,Thumb_ISO_Temp:ByteArray, empID: String)

    @Query("SELECT * FROM employees WHERE empID= :empID")
    suspend fun getEmployee(empID: String):Employee

}