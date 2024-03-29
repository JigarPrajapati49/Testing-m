package com.frenzin.machinemaintain.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
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
import com.frenzin.machinemaintain.databinding.ActivityRaiseComplaintBinding
import com.frenzin.machinemaintain.model.resModel.GetMyTickets
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
import java.text.SimpleDateFormat

class RaiseComplaintActivity : BaseActivity(),ImageAdapter.RemoveImage {

    private lateinit var binding: ActivityRaiseComplaintBinding
    private lateinit var uri: Uri
    private var mediaFile: File? = null
    private var imagePath = ""
    private var selectMultipleImage = true
    private val TAG = "BookActivity"
    private var imageList = ArrayList<String>()
    private var imageAdapter: ImageAdapter? = null
    private var machineId = ""

    @SuppressLint("SimpleDateFormat")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRaiseComplaintBinding.inflate(layoutInflater)
        setContentView(binding.root)

        intent.extras?.let {
            machineId = it.getString(PreferenceKeys.MACHINE_ID_BY_SERIAL_NUMBER).toString()
            binding.txtCustomerName.text = it.getString(Constant.CUSTOMER_NAME)
            binding.txtAddress.text = it.getString(Constant.ADDRESS)

            val date = SimpleDateFormat("yyyy-MM-dd").parse(it.getString(Constant.INVERT_DATE).toString())

            binding.txtPurchaseDate.text = SimpleDateFormat("dd MMM yyyy").format(date!!)
            binding.txtMachineDetail.text = it.getString(Constant.MACHINE_NAME)
        }

        initView()

        binding.imgAdd.setOnClickListener {
            showCameraImageDialog()
        }

        binding.btnSubmit.setOnClickListener {
            dialogRaiseTicketSuccessful()
        }

        binding.imgBack.setOnClickListener {
            onBackPressed()
        }
    }

    private fun dialogRaiseTicketSuccessful(){
        val dialog = AlertDialog.Builder(this).create()
        val view = layoutInflater.inflate(R.layout.dialog_successful, null)

        view.apply {

            val btnComplete = findViewById<AppCompatButton>(R.id.btnComplete)
            val close = findViewById<AppCompatImageView>(R.id.imgCancel)
            val txtDescription = findViewById<AppCompatTextView>(R.id.txtDescription)

            txtDescription.text = "Your Ticket Number #1234"
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.setView(view)
            dialog.setCanceledOnTouchOutside(false)

            btnComplete.setOnClickListener {
                dialog.dismiss()
                onBackPressed()
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

    override fun onBackPressed() {
        super.onBackPressed()
        gotoActivity(HomeActivity::class.java,null,true)
        finish()
    }
}