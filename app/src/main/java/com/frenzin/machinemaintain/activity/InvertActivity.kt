package com.frenzin.machinemaintain.activity

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
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
import android.view.Window
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.frenzin.machinemaintain.R
import com.frenzin.machinemaintain.activity.BookActivity.Companion.IMAGE_CAPTURE
import com.frenzin.machinemaintain.activity.BookActivity.Companion.IMAGE_GALLERY
import com.frenzin.machinemaintain.adapter.ImageAdapter
import com.frenzin.machinemaintain.base.BaseActivity
import com.frenzin.machinemaintain.databinding.ActivityInvertBinding
import com.frenzin.machinemaintain.model.resModel.MachineDetail
import com.frenzin.machinemaintain.utills.AppPref
import com.frenzin.machinemaintain.utills.ImageUtils
import com.frenzin.machinemaintain.utills.MyApp
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.*
import java.util.*

class InvertActivity : BaseActivity(),ImageAdapter.RemoveImage {

    private lateinit var binding: ActivityInvertBinding
    private lateinit var uri: Uri
    private var mediaFile: File? = null
    private var imagePath = ""
    private var selectMultipleImage = true
    private val TAG = "BookActivity"
    private var imageList = ArrayList<String>()
    private var imageAdapter: ImageAdapter? = null
    private var selectedDateForApi = ""
    private var selectedDate = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInvertBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Log.e("Authorization", "Bearer " + MyApp.getMyApp()?.let { AppPref.getInstance(it).getToken() })
        initView()

        binding.btnSubmit.setOnClickListener {
            if (isValid()){
                addMachineDetailApiCall()
            }
        }

        binding.imgBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.imgAdd.setOnClickListener {
            showCameraImageDialog()
        }

        val year = Calendar.getInstance().get(Calendar.YEAR)
        val month = Calendar.getInstance().get(Calendar.MONTH)
        val day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        selectedDateForApi = /*"$day/$month/$year"*/ "$year/${month + 1}/$day"
        selectedDate = "$day/${month + 1}/$year"
        binding.tvDate.text = selectedDate

        binding.invertDate.setOnClickListener {
            val datePickerDialog = DatePickerDialog(
                this, { _, year, month, dayOfMonth ->
                    selectedDateForApi = /*"$dayOfMonth/${month}/$year"*/ "$year/${month + 1}/$dayOfMonth"
                    selectedDate = "$dayOfMonth/${month + 1}/$year"
                    binding.tvDate.text = selectedDate
                }, year, month, day
            )
            datePickerDialog.show()
        }
    }

    private fun showSubmitDialog(){
        val dialog = AlertDialog.Builder(this).create()
        val view = layoutInflater.inflate(R.layout.dialog_successful, null)

        view.apply {

            val btnComplete = findViewById<AppCompatButton>(R.id.btnComplete)
            val close = findViewById<AppCompatImageView>(R.id.imgCancel)

            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.setView(view)
            dialog.setCanceledOnTouchOutside(false)

            btnComplete.setOnClickListener {
                gotoActivity(HomeActivity::class.java, null,true)
                dialog.dismiss()
            }

            close.setOnClickListener {
                dialog.dismiss()
            }

            dialog.show()

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
            startActivityForResult(intent, IMAGE_CAPTURE)
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
            startActivityForResult(Intent.createChooser(galleryIntent, "Select Picture"), IMAGE_GALLERY)

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

        if (requestCode == IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {

            imagePath = mediaFile!!.absolutePath
            if (!TextUtils.isEmpty(imagePath)) {
                imageList.add(uri.toString())
                imageAdapter?.setList(imageList)
            } else {
                showSnackBar(binding.root,"Can't get image")
            }

        }

        if (requestCode == IMAGE_GALLERY && resultCode == Activity.RESULT_OK && data != null) {
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
                    Log.e(TAG, "onActivityResult: No DATA OR URL FOUND", )
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

    private fun isValid(): Boolean {

        if (binding.edtMachineNumber.text.toString().isEmpty()) {
            showSnackBar(binding.root,"Please enter a machine number")
            return false
        }

        if (binding.edtSupCompanyName.text.toString().isEmpty()) {
            showSnackBar(binding.root,"Please enter a supplier company name")
            return false
        }

        if (binding.edtDescription.text.toString().isEmpty()) {
            showSnackBar(binding.root,"Please enter a description")
            return false
        }

        /*if (imageList.size == 0){
            showSnackBar(binding.root,"Please Upload Image")
            return false
        }*/

        return true
    }

    private fun addMachineDetailApiCall(){
        val machineNumber = RequestBody.create("multipart/form-data".toMediaTypeOrNull(), binding.edtMachineNumber.text.toString())
        val supplierCompanyName = RequestBody.create("multipart/form-data".toMediaTypeOrNull(), binding.edtSupCompanyName.text.toString())
        val supplierSerialNumber = RequestBody.create("multipart/form-data".toMediaTypeOrNull(), binding.edtSupSerialNo.text.toString())
        val selectedDate = RequestBody.create("multipart/form-data".toMediaTypeOrNull(), selectedDateForApi)
        val description = RequestBody.create("multipart/form-data".toMediaTypeOrNull(), binding.edtDescription.text.toString())
        val machineName = RequestBody.create("multipart/form-data".toMediaTypeOrNull(), "new test")
        val machineModelNumber = RequestBody.create("multipart/form-data".toMediaTypeOrNull(), "new2015")
        val addMachineImages : ArrayList<MultipartBody.Part> = ArrayList()

        for (i in 0 until imageList.size) {
            try {
                if (imageList[i].isEmpty()) {
                    addMachineImages.add(MultipartBody.Part.createFormData("image[]", null.toString()))
                } else {
                    val fileDir = applicationContext.filesDir
                    val file = File(fileDir, "image.jpg")
                    val inputStream = contentResolver.openInputStream(Uri.parse(imageList[i]))
                    val outputStream = FileOutputStream(file)
                    inputStream!!.copyTo(outputStream)

                    val requestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
                    addMachineImages.add(MultipartBody.Part.createFormData("images[]", file.name, requestBody))
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        Log.e(TAG, "addMachineDetailApiCall: API REQUEST machineNumber ${machineNumber.printRequestBody()}")
        Log.e(TAG, "addMachineDetailApiCall: API REQUEST supplierCompanyName ${supplierCompanyName.printRequestBody()}")
        Log.e(TAG, "addMachineDetailApiCall: API REQUEST supplierSerialNumber ${supplierSerialNumber.printRequestBody()}")
        Log.e(TAG, "addMachineDetailApiCall: API REQUEST selectedDate ${selectedDate.printRequestBody()}")
        Log.e(TAG, "addMachineDetailApiCall: API REQUEST description ${description.printRequestBody()}")
        Log.e(TAG, "addMachineDetailApiCall: API REQUEST machineName ${machineName.printRequestBody()}")
        Log.e(TAG, "addMachineDetailApiCall: API REQUEST machineModelNumber ${machineModelNumber.printRequestBody()}")
        Log.e(TAG, "addMachineDetailApiCall: API REQUEST addMachineImages ${addMachineImages.size}")

        if (isOnline()) {
            showLoading("Please wait...")
            val addMachineDetailApiCall = apiService?.addMachineDetail(machineNumber,supplierCompanyName,supplierSerialNumber,selectedDate,description,machineName,machineModelNumber,addMachineImages)

            addMachineDetailApiCall?.enqueue(object : Callback<MachineDetail?>{
                override fun onResponse(call: Call<MachineDetail?>, response: Response<MachineDetail?>) {
                    Log.e(TAG, "addMachineDetailApiCall onResponse : BODY " + Gson().toJson(response.body()))
                    Log.e(TAG, "addMachineDetailApiCall onResponse : CODE " + Gson().toJson(response.code()))
                    Log.e(TAG, "addMachineDetailApiCall onResponse : MESSAGE " + Gson().toJson(response.message()))
                    Log.e(TAG, "addMachineDetailApiCall onResponse : ERROR_BODY " + Gson().toJson(response.errorBody()))
                    Log.e(TAG, "addMachineDetailApiCall: API REQUEST addMachineImages ${addMachineImages.size}")
                    hideLoading()
                    if (response.body() != null && response.code() == 200) {
                        Log.e(TAG, "addMachineDetailApiCall onResponse Successfully")
                        showSubmitDialog()
                    } else {
                        hideLoading()
                        Log.e(TAG, "addMachineDetailApiCall onResponse Else Part")
                    }
                }

                override fun onFailure(call: Call<MachineDetail?>, t: Throwable) {
                    t.printStackTrace()
                    hideLoading()
                    Log.e(TAG, "addMachineDetailApiCall onFailure: ${t.message}")
                    showSnackBar(binding.root, t.message.toString())
                }
            })

        } else {
            showSnackBar(binding.root,getString(R.string.msg_no_network))
        }
    }
}