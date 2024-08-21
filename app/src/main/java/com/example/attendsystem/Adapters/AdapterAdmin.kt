package com.example.attendsystem.Adapters

import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.attendsystem.RoomDB.Employee

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.FragmentManager
import com.example.attendsystem.Fragment.FragmentDeleteUser
import com.example.attendsystem.Fragment.FragmentUpdateUser
import com.example.attendsystem.R

class AdapterAdmin(val context: Context, private val employeeList: List<Employee>,val fragmentManager: FragmentManager): RecyclerView.Adapter<AdapterAdmin.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_employee, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val employee = employeeList[position]
        holder.bind(employee)
    }

    override fun getItemCount(): Int {
        return employeeList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textViewEmpId: TextView = itemView.findViewById(R.id.textViewEmpId)
        private val textViewName: TextView = itemView.findViewById(R.id.textViewName)


        fun bind(employee: Employee) {
            textViewEmpId.text = "Employee ID: ${employee.empID}"
            textViewName.text = "Name: ${employee.name}"

            itemView.setOnLongClickListener {
                showOptionsDialog(employee)
                true
            }
        }

        private fun showOptionsDialog(employee: Employee) {
            val options = arrayOf("Update Biometrics", "Remove Employee")

            AlertDialog.Builder(context)
                .setTitle("Choose an Option")
                .setItems(options) { dialogInterface: DialogInterface, i: Int ->
                    // Handle option selection here
                    when (i) {
                        0 -> {
                            val b = Bundle().apply {
                                putSerializable("employee",employee)
                            }
                            val fragment = FragmentUpdateUser().apply {
                                arguments = b
                            }
                            fragmentManager.beginTransaction().apply {
                                replace(R.id.adminMain, fragment)
                                addToBackStack(null)
                                commit()
                            }

                        }
                        1 -> {
                            val b = Bundle().apply {
                                putSerializable("employee",employee)
                            }
                            val fragment = FragmentDeleteUser().apply {
                                arguments = b
                            }
                            fragmentManager.beginTransaction().apply {
                                replace(R.id.adminMain, fragment)
                                addToBackStack(null)
                                commit()
                            }
                        }
                    }
                    dialogInterface.dismiss()
                }
                .show()
        }

    }
}