package com.example.attendsystem.Activity

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.attendsystem.R
import com.example.attendsystem.RoomDB.Employee
import com.example.attendsystem.RoomDB.OfficeData
import com.example.attendsystem.databinding.ActivityRegisterBinding
import com.mantra.mfs100.FingerData
import com.mantra.mfs100.MFS100
import com.mantra.mfs100.MFS100Event
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Register : AppCompatActivity(), View.OnClickListener, MFS100Event, View.OnLongClickListener {

    private lateinit var mfs100: MFS100

    private lateinit var binding:ActivityRegisterBinding

    private lateinit var Index_Finger_Img:ByteArray
    private lateinit var Index_ISO_Temp:ByteArray
    private lateinit var Thumb_Finger_Img:ByteArray
    private lateinit var Thumb_ISO_Temp:ByteArray
    private var empId:String = ""
    private var empName:String = ""
    private var callForUpdate = false

    private lateinit var alertDialog:AlertDialog

    private var isDeviceAttached = false
    companion object{
        lateinit var officeData: OfficeData
    }

    private var verificationSuccess:Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=  ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnRecord.setOnClickListener(this)

        mfs100 = MFS100(this)
        mfs100.SetApplicationContext(this)

        officeData = OfficeData.getDatabase(applicationContext)
        binding.registerInitializeBtn.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked){
                initScanner()
            }
            else{

            }
        }
        binding.imgIndexFinger.setOnLongClickListener(this)
        binding.imgThumb.setOnLongClickListener(this)

    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.btnRecord ->{

                if (binding.btnRecord.text.equals("Register") && !callForUpdate) {
                    empName = binding.etName.text.toString()
                    empId = binding.etEmpID.text.toString()
                    if (empName.isNotBlank() && empId.isNotBlank()) {
                        if (isDeviceAttached) {
                            lifecycleScope.launch {
                                if (!checkUniqueID()) {
                                    handleIndexCapture()
                                } else {
                                    binding.duplicateIDWarningTxt.visibility = View.VISIBLE
                                }
                            }
                        } else {
                            makeToast("Please Initialize device first")
                        }
                    } else {
                        makeToast("Fill Details first")
                    }
                }else if (binding.btnRecord.text.equals("Register") && callForUpdate) {
                    lifecycleScope.launch { fingerUniqness() }}
                else{
                    lifecycleScope.launch {
                        saveData()
                    }
                }
            }
            R.id.registerInitializeBtn ->{
                initScanner()
            }
        }
    }

    private  fun handleIndexCapture() {

//        showDialog("Give Biometric of Index Finger","Please place your Index finger on scanner",
//            onCaptureClicked = {
                lifecycleScope.launch {
                    try {
                        delay(1000)
                        val indexDeferred = async {
                            captureBio("Index")
                        }

                        val index_fingerData = indexDeferred.await()
                        if (index_fingerData.FingerImage()
                                .isEmpty() || index_fingerData.FingerImage() == null || index_fingerData.ISOTemplate() == null
                        ) {
                            postAlertDialog("Error", "Failed to capture index finger data",
                                onTryAgainClicked = { handleIndexCapture() }, // Call handleCapture() again when OK is clicked
                                onCancelClicked = {  } // Do nothing when Cancel is clicked
                            )
                        } else {
                            Index_Finger_Img = index_fingerData.FingerImage()
                            Index_ISO_Temp = index_fingerData.ISOTemplate()

//                            showImageDialog(Index_Finger_Img,
//                                onConfirmClicked = {
//                                    handleThumbCapture()
//                                }, onRecaptureClicked = {
//                                    handleIndexCapture()
//                                }
//                            )

                            binding.llIndexImg.visibility = View.VISIBLE
                            binding.imgIndexFinger.visibility= View.VISIBLE
                            val bitmap = BitmapFactory.decodeByteArray(Index_Finger_Img, 0, Index_Finger_Img.size)
                            binding.imgIndexFinger.setImageBitmap(bitmap)
                            alertDialog.dismiss()

                            if (!callForUpdate) {
                                handleThumbCapture()
                            }

                        }
                    } catch (e: Exception) {
                        Log.e("handleIndexCapture", e.message.toString())
                    }
                }
//            }, onCancelClicked = {
//
//            })
    }
    private fun handleThumbCapture(){
//        showDialog("Give Biometric of Thumb","Please place your thumb on scanner",
//            onCaptureClicked = {
                lifecycleScope.launch {
                    try {
                        delay(1000)
                        val thumbDeferred = async {
                            captureBio("Thumb")

                        }
                        val thumb_fingerData = thumbDeferred.await()
                        if (thumb_fingerData.FingerImage()
                                .isEmpty() || thumb_fingerData.FingerImage() == null || thumb_fingerData.ISOTemplate() == null
                        ) {
                            postAlertDialog("Error", "Failed to capture Thumb finger data",
                                onTryAgainClicked = {
                                    handleThumbCapture()
                                }, // Call handleCapture() again when OK is clicked
                                onCancelClicked = {  } // Do nothing when Cancel is clicked
                            )
                        } else {
                            Thumb_Finger_Img = thumb_fingerData.FingerImage()
                            Thumb_ISO_Temp = thumb_fingerData.ISOTemplate()

//                            showImageDialog(Thumb_Finger_Img,
//                                onConfirmClicked = {
//                                    lifecycleScope.launch{fingerUniqness() }
//
//                                }, onRecaptureClicked = {
//                                    handleThumbCapture()
//                                }
//                                )
                            binding.llThumbImg.visibility = View.VISIBLE
                            binding.imgThumb.visibility = View.VISIBLE
                            val bitmap = BitmapFactory.decodeByteArray(Thumb_Finger_Img, 0, Thumb_Finger_Img.size)
                            binding.imgThumb.setImageBitmap(bitmap)
                            alertDialog.dismiss()

                            if (!callForUpdate) {
                                fingerUniqness()
                            }
                        }
                    }catch (e:Exception){
                        Log.e("handleThumbCapture",e.message.toString())
                    }
                }
//            }, onCancelClicked = {
//
//            })
    }
    private suspend fun fingerUniqness(){
        if (Index_Finger_Img != null && Index_ISO_Temp != null && Thumb_Finger_Img != null && Thumb_ISO_Temp != null) {
            if(!bothSameFinger()) {
                if (!checkBiometrics()) {
                    binding.btnRecord.text = "Confirm"
                } else {
                    postAlertDialog(
                        "Your finger biometrics are already in record",
                        "One or both of finger's biometrics are in record",
                        onTryAgainClicked = { },
                        onCancelClicked = {}
                    )
                }
            }else{
                postAlertDialog(
                    "Same Biometrics",
                    "Use two diff fingers",
                    onTryAgainClicked = { },
                    onCancelClicked = {}
                )
            }
        } else {
            Log.e("handleThumbCapture","dubasss")

        }
    }

    @SuppressLint("SuspiciousIndentation")
    private suspend fun captureBio(fingerType:String):FingerData {
        runOnUiThread {
            showDialog("Give Biometric of $fingerType", "Please place your $fingerType on scanner")
        }
            return withContext(Dispatchers.IO) {
                val fingerData = FingerData()
                try {
                    val ret = mfs100.AutoCapture(fingerData, 10000, true)
                    Log.e("startCapture", "" + ret)
                    if (ret != 0) {
                        Log.e("startCapture", mfs100.GetErrorMsg(ret))
                        makeToast(mfs100.GetErrorMsg(ret).toString())
                        runOnUiThread {
                            alertDialog.dismiss()
                            postAlertDialog("Capture failed",
                                "Not a proper placement of fingers",
                                onTryAgainClicked = {
                                    if (fingerType.equals("Index")) {
                                        handleIndexCapture()
                                    } else {
                                        handleThumbCapture()
                                    }
                                },
                                onCancelClicked = {}
                            )
                        }
                    } else {
                        Log.d("fingerData", fingerData.ISOTemplate().toString())
                    }
                } catch (e: java.lang.Exception) {
                    Log.e("startCapture", e.message.toString())
                }
                fingerData
            }

    }
    private suspend fun checkBiometrics(): Boolean {
        var result = false // Variable to store the result
        try {
            val employeeList = withContext(Dispatchers.IO) { officeData.employeeDao().getAll()}
            for (employee in employeeList) {
                verificationSuccess = false
                matchISO(employee.Index_ISO_Temp, Index_ISO_Temp)
                matchISO(employee.Thumb_ISO_Temp, Index_ISO_Temp)
                matchISO(employee.Index_ISO_Temp, Thumb_ISO_Temp)
                matchISO(employee.Thumb_ISO_Temp, Thumb_ISO_Temp)

                if (verificationSuccess) {
                    result = true
                    break // Exit the loop if a match is found
                }
            }
        } catch (e: Exception) {
            Log.e("fetchEmployeeData", e.message.toString())
        }
        return result
    }

    private fun bothSameFinger():Boolean{
        val ret = mfs100.MatchISO(Index_ISO_Temp, Thumb_ISO_Temp)
        if (ret < 0) {
            Log.e("bothSameFinger",mfs100.GetErrorMsg(ret))
        } else {
            if (ret >= 96) {
                Log.e("bothSameFinger","Finger matched with score: $ret")
                return true
            } else {
                Log.e("bothSameFinger","Finger not matched, score: $ret")
            }
        }
        return false
    }

    private suspend fun checkUniqueID(): Boolean {
        var ismatching = false
        try {
            val empIdList = withContext(Dispatchers.IO) { officeData.employeeDao().getempIDs()}
            for (id in empIdList) {
                Log.d("empIds",id)

               if(empId.equals(id)){
                   ismatching = true
               }
            }
        } catch (e: Exception) {
            Log.e("fetchEmployeeData", e.message.toString())
        }
        return ismatching
    }

    private suspend fun saveData() {
        val temp_name = binding.etName.text.toString()
        val temp_empId = binding.etEmpID.text.toString()

        if(temp_name.equals(empName)&&temp_empId.equals(empId)) {

            val employee = Employee(
                empId,
                empName,
                Index_Finger_Img,
                Index_ISO_Temp,
                Thumb_Finger_Img,
                Thumb_ISO_Temp
            )
            officeData.employeeDao().insertEmployee(employee)

            val employeeList = officeData.employeeDao().getAll()
            for (employee in employeeList) {
                Log.d("saveData", employee.name + " " + employee.Index_Finger_Img)
            }
            val i = Intent(this, MainActivity::class.java)
            startActivity(i)
            Toast.makeText(this, "Successfull registration", Toast.LENGTH_SHORT).show()
        }else{
            makeToast("Alteration in name or empId")
        }
    }

    private fun initScanner() {
        try {
            val ret = mfs100.Init()
            if(ret!=0){
                Log.e("initScanner",mfs100.GetErrorMsg(ret))
                Toast.makeText(this,"Error in device Initalization \n Try Again",Toast.LENGTH_SHORT).show()
            }
            else{
                Toast.makeText(this,"Initialization successful ",Toast.LENGTH_SHORT).show()
                binding.registerInitializeBtn.text = "Initialized"
                binding.registerInitializeBtn.isChecked = true

            }
        }catch (e:Exception){
            Log.e("initScanner",e.message.toString())
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

    fun makeToast(message:String){
        lifecycleScope.launch {
            Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun OnDeviceAttached(vid: Int, pid: Int, hasPermission: Boolean) {
        val ret: Int
        if (!hasPermission) {
            Log.e("OnDeviceAttached","Permission Denied")
            return
        }
        if (vid == 1204 || vid == 11279) {
            if (pid == 34323) {
                ret = mfs100.LoadFirmware()
                if (ret != 0) {
                    Log.e("OnDeviceAttached",mfs100.GetErrorMsg(ret))
                } else {
                    Log.e("OnDeviceAttached","Load firmware success")
                    binding.registerInitializeBtn.text = "Initialized"
                    binding.registerInitializeBtn.isChecked = true
                    isDeviceAttached = true
                }
            } else if (pid == 4101) {
                val key = "Without Key"
                ret = mfs100.Init()
                if (ret == 0) {
                    Log.e("OnDeviceAttached","WithoutKey")
                    binding.registerInitializeBtn.text = "Initialized"
                    binding.registerInitializeBtn.isChecked = true
                    isDeviceAttached = true
                } else {
                    Log.e("OnDeviceAttached",mfs100.GetErrorMsg(ret))
                }
            }
        }
    }

    override fun OnDeviceDetached() {
        binding.registerInitializeBtn.text = "Initialize"
        binding.registerInitializeBtn.isChecked = true
        binding.registerInitializeBtn.setBackgroundColor(resources.getColor(R.color.Red))
    }

    override fun OnHostCheckFailed(p0: String?) {
        TODO("Not yet implemented")
    }

    override fun OnMFS100Preview(rawData: ByteArray?, quality: Int, previewInfo: String?) {
        try{
            if(rawData!=null){
                Toast.makeText(this,"Captured SuccessFully",Toast.LENGTH_SHORT).show()
            }
        }catch (e:Exception){
            Log.e("OnMFS100Preview",e.message.toString())
        }
    }

    private fun showDialog(title:String,message:String) :AlertDialog{
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle("$title")
        alertDialogBuilder.setMessage("$message")
//        alertDialogBuilder.setPositiveButton("Capture"){_,_->
//            onCaptureClicked()
//        }
//        alertDialogBuilder.setNegativeButton("Cancle"){_,_->
//            onCancelClicked()
//        }
        alertDialog = alertDialogBuilder.create()
        alertDialog.show()
        return alertDialog
    }
    private fun postAlertDialog(
        title: String,
        message: String,
        onTryAgainClicked: () -> Unit,
        onCancelClicked: () -> Unit
    ): AlertDialog {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle(title)
        alertDialogBuilder.setMessage(message)
        alertDialogBuilder.setPositiveButton("Try Again") { _, _ ->
            onTryAgainClicked()
        }
        alertDialogBuilder.setNegativeButton("Cancel") { _, _ ->
            onCancelClicked()
        }
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
        return alertDialog
    }

    private fun showUpdateBioDialog(fingerType:String):AlertDialog{


        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setPositiveButton("Confirm"){ _,_->
        }
        alertDialogBuilder.setNegativeButton("Recapture"){_,_->
            callForUpdate = true
            binding.btnRecord.text = "Register"
            if (fingerType.equals("Index")){
                handleIndexCapture()
            }
            else{
                handleThumbCapture()
            }
        }
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
        return alertDialog
    }

    override fun onLongClick(v: View?): Boolean {
        when(v?.id){
            R.id.imgIndexFinger ->{
                showUpdateBioDialog("Index")
            }
            R.id.imgThumb ->{
                showUpdateBioDialog("Thumb")
            }
        }
        return false
    }
}