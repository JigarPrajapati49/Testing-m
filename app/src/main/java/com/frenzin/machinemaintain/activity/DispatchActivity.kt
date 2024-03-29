package com.frenzin.machinemaintain.activity

import android.Manifest
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
import com.frenzin.machinemaintain.databinding.ActivityDispatchBinding
import com.frenzin.machinemaintain.model.resModel.DispatchMachine
import com.frenzin.machinemaintain.utills.Constant
import com.frenzin.machinemaintain.utills.ImageUtils
import com.frenzin.machinemaintain.utills.PreferenceKeys
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

class DispatchActivity : BaseActivity(), ImageAdapter.RemoveImage {

    private lateinit var binding: ActivityDispatchBinding
    private lateinit var uri: Uri
    private var mediaFile: File? = null
    private var imagePath = ""
    private var selectMultipleImage = true
    private val TAG = "DispatchActivity"
    private var imageList = ArrayList<String>()
    private var imageAdapter: ImageAdapter? = null
    private var machineId = "47"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDispatchBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initView()

        intent.extras?.let {
            machineId = it.getString(PreferenceKeys.MACHINE_ID_BY_SERIAL_NUMBER).toString()
            binding.txtMachineNumber.text = it.getString(Constant.MACHINE_SERIAL_NUMBER)
            binding.txtCustomerName.text = it.getString(Constant.MACHINE_NAME)
            if (it.getString(Constant.PI_NUMBER).toString() != "null"){
                binding.edtPiNumber.setText(it.getString(Constant.PI_NUMBER).toString())
            } else if (it.getString(Constant.DIF_NUMBER).toString() != "null"){
                binding.edtDifNumber.setText(it.getString(Constant.DIF_NUMBER).toString())
            } else if (it.getString(Constant.E_WAY_NUMBER).toString() != "null"){
                binding.edtWayNo.setText(it.getString(Constant.E_WAY_NUMBER).toString())
            }
        }

        binding.imageAttach.setOnClickListener {
            showCameraImageDialog()
        }

        binding.imgBack.setOnClickListener {
            finish()
        }

        binding.btnDispatch.setOnClickListener {
            dispatchMachineApiCall()
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
                showSnackBar(binding.root, "Can't get image")
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

    private fun initView() {
        imageAdapter = ImageAdapter(this)
        imageAdapter!!.setRemover(this)
        binding.rvImages.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rvImages.adapter = imageAdapter
    }

    override fun removeImage(position: Int) {
        imageList.removeAt(position)
        imageAdapter!!.setList(imageList)
    }

    private fun dispatchMachineApiCall(){
        val piNumber = RequestBody.create("multipart/form-data".toMediaTypeOrNull(), binding.edtPiNumber.text.toString())
        val difNumber = RequestBody.create("multipart/form-data".toMediaTypeOrNull(), binding.edtDifNumber.text.toString())
        val eWayNumber = RequestBody.create("multipart/form-data".toMediaTypeOrNull(), binding.edtWayNo.text.toString())
        val machineId = RequestBody.create("multipart/form-data".toMediaTypeOrNull(), machineId)
        val addDispatchImages : ArrayList<MultipartBody.Part> = ArrayList()
        for (i in 0 until imageList.size) {
            try {
                if (imageList[i].isEmpty()) {
                    addDispatchImages.add(MultipartBody.Part.createFormData("image[]", null.toString()))
                } else {
                    val fileDir = applicationContext.filesDir
                    val file = File(fileDir, "image.jpg")
                    val inputStream = contentResolver.openInputStream(Uri.parse(imageList[i]))
                    val outputStream = FileOutputStream(file)
                    inputStream!!.copyTo(outputStream)

                    val requestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
                    addDispatchImages.add(MultipartBody.Part.createFormData("images[]", file.name, requestBody))
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        Log.e(TAG, "dispatchMachineApiCall : API REQUEST piNumber ${piNumber.printRequestBody()}")
        Log.e(TAG, "dispatchMachineApiCall : API REQUEST difNumber ${difNumber.printRequestBody()}")
        Log.e(TAG, "dispatchMachineApiCall : API REQUEST eWayNumber ${eWayNumber.printRequestBody()}")
        Log.e(TAG, "dispatchMachineApiCall : API REQUEST machineId ${machineId.printRequestBody()}")
        Log.e(TAG, "dispatchMachineApiCall : API REQUEST addDispatchImages ${addDispatchImages.size}")
        if (isOnline()) {
            showLoading("Please wait...")
            val dispatchMachineReq = apiService?.dispatchMachine(machineId,piNumber,difNumber,eWayNumber,addDispatchImages)
            dispatchMachineReq?.enqueue(object : Callback<DispatchMachine?>{
                override fun onResponse(call: Call<DispatchMachine?>, response: Response<DispatchMachine?>) {
                    Log.e(TAG, "dispatchMachineApiCall onResponse : BODY " + Gson().toJson(response.body()))
                    Log.e(TAG, "dispatchMachineApiCall onResponse : CODE " + Gson().toJson(response.code()))
                    Log.e(TAG, "dispatchMachineApiCall : API REQUEST addBookDetailImage ${addDispatchImages.size}")
                    hideLoading()
                    if (response.body() != null && response.code() == 200) {
                        Log.e(TAG, "dispatchMachineApiCall onResponse Successfully")
                        dialogDispatchSuccessfully()
                    } else {
                        Log.e(TAG, "dispatchMachineApiCall onResponse Else Part")
                    }
                }
                override fun onFailure(call: Call<DispatchMachine?>, t: Throwable) {
                    t.printStackTrace()
                    hideLoading()
                    Log.e(TAG, "dispatchMachineApiCall onFailure: ${t.message}")
                    showSnackBar(binding.root, t.message.toString())
                }
            })
        }else{
            showSnackBar(binding.root,getString(R.string.msg_no_network))
        }
    }

    private fun dialogDispatchSuccessfully(){
        val dialog = AlertDialog.Builder(this).create()
        val view = layoutInflater.inflate(R.layout.dialog_successful, null)
        view.apply {
            val btnComplete = findViewById<AppCompatButton>(R.id.btnComplete)
            val close = findViewById<AppCompatImageView>(R.id.imgCancel)
            val txtSuccessfully = findViewById<AppCompatTextView>(R.id.txtSuccessful)
            txtSuccessfully.text = context.getString(R.string.dispatch_successful)
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
}