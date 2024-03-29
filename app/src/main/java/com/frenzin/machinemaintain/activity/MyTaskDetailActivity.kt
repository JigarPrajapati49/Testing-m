package com.frenzin.machinemaintain.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.frenzin.machinemaintain.R
import com.frenzin.machinemaintain.adapter.ImageAdapter
import com.frenzin.machinemaintain.base.BaseActivity
import com.frenzin.machinemaintain.databinding.ActivityMyTaskDetailBinding
import com.frenzin.machinemaintain.model.resModel.GetMyTickets
import com.frenzin.machinemaintain.utills.Constant
import com.frenzin.machinemaintain.utills.ImageUtils
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class MyTaskDetailActivity : BaseActivity(),ImageAdapter.RemoveImage {

    private lateinit var binding: ActivityMyTaskDetailBinding
    private lateinit var uri: Uri
    private var TAG = "MyTaskDetailActivity"
    private var mediaFile: File? = null
    private var imagePath = ""
    private var ticketId = ""
    private var phoneNumber = ""
    private var selectMultipleImage = true
    private var imageList = ArrayList<String>()
    private var imageAdapter: ImageAdapter? = null

    @SuppressLint("SimpleDateFormat")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyTaskDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        intent.extras?.let {
            binding.txtCustomerName.text = it.getString(Constant.CUSTOMER_NAME).toString()
            binding.txtTicketNumber.text = "#${it.getString(Constant.TICKET_NUMBER).toString()}"
            binding.txtMachineName.text = it.getString(Constant.MACHINE_NAME).toString()
            binding.btnProgress.text = it.getString(Constant.STATUS).toString()
            binding.txtTime.text = convertDateTimeToTime(it.getString(Constant.CREATED_DATE).toString())
            val date = SimpleDateFormat("yyyy-MM-dd").parse(it.getString(Constant.INVERT_DATE).toString())
            binding.txtDate.text = SimpleDateFormat("dd MMM yyyy").format(date!!)
            ticketId = it.getString(Constant.TICKET_ID).toString()
            phoneNumber = it.getString(Constant.MOBILE_NUMBER).toString()
        }

        initView()

        binding.imgAdd.setOnClickListener {
            showCameraImageDialog()
        }

        binding.imgBack.setOnClickListener {
            finish()
        }

        binding.btnSubmit.setOnClickListener {
            changeOrAddTicketLogApiCall()
        }

        binding.imgCall.setOnClickListener {
            val callIntent = Intent(Intent.ACTION_DIAL)
            callIntent.data = Uri.parse("tel:$phoneNumber")
            startActivity(callIntent)
        }
    }

    private fun showCameraImageDialog() {

        val dialog = Dialog(this)

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_for_camera_gallery)
        dialog.setCanceledOnTouchOutside(true)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val llGallery = dialog.findViewById<LinearLayout>(R.id.llGallery)
        val llCamera = dialog.findViewById<LinearLayout>(R.id.llCamera)

        llGallery.setOnClickListener {
            galleryIntent()
            dialog.dismiss()
        }

        llCamera.setOnClickListener {
            cameraIntent()
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun cameraIntent() {
        if (ImageUtils.hasPermission(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE))) {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

            mediaFile = ImageUtils.getOutputMediaFile()
            uri = if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                FileProvider.getUriForFile(
                    this, this.packageName + ".provider",
                    mediaFile!!
                )
            } else {
                Uri.fromFile(mediaFile)
            }
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
            startActivityForResult(intent, BookActivity.IMAGE_CAPTURE)
        } else {
            requestPermissions(
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ), 1
            )
        }
    }

    private fun galleryIntent() {
        if (ImageUtils.hasPermission(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE))) {
            val galleryIntent = Intent()
            galleryIntent.action = Intent.ACTION_GET_CONTENT
            galleryIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
            if (selectMultipleImage) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                    galleryIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                }
            }
            startActivityForResult(
                Intent.createChooser(galleryIntent, "Select Picture"),
                BookActivity.IMAGE_GALLERY
            )

        } else {
            requestPermissions(
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ), 2
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            1 -> if (ImageUtils.hasPermission(
                    this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                )
            ) cameraIntent()
            2 -> if (ImageUtils.hasPermission(
                    this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                )
            ) galleryIntent()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == BookActivity.IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {

            imagePath = mediaFile!!.absolutePath
            if (!TextUtils.isEmpty(imagePath)) {
                imageList.add(uri.toString())
                imageAdapter?.setList(imageList)
            } else {
                showSnackBar(binding.root,"Can't get image")
            }

        }

        if (requestCode == BookActivity.IMAGE_GALLERY && resultCode == Activity.RESULT_OK && data != null) {
            data.let { intent ->

                if (intent.clipData != null) {
                    val count = intent.clipData!!.itemCount
                    for (i in 0 until count) {
                        val imageUri = intent.clipData!!.getItemAt(i).uri
                        imageList.add(imageUri.toString())
                    }
                    imageAdapter?.setList(imageList)

                } else if (intent.data != null) {
                    val imageUri = intent.data
                    imageList.add(imageUri.toString())
                    imageAdapter?.setList(imageList)
                } else {
                    Log.e(TAG, "onActivityResult: No DATA OR URL FOUND")
                }
            }
        }
    }

    private fun initView(){
        imageAdapter = ImageAdapter(this)
        imageAdapter!!.setRemover(this)
        binding.rvImages.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rvImages.adapter = imageAdapter
    }

    override fun removeImage(position: Int) {
        imageList.removeAt(position)
        imageAdapter!!.setList(imageList)
    }
    private fun changeOrAddTicketLogApiCall(){

        val ticketID = RequestBody.create("multipart/form-data".toMediaTypeOrNull(), ticketId)
        val status = RequestBody.create("multipart/form-data".toMediaTypeOrNull(), "completed")
        val remarks = RequestBody.create("multipart/form-data".toMediaTypeOrNull(), binding.edtRemarks.text.toString())
        val addComplaintImages : ArrayList<MultipartBody.Part> = ArrayList()

        for (i in 0 until imageList.size) {
            try {
                if (imageList[i].isEmpty()) {
                    addComplaintImages.add(MultipartBody.Part.createFormData("files[]", null.toString()))
                } else {
                    val fileDir = applicationContext.filesDir
                    val file = File(fileDir, "image.jpg")
                    val inputStream = contentResolver.openInputStream(Uri.parse(imageList[i]))
                    val outputStream = FileOutputStream(file)
                    inputStream!!.copyTo(outputStream)

                    val requestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
                    addComplaintImages.add(MultipartBody.Part.createFormData("files[]", file.name, requestBody))
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        Log.e(TAG, "changeOrAddTicketLogApiCall : API REQUEST ticketID ${ticketID.printRequestBody()}")
        Log.e(TAG, "changeOrAddTicketLogApiCall : API REQUEST status ${status.printRequestBody()}")
        Log.e(TAG, "changeOrAddTicketLogApiCall : API REQUEST remarks ${remarks.printRequestBody()}")
        Log.e(TAG, "changeOrAddTicketLogApiCall : API REQUEST addComplaintImages ${addComplaintImages.size}")

        if (isOnline()) {
            showLoading("Please wait...")
            val dispatchMachineReq = apiService?.changeOrAddTicketLog(ticketID,status,remarks,addComplaintImages)

            dispatchMachineReq?.enqueue(object : Callback<GetMyTickets?> {
                override fun onResponse(call: Call<GetMyTickets?>, response: Response<GetMyTickets?>) {
                    Log.e(TAG, "changeOrAddTicketLogApiCall onResponse : BODY " + Gson().toJson(response.body()))
                    Log.e(TAG, "changeOrAddTicketLogApiCall onResponse : CODE " + Gson().toJson(response.code()))
                    Log.e(TAG, "changeOrAddTicketLogApiCall : API REQUEST addBookDetailImage ${addComplaintImages.size}")
                    Log.e(TAG, "changeOrAddTicketLogApiCall : API REQUEST ticketID ${ticketID.printRequestBody()}")
                    hideLoading()
                    if (response.body() != null && response.code() == 200) {
                        Log.e(TAG, "changeOrAddTicketLogApiCall onResponse Successfully")
                        dialogBookedSuccessfully()
                    } else {
                        Log.e(TAG, "changeOrAddTicketLogApiCall onResponse Else Part")
                    }
                }

                override fun onFailure(call: Call<GetMyTickets?>, t: Throwable) {
                    t.printStackTrace()
                    hideLoading()
                    Log.e(TAG, "changeOrAddTicketLogApiCall onFailure: ${t.message}")
                    showSnackBar(binding.root, t.message.toString())
                }
            })
        }else{
            showSnackBar(binding.root,getString(R.string.msg_no_network))
        }
    }
    private fun dialogBookedSuccessfully() {
        val dialog = AlertDialog.Builder(this).create()
        val view = layoutInflater.inflate(R.layout.dialog_successful, null)

        view.apply {

            val btnComplete = findViewById<AppCompatButton>(R.id.btnComplete)
            val close = findViewById<AppCompatImageView>(R.id.imgCancel)
            val txtSuccessfully = findViewById<AppCompatTextView>(R.id.txtSuccessful)
            val txtDescription = findViewById<AppCompatTextView>(R.id.txtDescription)

            txtSuccessfully.text = context.getString(R.string.completed_successfully)
            txtDescription.visibility = View.GONE
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.setView(view)
            dialog.setCanceledOnTouchOutside(false)

            btnComplete.setOnClickListener {
                finish()
                dialog.dismiss()
            }

            close.setOnClickListener {
                dialog.dismiss()
            }

            dialog.show()

        }
    }

    fun convertDateTimeToTime(dateTime: String): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.getDefault())
        val outputFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val date = inputFormat.parse(dateTime)
        return outputFormat.format(date)
    }

}