package com.frenzin.machinemaintain.activity

import android.Manifest
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.FragmentManager
import com.budiyev.android.codescanner.*
import com.frenzin.machinemaintain.base.BaseActivity
import com.frenzin.machinemaintain.base.BaseActivity.IMAGE.getLastBitFromUrl
import com.frenzin.machinemaintain.databinding.ActivityQrCodeBinding
import com.frenzin.machinemaintain.fragments.HomeFragment
import com.frenzin.machinemaintain.model.resModel.MachineDetailByQr
import com.frenzin.machinemaintain.utills.PreferenceKeys
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import com.frenzin.machinemaintain.R
import com.frenzin.machinemaintain.utills.Constant
import com.frenzin.machinemaintain.utills.ImageUtils
import retrofit2.Response


class QrCodeActivity : BaseActivity() {

    private lateinit var binding: ActivityQrCodeBinding
    private lateinit var codeScanner: CodeScanner
    private var TAG = "QRCodeActivity"
    private var qrValue = ""
    private var isComeFromDispatch = ""
    private var forRaiseActivity = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQrCodeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)

        intent.extras?.let {
            isComeFromDispatch = it.getString(Constant.IS_COME_FORM_DISPATCH).toString()
            forRaiseActivity = it.getString(Constant.FOR_RAISE_ACTIVITY).toString()
        }

        codeScanner = CodeScanner(this, binding.scannerView)
        codeScanner.decodeCallback = DecodeCallback {
            runOnUiThread {
                if (it.text != null){
                    qrValue = getLastBitFromUrl(it.text).toString()
                    Log.e(TAG, "onCreate: qrValue is $qrValue")
                    bookMachineByQrApiCall(qrValue)
                }
            }
        }
        codeScanner.errorCallback = ErrorCallback {
            runOnUiThread {
                Toast.makeText(this, "Please allow to camera permission", Toast.LENGTH_LONG).show()
            }
        }
        showQrPreview()
    }

    private fun bookMachineByQrApiCall(qrValue : String){
        if (isOnline()) {
            showLoading("Please wait...")
            val machineDetailByQrReq = apiService?.getMachineDetailByQr(qrValue)
            machineDetailByQrReq?.enqueue(object : Callback<MachineDetailByQr?> {
                override fun onResponse(call: Call<MachineDetailByQr?>, response: Response<MachineDetailByQr?>) {
                    Log.e(TAG, "bookMachineByQrApiCall onResponse : BODY " + Gson().toJson(response.body()))
                    Log.e(TAG, "bookMachineByQrApiCall onResponse : CODE " + Gson().toJson(response.code()))
                    Log.e(TAG, "bookMachineByQrApiCall onResponse : MESSAGE " + Gson().toJson(response.message()))
                    Log.e(TAG, "bookMachineByQrApiCall onResponse : ERROR_BODY " + Gson().toJson(response.errorBody()))
                    hideLoading()
                    if (response.body() != null && response.code() == 200) {
                        if (forRaiseActivity == "isComeFromRaiseActivity"){
                            if (response.body()?.isBooked == true){
                                Log.e(TAG, "bookMachineByQrApiCall isComeFromRaiseActivity onResponse Successfully")
                                Log.e(TAG, "bookMachineByQrApiCall isComeFromRaiseActivity onResponse machine id ${response.body()?.machineDetail?.id}")
                                Bundle().let {
                                    it.putString(PreferenceKeys.MACHINE_ID_BY_SERIAL_NUMBER, response.body()?.machineDetail?.id.toString())
                                    it.putString(Constant.CUSTOMER_NAME,response.body()?.bookmachinedetail?.customerName)
                                    it.putString(Constant.ADDRESS,response.body()?.bookmachinedetail?.address)
                                    it.putString(Constant.MACHINE_NAME,response.body()?.machineDetail?.machineName)
                                    it.putString(Constant.INVERT_DATE,response.body()?.machineDetail?.invertDate)
                                    gotoActivity(RaiseComplaintActivity::class.java,it,false)
                                    finish()
                                }
                            } else {
                                notBookedDialog()
                            }
                        } else if (isComeFromDispatch == "isComeFromDispatch"){
                            Log.e(TAG, "bookMachineByQrApiCall isComeFromDispatch onResponse Successfully")
                            Log.e(TAG, "bookMachineByQrApiCall isComeFromDispatch onResponse machine id ${response.body()?.machineDetail?.id}")
                            if (response.body()?.isBooked == true){
                                Bundle().let {
                                    it.putString(PreferenceKeys.MACHINE_ID_BY_SERIAL_NUMBER, response.body()?.machineDetail?.id.toString())
                                    it.putString(Constant.MACHINE_NAME,response.body()?.machineDetail?.machineName)
                                    it.putString(Constant.MACHINE_SERIAL_NUMBER,response.body()?.machineDetail?.machineSerialNumber)
                                    it.putString(Constant.PI_NUMBER,response.body()?.bookmachinedetail?.piNumber)
                                    it.putString(Constant.DIF_NUMBER,response.body()?.bookmachinedetail?.difNumber)
                                    it.putString(Constant.E_WAY_NUMBER,response.body()?.bookmachinedetail?.ewayBillNumber)
                                    gotoActivity(DispatchActivity::class.java,it,false)
                                    finish()
                                }
                            } else {
                                notBookedDialog()
                            }
                        } else {
                            Log.e(TAG, "bookMachineByQrApiCall onResponse Successfully")
                            Log.e(TAG, "bookMachineByQrApiCall onResponse machine id ${response.body()?.machineDetail?.id}")
                            Log.e(TAG, "bookMachineByQrApiCall onResponse machineName ${response.body()?.machineDetail?.machineName}")
                            Log.e(TAG, "bookMachineByQrApiCall onResponse isBooked ${response.body()?.isBooked}")
                            Log.e(TAG, "bookMachineByQrApiCall onResponse isBooked ${response.body()?.bookmachinedetail?.customerName}")
                            if (response.body()?.isBooked == true){
                                Bundle().let {
                                    it.putString(Constant.IS_QR_BOOKED,"isBooked")
                                    it.putString(Constant.MACHINE_NAME,response.body()?.machineDetail?.machineSerialNumber)
                                    it.putString(Constant.CUSTOMER_NAME,response.body()?.bookmachinedetail?.customerName)
                                    it.putString(Constant.DATE_OF_BOOK,response.body()?.machineDetail?.invertDate)
                                    it.putString(Constant.MOBILE_NUMBER,response.body()?.bookmachinedetail?.mobilenumber)
                                    it.putString(Constant.TAX_INVOICE_NUMBER,response.body()?.bookmachinedetail?.taxInvoice)
                                    it.putString(Constant.LR_NUMBER,response.body()?.bookmachinedetail?.LrNumber)
                                    it.putString(Constant.PI_NUMBER,response.body()?.bookmachinedetail?.piNumber)
                                    it.putString(Constant.ADDRESS,response.body()?.bookmachinedetail?.address)
                                    it.putString(Constant.DIF_NUMBER,response.body()?.bookmachinedetail?.difNumber)
                                    it.putStringArrayList(Constant.MACHINE_IMAGES,response.body()?.machineImage)
                                    gotoActivity(BookedDetailActivity::class.java,it,false)
                                    finish()
                                }
                            } else {
                                Bundle().let {
                                    it.putString(PreferenceKeys.MACHINE_ID_BY_SERIAL_NUMBER, response.body()?.machineDetail?.id.toString())
                                    it.putString(Constant.IS_QR_NOT_BOOKED,"isNotBooked")
                                    it.putString(Constant.MACHINE_NAME,response.body()?.machineDetail?.machineSerialNumber)
                                    gotoActivity(BookActivity::class.java,it,false)
                                    finish()
                                }
                            }
                        }
                    } else {
                        hideLoading()
                        Log.e(TAG, "bookMachineByQrApiCall onResponse Else Part")
                    }
                }

                override fun onFailure(call: Call<MachineDetailByQr?>, t: Throwable) {
                    t.printStackTrace()
                    hideLoading()
                    Log.e(TAG, "bookMachineByQrApiCall onFailure: ${t.message}")
                    showSnackBar(binding.root, t.message.toString())
                }
            })

        } else {
            showSnackBar(binding.root,getString(R.string.msg_no_network))
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            1 -> if (hasPermission(
                    arrayOf(Manifest.permission.CAMERA)
                )
            ) showQrPreview()
        }
    }

    private fun showQrPreview(){
        if (hasPermission(arrayOf(Manifest.permission.CAMERA))) {
            codeScanner.startPreview()
        } else {
            requestPermissions(
                arrayOf(
                    Manifest.permission.CAMERA,
                ), 1
            )
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        gotoActivity(HomeActivity::class.java,null,true)
    }

    fun notBookedDialog(){
        val builder = AlertDialog.Builder(this)
        builder.apply {
            setMessage("This machine is not booked!")
            setTitle("FCMS")
            setCancelable(false)
            setPositiveButton("OK") {
                    _, _ -> gotoActivity(HomeActivity::class.java,null,true)
            }
        }
        builder.create().show()
    }
}