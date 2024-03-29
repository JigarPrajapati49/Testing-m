package com.frenzin.machinemaintain.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
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
import com.frenzin.machinemaintain.base.BaseActivity
import com.frenzin.machinemaintain.base.BaseFragment
import com.frenzin.machinemaintain.databinding.FragmentScanQrBinding
import com.frenzin.machinemaintain.model.resModel.MachineDetailByNumber
import com.frenzin.machinemaintain.utills.Constant
import com.frenzin.machinemaintain.utills.PreferenceKeys
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ScanQrFragment : BaseFragment() {

    private lateinit var binding: FragmentScanQrBinding
    private lateinit var contextScan: Context
    private var TAG = "ScanQrFragment"

    override fun onAttach(context: Context) {
        super.onAttach(context)
        contextScan = context
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentScanQrBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialogForScanAndSerialNumber()
    }

    private fun dialogForScanAndSerialNumber(){

        val dialog = Dialog(contextScan)

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_scan_or_manually)
        dialog.setCanceledOnTouchOutside(true)
        dialog.setCancelable(false)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val llScanQR = dialog.findViewById<LinearLayout>(R.id.llScanQR)
        val llManually = dialog.findViewById<LinearLayout>(R.id.llManually)
        val llSerialNumber = dialog.findViewById<LinearLayout>(R.id.llEnterSerialNumber)
        val imgCancel = dialog.findViewById<AppCompatImageView>(R.id.imgCancel)

        llManually.visibility = View.GONE

        llScanQR.setOnClickListener {
            Bundle().run {
                this.putString(Constant.FOR_RAISE_ACTIVITY,"isComeFromRaiseActivity")
                gotoActivity(QrCodeActivity::class.java, this, false)
            }
            dialog.dismiss()
        }

        imgCancel.setOnClickListener {
            findNavController().navigate(R.id.homeFragment)
            dialog.dismiss()
        }

        llSerialNumber.setOnClickListener {
            showSerialNumberDialog()
            dialog.dismiss()
        }

        dialog.show()
    }
    private fun showSerialNumberDialog(){
        val dialog = AlertDialog.Builder(contextScan).create()
        val view = layoutInflater.inflate(R.layout.dialog_serial_number, null)

        view.apply {
            val edtSerialNumber = view.findViewById<TextInputEditText>(R.id.edtSerialNumber)
            val btnNext = view.findViewById<AppCompatButton>(R.id.btnNext)
            val imgCancel = view.findViewById<AppCompatImageView>(R.id.imgCancel)

            btnNext.setOnClickListener {
                if (edtSerialNumber.text.toString().isEmpty()){
                    showSnackBar(binding.root,"Please Enter Serial Number")
                } else {
                    dialog.dismiss()
                    machineDetailByNumberApiCall(edtSerialNumber.text.toString())
                }
            }

            imgCancel.setOnClickListener {
                dialog.dismiss()
                findNavController().navigate(R.id.homeFragment)
            }

            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.setView(view)
            dialog.setCanceledOnTouchOutside(false)
            dialog.setCancelable(false)
            dialog.show()
        }
    }

    private fun machineDetailByNumberApiCall(serialNumber : String){
        if (isOnline()) {
            showLoading("Please wait...")
            val machineDetailByQrReq = BaseActivity.apiService?.machineDetailByNumber(serialNumber)

            machineDetailByQrReq?.enqueue(object : Callback<MachineDetailByNumber?> {
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
                            Bundle().let {
                                it.putString(PreferenceKeys.MACHINE_ID_BY_SERIAL_NUMBER, response.body()?.data?.id.toString())
                                it.putString(Constant.INVERT_DATE, response.body()?.data?.invertDate.toString())
                                it.putString(Constant.MACHINE_NAME, response.body()?.data?.machineName.toString())
                                gotoActivity(RaiseComplaintActivity::class.java,it,false)
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
                    Log.e(TAG, "machineDetailByNumberApiCall onFailure: ${t.message}")
                    showSnackBar(binding.root, t.message.toString())
                }

            })
        } else {
            showSnackBar(binding.root,getString(R.string.msg_no_network))
        }
    }

    fun notBookedDialog(){
        val builder = AlertDialog.Builder(contextScan)
        builder.apply {
            setMessage("This machine is not booked!")
            setTitle("FCMS")
            setCancelable(false)
            setPositiveButton("OK") {
                    _, _ -> findNavController().navigate(R.id.homeFragment)
            }
        }
        builder.create().show()
    }
}
