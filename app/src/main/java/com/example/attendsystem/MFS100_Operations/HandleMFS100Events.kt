package com.example.attendsystem.MFS100_Operations

import android.annotation.SuppressLint
import android.util.Log
import com.mantra.mfs100.MFS100
import com.mantra.mfs100.MFS100Event

object HandleMFS100Events:MFS100Event {

    @SuppressLint("StaticFieldLeak")
    var mfs100: MFS100?=null
    var  isInitialized = false


    init {
        mfs100 = MFS100(this)
        mfs100?.SetApplicationContext(MFS100Application.instance)
        isInitialized = false
        Log.d("HandleMFS100Events","in HandleMFS100Events")
    }

    private fun initScanner() {
        try {
            val ret = mfs100?.Init()
            if(ret!=0){
                Log.e("initScanner", mfs100?.GetErrorMsg(ret?:0).toString())
            }
            else{
                isInitialized = true
            }
        }catch (e:Exception){
            Log.e("initScanner",e.message.toString())
        }
    }
    override fun OnDeviceAttached(vid: Int, pid: Int, hasPermission: Boolean) {
        Log.d("OnDeviceAttached","in OnDeviceAttached")
        val ret: Int
        if (!hasPermission) {
            Log.e("OnDeviceAttached","Permission Denied")
            return
        }
        if (vid == 1204 || vid == 11279) {
            if (pid == 34323) {
                ret = mfs100?.LoadFirmware()!!
                if (ret != 0) {
                    Log.e("OnDeviceAttached",mfs100?.GetErrorMsg(ret).toString())

                } else {
                    Log.e("OnDeviceAttached","Load firmware success")
                }
            } else if (pid == 4101) {
                ret = mfs100?.Init()!!
                if (ret == 0) {
                    Log.e("OnDeviceAttached","WithoutKey")
                    isInitialized = true

                } else {
                    Log.e("OnDeviceAttached",mfs100?.GetErrorMsg(ret).toString())
                }
            }
        }
    }

    override fun OnDeviceDetached() {
        isInitialized=  false
        Log.e("OnDeviceDetached","in OnDeviceDetached")
    }

    override fun OnHostCheckFailed(p0: String?) {
        TODO("Not yet implemented")
    }

    override fun OnMFS100Preview(rawData: ByteArray?, p1: Int, p2: String?) {
        try{
            if(rawData!=null){
                Log.e("OnMFS100Preview","Captured SuccessFully")
            }
        }catch (e:Exception){
            Log.e("OnMFS100Preview",e.message.toString())
        }
    }
}

