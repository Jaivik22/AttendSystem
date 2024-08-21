package com.example.attendsystem.Fragment

import android.annotation.SuppressLint
import android.content.Intent
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
import com.mantra.mfs100.FingerData
import com.mantra.mfs100.MFS100
import com.mantra.mfs100.MFS100Event
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class FragmentDeleteUser : Fragment(), MFS100Event {

    private var verificationSuccess:Boolean = false

    private lateinit var mfs100: MFS100

    private lateinit var employeeBundle: Employee

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
       val v = inflater.inflate(R.layout.fragment_delete_user, container, false)

        employeeBundle = arguments?.getSerializable("employee") as Employee

        mfs100 = MFS100(this)
        mfs100.SetApplicationContext(requireActivity())

        Register.officeData = OfficeData.getDatabase(requireContext())
        captureVerify()
        return v
    }

    private fun captureVerify(){
        showDialog("Verify User","Please place any one of your registered finger on scanner",
            onCaptureClicked = {
                lifecycleScope.launch {
                    try {
                        delay(1000)
                        val indexDeferred = async {
                            captureBio()
                        }
                        val index_fingerData = indexDeferred.await()
                        if (index_fingerData.FingerImage()
                                .isEmpty() || index_fingerData.FingerImage() == null || index_fingerData.ISOTemplate() == null
                        ) {
                            postAlertDialog("Error", "Failed to capture index finger data",
                                onTryAgainClicked = { captureVerify() }, // Call handleCapture() again when OK is clicked
                                onCancelClicked = {  } // Do nothing when Cancel is clicked
                            )
                        } else {
                            verifyEmp(index_fingerData)
                        }
                    }catch (e:Exception){
                        Log.e("handleIndexCapture",e.message.toString())
                    }
                }
            }, onCancelClicked = {

            })
    }

    private fun verifyEmp(index_fingerData: FingerData){
        try {
            verificationSuccess = false
            matchISO(index_fingerData.ISOTemplate(), employeeBundle.Index_ISO_Temp)
            matchISO(index_fingerData.ISOTemplate(), employeeBundle.Thumb_ISO_Temp)
            if (verificationSuccess) {
                deleteUser()
            }else{
                postAlertDialog("Error", "verification failed",
                    onTryAgainClicked = { captureVerify() }, // Call handleCapture() again when OK is clicked
                    onCancelClicked = {  } // Do nothing when Cancel is clicked
                )
            }

        } catch (e: Exception) {
            Log.e("fetchEmployeeData", e.message.toString())
        }
    }

    private fun deleteUser(){
        lifecycleScope.launch {
            Register.officeData.employeeDao().deleteEmployee(employeeBundle)
            val i = Intent(requireContext(), AdminPage::class.java)
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i)
            Toast.makeText(requireContext(),"User Deleted", Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("SuspiciousIndentation")
    private suspend fun captureBio(): FingerData {
        return withContext(Dispatchers.IO) {
            val fingerData = FingerData()
            try {
                val ret = mfs100.AutoCapture(fingerData, 10000, true)
                Log.e("startCapture", "" + ret)
                if (ret != 0) {
                    Log.e("startCapture", mfs100.GetErrorMsg(ret))
                    postAlertDialog("Error", mfs100.GetErrorMsg(ret),
                        onTryAgainClicked = { captureVerify() }, // Call handleCapture() again when OK is clicked
                        onCancelClicked = { } // Do nothing when Cancel is clicked
                    )
                } else {
                    Log.d("fingerData", fingerData.ISOTemplate().toString())
                }
            } catch (e: java.lang.Exception) {
                Log.e("startCapture", e.message.toString())
                postAlertDialog("Error", e.message.toString(),
                    onTryAgainClicked = { captureVerify() }, // Call handleCapture() again when OK is clicked
                    onCancelClicked = {  } // Do nothing when Cancel is clicked
                )
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

    private fun showDialog(title:String,message:String, onCaptureClicked: () -> Unit,
                           onCancelClicked: () -> Unit) :AlertDialog{
        val alertDialogBuilder = AlertDialog.Builder(requireContext())
        alertDialogBuilder.setTitle("$title")
        alertDialogBuilder.setMessage("$message")
        alertDialogBuilder.setPositiveButton("Capture"){_,_->
            onCaptureClicked()
        }
        alertDialogBuilder.setNegativeButton("Cancle"){_,_->
            onCancelClicked()
        }
        val alertDialog = alertDialogBuilder.create()
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
}