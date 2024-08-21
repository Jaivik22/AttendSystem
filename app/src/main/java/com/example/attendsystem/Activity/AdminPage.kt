package com.example.attendsystem.Activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.attendsystem.Adapters.AdapterAdmin
import com.example.attendsystem.RoomDB.Employee
import com.example.attendsystem.RoomDB.OfficeData
import com.example.attendsystem.databinding.ActivityAdminPageBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class AdminPage : AppCompatActivity() {
    private lateinit var adapter: AdapterAdmin
    private lateinit var binding: ActivityAdminPageBinding

    private var employeeList = mutableListOf<Employee>()
    companion object{
        lateinit var officeData: OfficeData
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        officeData = OfficeData.getDatabase(applicationContext)

        binding.adminRV.layoutManager = LinearLayoutManager(this)
        val fragmentManager = supportFragmentManager
        adapter  = AdapterAdmin(this,employeeList,fragmentManager)
        binding.adminRV.adapter = adapter


    }

    private fun fetchEmployees() {

        GlobalScope.launch(Dispatchers.IO) {
            try {
                val empIdAndNameList = officeData.employeeDao().getAll()
                launch(Dispatchers.Main) {
                    employeeList.addAll(empIdAndNameList)
                    adapter.notifyDataSetChanged()
                }
            } catch (e: Exception) {
                Log.e("fetchEmployees", e.message.toString())
            }
        }
    }

    override fun onResume() {
        super.onResume()
        fetchEmployees()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val i = Intent(this, MainActivity::class.java)
        startActivity(i)
    }
}