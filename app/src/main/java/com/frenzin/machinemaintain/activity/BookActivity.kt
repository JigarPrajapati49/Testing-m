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
import android.view.View
import android.view.Window
import android.widget.*
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.frenzin.machinemaintain.R
import com.frenzin.machinemaintain.adapter.ImageAdapter
import com.frenzin.machinemaintain.base.BaseActivity
import com.frenzin.machinemaintain.databinding.ActivityBookBinding
import com.frenzin.machinemaintain.model.resModel.LoginRes
import com.frenzin.machinemaintain.model.resModel.UserList
import com.frenzin.machinemaintain.model.resModel.UserListData
import com.frenzin.machinemaintain.utills.Constant
import com.frenzin.machinemaintain.utills.ImageUtils.Companion.getOutputMediaFile
import com.frenzin.machinemaintain.utills.ImageUtils.Companion.hasPermission
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

class BookActivity : BaseActivity(), ImageAdapter.RemoveImage {

    private lateinit var uri: Uri
    private lateinit var binding: ActivityBookBinding
    private var mediaFile: File? = null
    private var imagePath = ""
    private var selectMultipleImage = true
    private val TAG = "BookActivity"
    private var imageList = ArrayList<String>()
    private var imageAdapter: ImageAdapter? = null
    private var machineName = ""
    private var machineId = ""
    private var isNotBookedQr = ""
    private var salesPersonId = 0
    private var salesPersonList = ArrayList<String>()
    private var salesPersonMainList = ArrayList<UserListData>()

    companion object {
        const val IMAGE_CAPTURE = 1
        const val IMAGE_GALLERY = 2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookBinding.inflate(layoutInflater)
        setContentView(binding.root)

        getLoginUserListApiCall()
        initView()

        intent.extras?.let {
            machineId = it.getString(PreferenceKeys.MACHINE_ID_BY_SERIAL_NUMBER).toString()
            isNotBookedQr = it.getString(Constant.IS_QR_NOT_BOOKED).toString()
            machineName = it.getString(Constant.MACHINE_NAME).toString()
            Log.e(TAG, "onCreate: $machineName", )
        }

        if (machineName == "null" || machineName == ""){
            binding.txtBook.text = "Book"
        } else {
            binding.txtBook.text = "Book $machineName"
        }

        binding.btnSubmit.setOnClickListener {
            if (isValid()) {
                bookMachineApiCall()
            }
        }

        binding.imgBack.setOnClickListener {
            onBackPressed()
        }

        binding.imageAttach.setOnClickListener {
            showCameraImageDialog()
        }

        binding.spinner.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                (parent.getChildAt(0) as TextView).setTextColor(Color.WHITE)
                for (i in 0 until salesPersonMainList.size){
                    if (salesPersonList[position] == salesPersonMainList[i].name){
                        salesPersonId = salesPersonMainList[i].id!!
                        Toast.makeText(this@BookActivity, "selected Name" + salesPersonList[position], Toast.LENGTH_SHORT).show()
                        Toast.makeText(this@BookActivity, "selected ID $salesPersonId", Toast.LENGTH_SHORT).show()
                        break
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {

            }
        }
    }

    private fun dialogBookedSuccessfully() {
        val dialog = AlertDialog.Builder(this).create()
        val view = layoutInflater.inflate(R.layout.dialog_successful, null)

        view.apply {

            val btnComplete = findViewById<AppCompatButton>(R.id.btnComplete)
            val close = findViewById<AppCompatImageView>(R.id.imgCancel)
            val txtSuccessfully = findViewById<AppCompatTextView>(R.id.txtSuccessful)

            txtSuccessfully.text = context.getString(R.string.book_succesful)
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S || hasPermission(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE))) {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

            mediaFile = getOutputMediaFile()
            uri = FileProvider.getUriForFile(this, this.packageName + ".provider", mediaFile!!)
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S || hasPermission(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE))) {
            val galleryIntent = Intent()
            galleryIntent.action = Intent.ACTION_GET_CONTENT
            galleryIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
            if (selectMultipleImage) {
                galleryIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            }
            startActivityForResult(
                Intent.createChooser(galleryIntent, "Select Picture"),
                IMAGE_GALLERY
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

    private fun initView() {
        imageAdapter = ImageAdapter(this)
        imageAdapter!!.setRemover(this)
        binding.rvImages.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rvImages.adapter = imageAdapter
    }

    private fun setSpinnerAdapter() {
        if (binding.spinner != null) {
            val adapter = ArrayAdapter(this, R.layout.adpter_spinner_row_file, salesPersonList)
            binding.spinner.adapter = adapter
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            1 -> if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.S || hasPermission(
                    this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                )
            ) cameraIntent()
            2 -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S || hasPermission(
                    this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                )
            ) galleryIntent()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            imagePath = mediaFile!!.absolutePath
            if (!TextUtils.isEmpty(imagePath)) {
                imageList.add(uri.toString())
                imageAdapter?.setList(imageList)
            } else {
                showSnackBar(binding.root, "Can't get image")
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
                    Log.e(TAG, "onActivityResult: No DATA OR URL FOUND")
                }
            }
        }
    }

    override fun removeImage(position: Int) {
        imageList.removeAt(position)
        imageAdapter!!.setList(imageList)
    }

    private fun isValid(): Boolean {

        if (binding.edtCustomerName.text.toString().isEmpty()) {
            showSnackBar(binding.root, "Please enter a customer name")
            return false
        }

        if (binding.edtMobileNumber.text.toString().isEmpty()) {
            showSnackBar(binding.root, "Please enter a mobile number")
            return false
        }

        if (binding.edtMobileNumber.text.toString().length < 10) {
            showSnackBar(binding.root, "Please enter a valid mobile number")
            return false
        }

        if (binding.edtTaxInvoiceNumber.text.toString().isEmpty()) {
            showSnackBar(binding.root, "Please enter a tax invoice number")
            return false
        }

        if (binding.edtLrNumber.text.toString().isEmpty()) {
            showSnackBar(binding.root, "Please enter a LR number")
            return false
        }

        if (binding.edtAddress.text.toString().isEmpty()) {
            showSnackBar(binding.root, "Please enter a address")
            return false
        }

        if (imageList.size == 0) {
            showSnackBar(binding.root, "Please Upload Image")
            return false
        }

        return true
    }

    private fun bookMachineApiCall() {

        val customerName = RequestBody.create("multipart/form-data".toMediaTypeOrNull(), binding.edtCustomerName.text.toString())
        val mobileNumber = RequestBody.create("multipart/form-data".toMediaTypeOrNull(), binding.edtMobileNumber.text.toString())
        val taxInvoiceNumber = RequestBody.create("multipart/form-data".toMediaTypeOrNull(), binding.edtTaxInvoiceNumber.text.toString())
        val lrNumber = RequestBody.create("multipart/form-data".toMediaTypeOrNull(), binding.edtLrNumber.text.toString())
        val address = RequestBody.create("multipart/form-data".toMediaTypeOrNull(), binding.edtAddress.text.toString())
        val piNumber = RequestBody.create("multipart/form-data".toMediaTypeOrNull(), binding.edtPiNumber.text.toString())
        val difNumber = RequestBody.create("multipart/form-data".toMediaTypeOrNull(), binding.edtDifNumber.text.toString())
        val salesPersonId = RequestBody.create("multipart/form-data".toMediaTypeOrNull(), "$salesPersonId")
        val machineId = RequestBody.create("multipart/form-data".toMediaTypeOrNull(), machineId)
        val addBookDetailImage: ArrayList<MultipartBody.Part> = ArrayList()

        for (i in 0 until imageList.size) {
            try {
                if (imageList[i].isEmpty()) {
                    addBookDetailImage.add(
                        MultipartBody.Part.createFormData(
                            "image[]",
                            null.toString()
                        )
                    )
                } else {
                    val fileDir = applicationContext.filesDir
                    val file = File(fileDir, "image.jpg")
                    val inputStream = contentResolver.openInputStream(Uri.parse(imageList[i]))
                    val outputStream = FileOutputStream(file)
                    inputStream!!.copyTo(outputStream)

                    val requestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
                    addBookDetailImage.add(
                        MultipartBody.Part.createFormData(
                            "images[]",
                            file.name,
                            requestBody
                        )
                    )
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        Log.e(TAG, "bookMachineApiCall : API REQUEST customerName ${customerName.printRequestBody()}")
        Log.e(TAG, "bookMachineApiCall : API REQUEST mobileNumber ${mobileNumber.printRequestBody()}")
        Log.e(TAG, "bookMachineApiCall : API REQUEST taxInvoiceNumber ${taxInvoiceNumber.printRequestBody()}")
        Log.e(TAG, "bookMachineApiCall : API REQUEST lrNumber ${lrNumber.printRequestBody()}")
        Log.e(TAG, "bookMachineApiCall : API REQUEST address ${address.printRequestBody()}")
        Log.e(TAG, "bookMachineApiCall : API REQUEST piNumber ${piNumber.printRequestBody()}")
        Log.e(TAG, "bookMachineApiCall : API REQUEST difNumber ${difNumber.printRequestBody()}")
        Log.e(TAG, "bookMachineApiCall : API REQUEST salesPersonId ${salesPersonId.printRequestBody()}")
        Log.e(TAG, "bookMachineApiCall : API REQUEST machineId ${machineId.printRequestBody()}")
        Log.e(TAG, "bookMachineApiCall : API REQUEST addMachineImages ${addBookDetailImage.size}")

        if (isOnline()) {
            showLoading("Please wait...")
            val bookDetailApiCall = apiService?.bookMachine(
                customerName,
                mobileNumber,
                piNumber,
                difNumber,
                taxInvoiceNumber,
                lrNumber,
                address,
                salesPersonId,
                machineId,
                addBookDetailImage
            )

            bookDetailApiCall?.enqueue(object : Callback<LoginRes?> {
                override fun onResponse(call: Call<LoginRes?>, response: Response<LoginRes?>) {
                    Log.e(TAG, "bookMachineApiCall onResponse : BODY " + Gson().toJson(response.body()))
                    Log.e(TAG, "bookMachineApiCall onResponse : CODE " + Gson().toJson(response.code()))
                    Log.e(TAG, "bookMachineApiCall: API REQUEST addBookDetailImage ${addBookDetailImage.size}")
                    Log.e(TAG, "bookMachineApiCall : API REQUEST machineId ${machineId.printRequestBody()}")
                    hideLoading()
                    if (response.body() != null && response.code() == 200) {
                        dialogBookedSuccessfully()
                        Log.e(TAG, "bookMachineApiCall onResponse Successfully")
                    } else {
                        Log.e(TAG, "bookMachineApiCall onResponse Else Part")
                    }
                }

                override fun onFailure(call: Call<LoginRes?>, t: Throwable) {
                    t.printStackTrace()
                    hideLoading()
                    Log.e(TAG, "bookMachineApiCall onFailure: ${t.message}")
                    showSnackBar(binding.root, t.message.toString())
                }
            })
        } else {
            showSnackBar(binding.root, getString(R.string.msg_no_network))
        }
    }

    private fun getLoginUserListApiCall() {
        if (isOnline()) {
            showLoading("Please wait...")
            val getLoginUserDetail = apiService?.getLoginUserList()

            getLoginUserDetail?.enqueue(object : Callback<UserList?> {
                override fun onResponse(call: Call<UserList?>, response: Response<UserList?>) {
                    Log.e(TAG, "getLoginUserListApiCall onResponse : BODY " + Gson().toJson(response.body()))
                    Log.e(TAG, "getLoginUserListApiCall onResponse : CODE " + Gson().toJson(response.code()))
                    Log.e(TAG, "getLoginUserListApiCall onResponse : MESSAGE " + Gson().toJson(response.message()))
                    Log.e(TAG, "getLoginUserListApiCall onResponse : ERROR_BODY " + Gson().toJson(response.errorBody()))
                    hideLoading()
                    if (response.body() != null && response.code() == 200) {
                        Log.e(TAG, "getLoginUserListApiCall onResponse Successfully")
                        salesPersonMainList.addAll(response.body()!!.data)
                        for (i in 0 until response.body()!!.data.size){
                            salesPersonList.add(response.body()!!.data[i].name.toString())
                        }
                        setSpinnerAdapter()
                    } else {
                        hideLoading()
                        Log.e(TAG, "getLoginUserListApiCall onResponse Else Part")
                    }
                }

                override fun onFailure(call: Call<UserList?>, t: Throwable) {
                    t.printStackTrace()
                    hideLoading()
                    Log.e(TAG, "getLoginUserListApiCall onFailure: ${t.message}")
                    showSnackBar(binding.root, t.message.toString())
                }

            })

        } else {
            showSnackBar(binding.root, getString(R.string.msg_no_network))
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if (isNotBookedQr == "isNotBooked") {
            gotoActivity(HomeActivity::class.java, null, true)
        } else {
            onBackPressedDispatcher.onBackPressed()
        }
    }
}