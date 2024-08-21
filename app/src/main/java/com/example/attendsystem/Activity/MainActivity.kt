package com.example.attendsystem.Activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.attendsystem.MFS100_Operations.HandleMFS100Events
import com.example.attendsystem.MFS100_Operations.MFS100Application
import com.example.attendsystem.R
import com.example.attendsystem.RoomDB.OfficeData
import com.example.attendsystem.databinding.ActivityMainBinding
import com.mantra.mfs100.FingerData
import com.mantra.mfs100.MFS100
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var binding:ActivityMainBinding

    private lateinit var mfs100:MFS100

    private var lastCapture = FingerData()

    private var startTime:Long  = 0
    private var endTime :Long= 0;

    private var verificationSuccess:Boolean = false

    companion object{
        lateinit var officeData: OfficeData
    }

    private lateinit var empID:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Log.d("MainActivity", "onCreate()")

        mfs100 = HandleMFS100Events.mfs100!!

        binding.txtRegister.setOnClickListener(this)
        binding.btnVerify.setOnClickListener(this)
        binding.imgAdmin.setOnClickListener(this)

        officeData = OfficeData.getDatabase(applicationContext)

        binding.btnInitialize.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked){
                
            }
            else{ }
        }
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.btnVerify ->{
                inputEmpIdDialog()
            }
            R.id.txtRegister ->{
                val i = Intent(this, Register::class.java)
                startActivity(i)
            }
            R.id.imgAdmin ->{
                val i = Intent(this, AdminPage::class.java)
                startActivity(i)
            }
        }
    }

    fun startCapture(){
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val fingerData = FingerData()
                val ret =mfs100.AutoCapture(fingerData,10000,true)
                Log.e("startCapture",""+ret)
                if (ret!=0){
                    Log.e("startCapture",mfs100.GetErrorMsg(ret))
                }else{
                    lastCapture = fingerData
                    Log.d("fingerData",lastCapture.ISOTemplate().toString())
                    fetchEmployeeData(fingerData.ISOTemplate())
                }
            }
            catch (e:java.lang.Exception){
                Log.e("startCapture",e.message.toString())
            }
        }
    }

    private suspend fun CoroutineScope.fetchEmployeeData(verifyTemplate:ByteArray) {
        startTime = System.currentTimeMillis()
        launch {
            try {
                val employeData = withContext(Dispatchers.IO) { officeData.employeeDao().getEmployee(empID)}
                verificationSuccess = false;
                matchISO(employeData.Index_ISO_Temp, verifyTemplate)
                matchISO(employeData.Thumb_ISO_Temp, verifyTemplate)
                if (verificationSuccess) {
                    showWelcomeDialog(employeData.name)
                }
                else{
                    showFailureDialog()
                }
            }catch (e:Exception){
                Log.e("fetchEmployeeData",e.message.toString())
            }
        }
    }

    private fun matchISO(Enroll_Template:ByteArray,Verify_Template:ByteArray) {


        if (verificationSuccess) return
        val ret = mfs100.MatchISO(Enroll_Template, Verify_Template)
        if (ret < 0) {
            Log.e("matchISO",mfs100.GetErrorMsg(ret))
        } else {
            if (ret >= 96) {
                Log.e("matchISO","Finger matched with score: $ret")
                verificationSuccess = true
            } else {
                Log.e("matchISO","Finger not matched, score: $ret")
            }
        }
    }

    private fun inputEmpIdDialog(){
        val inputEditText = EditText(this)
        val alertDialogBuilder = AlertDialog.Builder(this)

        alertDialogBuilder.setTitle("Enter Your EmpID")
        alertDialogBuilder.setView(inputEditText)
        alertDialogBuilder.setCancelable(true)
        alertDialogBuilder.setPositiveButton("Confirm"){_,_->
            empID = inputEditText.text.toString()
            if (!empID.isEmpty()){
                startCapture()
            }
        }

        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()

    }

    fun showWelcomeDialog(employeeName: String) {
        endTime = System.currentTimeMillis()
        val totalTime = (endTime-startTime).toDouble()/1000

        Log.d("TotalTime",totalTime.toString())
        val alertDialogBuilder = AlertDialog.Builder(this@MainActivity)
        alertDialogBuilder.apply {
            setTitle("Welcome")
            setMessage("Welcome $employeeName")
            setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
        }
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    private fun showFailureDialog() {
        val alertDialogBuilder = AlertDialog.Builder(this@MainActivity)
        alertDialogBuilder.apply {
            setTitle("Verification Failed")
            setMessage("No matching employee found.")
            setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
        }
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    private fun showToast(message: String) {
        GlobalScope.launch(Dispatchers.Main) {
            Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show()
        }
    }


}