package com.example.attendsystem.Fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import com.example.attendsystem.Activity.AdminPage
import com.example.attendsystem.Activity.Register
import com.example.attendsystem.R
import com.example.attendsystem.RoomDB.Employee
import com.example.attendsystem.RoomDB.OfficeData
import com.example.attendsystem.databinding.FragmentUpdateUserBinding
import com.mantra.mfs100.FingerData
import com.mantra.mfs100.MFS100
import com.mantra.mfs100.MFS100Event
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class FragmentUpdateUser : Fragment(), MFS100Event, View.OnClickListener, View.OnLongClickListener {

    private var verificationSuccess:Boolean = false

    private lateinit var mfs100: MFS100

    private lateinit var employeeBundle:Employee

    private lateinit var Index_Finger_Img:ByteArray
    private lateinit var Index_ISO_Temp:ByteArray
    private lateinit var Thumb_Finger_Img:ByteArray
    private lateinit var Thumb_ISO_Temp:ByteArray
    private lateinit var alertDialog:AlertDialog

    private var callForUpdate = false
    private lateinit var binding:FragmentUpdateUserBinding



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
       binding = FragmentUpdateUserBinding.inflate(inflater,container,false)
        val v= binding.root

         employeeBundle = arguments?.getSerializable("employee") as Employee

        mfs100 = MFS100(this)
        mfs100.SetApplicationContext(requireActivity())

        Register.officeData = OfficeData.getDatabase(requireContext())

        binding.fragmentUpdateBtn.setOnClickListener(this)

        binding.imgUpdateIndexFinger.setOnLongClickListener(this)
        binding.imgUpdateThumb.setOnLongClickListener(this)


        captureVerify()

        return v
    }

    private fun captureVerify(){
//        showDialog("Verify User","Please place any one of your registered finger on scanner",
//            onCaptureClicked = {
                lifecycleScope.launch {
                    try {
                        delay(1000)
                        val indexDeferred = async {
                            captureBio("Verify")
                        }
                        val index_fingerData = indexDeferred.await()
                        if (index_fingerData.FingerImage()
                                .isEmpty() || index_fingerData.FingerImage() == null || index_fingerData.ISOTemplate() == null
                        ) {
                            postAlertDialog("Error", "Failed to capture index finger data",
                                onTryAgainClicked = { captureVerify() }, // Call handleCapture() again when OK is clicked
                                onCancelClicked = {  } // Do nothing when Cancel is clicked
                            )
                            alertDialog.dismiss()
                        } else {
                            verifyEmp(index_fingerData)
                        }
                    } catch (e: Exception) {
                        Log.e("handleIndexCapture", e.message.toString())
                    }
                }
//            }, onCancelClicked = {
//
//            })
    }

    private fun verifyEmp(index_fingerData:FingerData){
        alertDialog.dismiss()
        try {
                verificationSuccess = false
                matchISO(index_fingerData.ISOTemplate(), employeeBundle.Index_ISO_Temp)
                matchISO(index_fingerData.ISOTemplate(), employeeBundle.Thumb_ISO_Temp)
                if (verificationSuccess) {
                    handleIndexCapture()
                }else{
                    postAlertDialog("Error", "verification failed",
                        onTryAgainClicked = { captureVerify() }, // Call handleCapture() again when OK is clicked
                        onCancelClicked = {  } // Do nothing when Cancel is clicked
                    )
                    alertDialog.dismiss()
                }

        } catch (e: Exception) {
            Log.e("fetchEmployeeData", e.message.toString())
        }
    }

    private  fun handleIndexCapture() {
//        showDialog("Give Biometric of Index Finger","Please place your Index finger on scanner",
//            onCaptureClicked = {
        alertDialog.dismiss()
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
                                onCancelClicked = {} // Do nothing when Cancel is clicked
                            )
                        } else {
                            Index_Finger_Img = index_fingerData.FingerImage()
                            Index_ISO_Temp = index_fingerData.ISOTemplate()

                            val bitmap = BitmapFactory.decodeByteArray(Index_Finger_Img, 0, Index_Finger_Img.size)
                            binding.imgUpdateIndexFinger.setImageBitmap(bitmap)
                            alertDialog.dismiss()

                            if (!callForUpdate) {
                                handleThumbCapture()
                            }

//                            showImageDialog(Index_Finger_Img,
//                                onConfirmClicked = {
//                                    handleThumbCapture()
//                                }, onRecaptureClicked = {
//                                    handleIndexCapture()
//                                }
//                                )
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
//        showDialog("Give Biometric of Thumb","Please place your Thumb on scanner",
//            onCaptureClicked = {
        alertDialog.dismiss()
                lifecycleScope.launch {
                    try {
                        delay(2000)
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
                                },
                                onCancelClicked = {
                                } // Do nothing when Cancel is clicked
                            )
                        } else {
                            Thumb_Finger_Img = thumb_fingerData.FingerImage()
                            Thumb_ISO_Temp = thumb_fingerData.ISOTemplate()

//                            showImageDialog(Thumb_Finger_Img,
//                                onConfirmClicked = {
//                                    lifecycleScope.launch {fingerUniqness()}
//                                }, onRecaptureClicked = {
//                                    handleThumbCapture()
//                                }
//                                )

                            val bitmap = BitmapFactory.decodeByteArray(Thumb_Finger_Img, 0, Thumb_Finger_Img.size)
                            binding.imgUpdateThumb.setImageBitmap(bitmap)
                            alertDialog.dismiss()

                            if (!callForUpdate) {
                                fingerUniqness()
                            }


                        }
                    } catch (e: Exception) {
                        Log.e("handleThumbCapture", e.message.toString())
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
//                    updateData()
                    binding.fragmentUpdateBtn.text = "Confirm"
                    alertDialog.dismiss()
                } else {
                    postAlertDialog(
                        "Your finger biometrics are already in record",
                        "One or both of finger's biometrics are in record",
                        onTryAgainClicked = {handleIndexCapture() },
                        onCancelClicked = {}
                    )
                }
            }else{
                postAlertDialog(
                    "Same Biometrics",
                    "Use two diff fingers",
                    onTryAgainClicked = { handleIndexCapture()},
                    onCancelClicked = {}
                )
            }
        } else {
            Log.e("handleThumbCapture","dubasss")

        }
    }

    private suspend fun checkBiometrics(): Boolean {
        var result = false // Variable to store the result
        try {
            val employeeList = withContext(Dispatchers.IO) { Register.officeData.employeeDao().getAll()}
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

    private suspend fun updateData() {
        Register.officeData.employeeDao().updateBiometrics(Index_Finger_Img,Index_ISO_Temp,Thumb_Finger_Img,Thumb_ISO_Temp,employeeBundle.empID)

        val employeeList = Register.officeData.employeeDao().getAll()
        for(employee in employeeList){
            Log.d("saveData",employee.name+" "+employee.Index_Finger_Img)
        }
        val i = Intent(requireContext(), AdminPage::class.java)
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i)
        Toast.makeText(requireContext(),"Data updated",Toast.LENGTH_SHORT).show()
    }

    @SuppressLint("SuspiciousIndentation")
    private suspend fun captureBio(fingerType:String): FingerData {
        requireActivity().runOnUiThread {
            showDialog("Give Biometric of $fingerType", "Please place your $fingerType on scanner")
        }
        return withContext(Dispatchers.IO) {
            val fingerData = FingerData()
            try {
                val ret = mfs100.AutoCapture(fingerData, 10000, true)
                Log.e("startCapture", "" + ret)
                if (ret != 0) {
                    Log.e("startCapture", mfs100.GetErrorMsg(ret))
                    requireActivity().runOnUiThread {
                        alertDialog.dismiss()
                        postAlertDialog("Capture failed",
                            "Not a proper placement of fingers",
                            onTryAgainClicked = {
                                if (fingerType.equals("Index")) {
                                    handleIndexCapture()
                                }
                                else if (fingerType.equals("Verify")){
                                    captureVerify()
                                }
                                else {
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

    private fun showDialog(title:String,message:String) :AlertDialog{
        val alertDialogBuilder = AlertDialog.Builder(requireContext())
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
        val alertDialogBuilder = AlertDialog.Builder(requireContext())
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


    override fun OnDeviceAttached(vid: Int, pid: Int, hasPermission: Boolean) {
            val ret: Int
            if (!hasPermission) {
                Log.e("OnDeviceAttached", "Permission Denied")
                return
            }
            if (vid == 1204 || vid == 11279) {
                if (pid == 34323) {
                    ret = mfs100.LoadFirmware()
                    if (ret != 0) {
                        Log.e("OnDeviceAttached", mfs100.GetErrorMsg(ret))
                    } else {
                        Log.e("OnDeviceAttached", "Load firmware success")
                    }
                } else if (pid == 4101) {
                    val key = "Without Key"
                    ret = mfs100.Init()
                    if (ret == 0) {
                        Log.e("OnDeviceAttached", "WithoutKey")
                    } else {
                        Log.e("OnDeviceAttached", mfs100.GetErrorMsg(ret))
                    }
                }
            }
    }

    override fun OnDeviceDetached() {
        TODO("Not yet implemented")
    }

    override fun OnHostCheckFailed(p0: String?) {
        TODO("Not yet implemented")
    }

    override fun OnMFS100Preview(rawData: ByteArray?, quality: Int, previewInfo: String?) {
        try{
            if(rawData!=null){
                Toast.makeText(requireContext(),"Captured SuccessFully", Toast.LENGTH_SHORT).show()
            }
        }catch (e:Exception){
            Log.e("OnMFS100Preview",e.message.toString())
        }
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.fragmentUpdateBtn ->{
                if (binding.fragmentUpdateBtn.text.equals("Update") &&!callForUpdate){
                    lifecycleScope.launch {
                        updateData()
                    }
                }
                else if(binding.fragmentUpdateBtn.text.equals("Update") && callForUpdate){
                    lifecycleScope.launch {
                        fingerUniqness()
                    }
                }
                else{
                    lifecycleScope.launch {
                        updateData()
                    }
                }
            }
        }
    }

    private fun showUpdateBioDialog(fingerType:String):AlertDialog{

        val alertDialogBuilder = AlertDialog.Builder(requireContext())
        alertDialogBuilder.setPositiveButton("Confirm"){ _,_->
        }
        alertDialogBuilder.setNegativeButton("Recapture"){_,_->
            callForUpdate = true
            binding.fragmentUpdateBtn.text = "Update"
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
            R.id.imgUpdateIndexFinger ->{
                showUpdateBioDialog("Index")
            }
            R.id.imgUpdateThumb ->{
                showUpdateBioDialog("Thumb")
            }
        }
        return false
    }
}