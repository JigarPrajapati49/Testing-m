package com.frenzin.machinemaintain.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.navigation.fragment.findNavController
import com.frenzin.machinemaintain.R
import com.frenzin.machinemaintain.activity.*
import com.frenzin.machinemaintain.base.BaseActivity.Companion.apiService
import com.frenzin.machinemaintain.base.BaseFragment
import com.frenzin.machinemaintain.databinding.FragmentHomeBinding
import com.frenzin.machinemaintain.model.resModel.MachineDetailByNumber
import com.frenzin.machinemaintain.utills.Constant
import com.frenzin.machinemaintain.utills.PreferenceKeys
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class HomeFragment : BaseFragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var contextHome : Context
    private var TAG = "HomeFragment"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onAttach(context: Context) {
        contextHome = context
        super.onAttach(context)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.llInvert.setOnClickListener {
            gotoActivity(InvertActivity::class.java,null,false)
        }

        binding.txtUserName.setOnClickListener {
            showToast(appPref.getString(PreferenceKeys.USER_TOKEN))
        }

        binding.llBook.setOnClickListener {
            dialogForScanORManuallyForBook()
        }

        binding.imgScanQr.setOnClickListener {
            gotoActivity(QrCodeActivity::class.java,null,false)
        }

        binding.imgNotification.setOnClickListener {
            gotoActivity(RaiseComplaintActivity::class.java,null,false)
        }

        binding.llDispatch.setOnClickListener {
            dialogForScanORManuallyForDispatch()
        }
    }

    /** For Book start */
    private fun dialogForScanORManuallyForBook(){

        val dialog = Dialog(contextHome)

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_scan_or_manually)
        dialog.setCanceledOnTouchOutside(true)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val llScanQR = dialog.findViewById<LinearLayout>(R.id.llScanQR)
        val llManually = dialog.findViewById<LinearLayout>(R.id.llManually)
        val llSerialNumber = dialog.findViewById<LinearLayout>(R.id.llEnterSerialNumber)
        val imgCancel = dialog.findViewById<AppCompatImageView>(R.id.imgCancel)

        llScanQR.setOnClickListener {
            gotoActivity(QrCodeActivity::class.java, null, false)
            dialog.dismiss()
        }

        imgCancel.setOnClickListener {
            dialog.dismiss()
        }

        llManually.setOnClickListener {
            gotoActivity(MachinesInStockActivity::class.java,null,false)
            dialog.dismiss()
        }

        llSerialNumber.setOnClickListener {
            showSerialNumberDialog()
            dialog.dismiss()
        }

        dialog.show()
    }
    private fun showSerialNumberDialog(){
        val dialog = AlertDialog.Builder(contextHome).create()
        val view = layoutInflater.inflate(R.layout.dialog_serial_number, null)

        view.apply {
            val edtSerialNumber = view.findViewById<TextInputEditText>(R.id.edtSerialNumber)
            val btnNext = view.findViewById<AppCompatButton>(R.id.btnNext)
            val imgCancel = view.findViewById<AppCompatImageView>(R.id.imgCancel)

            btnNext.setOnClickListener {
                if (edtSerialNumber.text.toString().isEmpty()){
                    showSnackBar(binding.root,"Please Enter Serial Number")
                } else {
                    machineDetailByNumberApiCall(edtSerialNumber.text.toString())
                    dialog.dismiss()
                }
            }

            imgCancel.setOnClickListener {
                dialog.dismiss()
            }

            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.setView(view)
            dialog.setCanceledOnTouchOutside(false)
            dialog.show()
        }
    }
    private fun machineDetailByNumberApiCall(serialNumber : String){
        if (isOnline()) {
            showLoading("Please wait...")
            val machineDetailByQrReq = apiService?.machineDetailByNumber(serialNumber)

            machineDetailByQrReq?.enqueue(object : Callback<MachineDetailByNumber?>{
                override fun onResponse(
                    call: Call<MachineDetailByNumber?>,
                    response: Response<MachineDetailByNumber?>
                ) {
                    Log.e(TAG, "machineDetailByNumberApiCall onResponse : BODY " + Gson().toJson(response.body()))
                    Log.e(TAG, "machineDetailByNumberApiCall onResponse : CODE " + Gson().toJson(response.code()))
                    Log.e(TAG, "machineDetailByNumberApiCall onResponse : MESSAGE " + Gson().toJson(response.message()))
                    Log.e(TAG, "machineDetailByNumberApiCall onResponse : ERROR_BODY " + Gson().toJson(response.errorBody()))
                    hideLoading()
                    if (response.body() != null && response.code() == 200) {

                        Log.e(TAG, "machineDetailByNumberApiCall onResponse Successfully")
                        Log.e(TAG, "machineDetailByNumberApiCall onResponse machine id ${response.body()?.data?.id}")
                        Log.e(TAG, "machineDetailByNumberApiCall onResponse isBooked ${response.body()?.data?.isBooked}")

                        if (response.body()?.data?.isBooked == "true"){
                            gotoActivity(BookedDetailActivity::class.java,null,false)
                        } else {
                            Bundle().let {
                                it.putString(PreferenceKeys.MACHINE_ID_BY_SERIAL_NUMBER, response.body()?.data?.id.toString())
                                gotoActivity(BookActivity::class.java,it,false)
                            }
                        }

                    } else {
                        hideLoading()
                        Log.e(TAG, "machineDetailByNumberApiCall onResponse Else Part")
                    }
                }

                override fun onFailure(call: Call<MachineDetailByNumber?>, t: Throwable) {
                    t.printStackTrace()
                    hideLoading()
                    Log.e(TAG, "getMachineDetailApiCall onFailure: ${t.message}")
                    showSnackBar(binding.root, t.message.toString())
                }

            })
        } else {
            showSnackBar(binding.root,getString(R.string.msg_no_network))
        }
    }

    /** For Book end */


    /** For Dispatch start*/
    private fun dialogForScanORManuallyForDispatch(){

        val dialog = Dialog(contextHome)

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_scan_or_manually)
        dialog.setCanceledOnTouchOutside(true)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val llScanQR = dialog.findViewById<LinearLayout>(R.id.llScanQR)
        val llManually = dialog.findViewById<LinearLayout>(R.id.llManually)
        val llSerialNumber = dialog.findViewById<LinearLayout>(R.id.llEnterSerialNumber)
        val imgCancel = dialog.findViewById<AppCompatImageView>(R.id.imgCancel)

        llScanQR.setOnClickListener {
            Bundle().let {
                it.putString(Constant.IS_COME_FORM_DISPATCH,"isComeFromDispatch")
                gotoActivity(QrCodeActivity::class.java,it,false)
            }
            dialog.dismiss()
        }

        imgCancel.setOnClickListener {
            dialog.dismiss()
        }

        llManually.setOnClickListener {
            gotoActivity(DispatchInStockActivity::class.java,null,false)
            dialog.dismiss()
        }

        llSerialNumber.setOnClickListener {
            showSerialNumberDialogForDispatch()
            dialog.dismiss()
        }

        dialog.show()
    }
    private fun showSerialNumberDialogForDispatch(){
        val dialog = AlertDialog.Builder(contextHome).create()
        val view = layoutInflater.inflate(R.layout.dialog_serial_number, null)

        view.apply {
            val edtSerialNumber = view.findViewById<TextInputEditText>(R.id.edtSerialNumber)
            val btnNext = view.findViewById<AppCompatButton>(R.id.btnNext)
            val imgCancel = view.findViewById<AppCompatImageView>(R.id.imgCancel)

            btnNext.setOnClickListener {
                if (edtSerialNumber.text.toString().isEmpty()){
                    showSnackBar(binding.root,"Please Enter Serial Number")
                } else {
                    machineDetailByNumberForDispatchApiCall(edtSerialNumber.text.toString())
                    dialog.dismiss()
                }
            }

            imgCancel.setOnClickListener {
                dialog.dismiss()
            }

            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.setView(view)
            dialog.setCanceledOnTouchOutside(false)
            dialog.show()
        }
    }
    private fun machineDetailByNumberForDispatchApiCall(serialNumber : String){
        if (isOnline()) {
            showLoading("Please wait...")
            val machineDetailByQrReq = apiService?.machineDetailByNumber(serialNumber)

            machineDetailByQrReq?.enqueue(object : Callback<MachineDetailByNumber?>{
                override fun onResponse(
                    call: Call<MachineDetailByNumber?>,
                    response: Response<MachineDetailByNumber?>
                ) {
                    Log.e(TAG, "machineDetailByNumberForDispatchApiCall onResponse : BODY " + Gson().toJson(response.body()))
                    Log.e(TAG, "machineDetailByNumberForDispatchApiCall onResponse : CODE " + Gson().toJson(response.code()))
                    Log.e(TAG, "machineDetailByNumberForDispatchApiCall onResponse : MESSAGE " + Gson().toJson(response.message()))
                    Log.e(TAG, "machineDetailByNumberForDispatchApiCall onResponse : ERROR_BODY " + Gson().toJson(response.errorBody()))
                    hideLoading()
                    if (response.body() != null && response.code() == 200) {

                        Log.e(TAG, "machineDetailByNumberForDispatchApiCall onResponse Successfully")
                        Log.e(TAG, "machineDetailByNumberForDispatchApiCall onResponse machine id ${response.body()?.data?.id}")
                        Log.e(TAG, "machineDetailByNumberForDispatchApiCall onResponse isBooked ${response.body()?.data?.isBooked}")

                        if (response.body()?.data?.isBooked == "true"){
                            Bundle().let {
                                it.putString(PreferenceKeys.MACHINE_ID_BY_SERIAL_NUMBER, response.body()?.data?.id.toString())
                                it.putString(Constant.MACHINE_SERIAL_NUMBER, response.body()?.data?.machineSerialNumber.toString())
                                it.putString(Constant.MACHINE_NAME, response.body()?.data?.machineName.toString())
                                gotoActivity(DispatchActivity::class.java,it,false)
                            }
                        } else {
                            notBookedDialog()
                        }
                    } else {
                        hideLoading()
                        Log.e(TAG, "machineDetailByNumberApiCall onResponse Else Part")
                    }
                }

                override fun onFailure(call: Call<MachineDetailByNumber?>, t: Throwable) {
                    t.printStackTrace()
                    hideLoading()
                    Log.e(TAG, "getMachineDetailApiCall onFailure: ${t.message}")
                    showSnackBar(binding.root, t.message.toString())
                }

            })
        } else {
            showSnackBar(binding.root,getString(R.string.msg_no_network))
        }
    }

    /** For Dispatch end*/

    fun notBookedDialog(){
        val builder = AlertDialog.Builder(contextHome)
        builder.apply {
            setMessage("This machine is not booked!")
            setTitle("FCMS")
            setCancelable(false)
            setPositiveButton("OK") {
                    _, _ -> builder.create().dismiss()
            }
        }
        builder.create().show()
    }
}