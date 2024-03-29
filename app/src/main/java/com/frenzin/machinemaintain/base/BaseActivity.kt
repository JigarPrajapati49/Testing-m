package com.frenzin.machinemaintain.base

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.frenzin.machinemaintain.BuildConfig
import com.frenzin.machinemaintain.R
import com.frenzin.machinemaintain.network.ApiService
import com.frenzin.machinemaintain.network.RetrofitClient
import com.frenzin.machinemaintain.utills.AppPref
import com.frenzin.machinemaintain.utills.PreferenceKeys
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okio.Buffer
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt


open class BaseActivity : AppCompatActivity() {

    lateinit var context: Context
    protected var dialog: ProgressDialog? = null
    private var inputStream: InputStream? = null
    var androidVersion = Build.VERSION.RELEASE
    var appVersion = BuildConfig.VERSION_NAME

    companion object {
        var apiService: ApiService? = null
        var appPref: AppPref? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
        super.onCreate(savedInstanceState)
        context = this@BaseActivity
        apiService = RetrofitClient.getApiService()
        appPref = AppPref.getInstance(this)!!
    }

    val httpClient: OkHttpClient
        get() = OkHttpClient().newBuilder().connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS).addInterceptor { chain ->
                val originalRequest = chain.request()
                val builder = originalRequest.newBuilder()
                    .header(
                        "Authorization",
                        "Bearer" + appPref?.getString(PreferenceKeys.USER_TOKEN)
                    )

                Log.e("Authorization", "Bearer" + appPref?.getString(PreferenceKeys.USER_TOKEN))
                val newRequest = builder.build()
                chain.proceed(newRequest)
            }
            .build()


    //For... show toast msg.
    fun showToast(msg: String) {
        Toast.makeText(this, msg + "", Toast.LENGTH_SHORT).show()
    }

    //for....prefrences
    val pref: AppPref?
        get() = AppPref.getInstance(this@BaseActivity)//AppDialog.showNoNetworkDialog(this);

    //For...check internet is on or off
    open fun isOnline(): Boolean {
        val manager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = manager.activeNetworkInfo
        var isAvailable = false
        if (networkInfo != null && networkInfo.isConnected) {
            isAvailable = true
        }
        if (!isAvailable) {

        }
        return isAvailable
    }

    //For...check permission
    fun hasPermission(permissions: Array<String>?): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && permissions != null) {
            for (permission in permissions) {
                if (ActivityCompat.checkSelfPermission(
                        this,
                        permission
                    ) != PackageManager.PERMISSION_GRANTED
                ) return false
            }
        }
        return true
    }

    //For....validation of EditText
    fun valid(editText: EditText, errorMsg: Int): Boolean {
        if (TextUtils.isEmpty(editText.getText().toString().trim { it <= ' ' })) {
            showToast(getResources().getString(errorMsg))
            return false
        }
        return true
    }

    fun valid(editText: TextView, errorMsg: Int): Boolean {
        if (TextUtils.isEmpty(editText.getText().toString().trim { it <= ' ' })) {
            showToast(getResources().getString(errorMsg))
            return false
        }
        return true
    }

    //For....validation of email
    fun validEmail(editText: EditText, errorMsg: Int): Boolean {
        if (!Patterns.EMAIL_ADDRESS.matcher(editText.getText().toString()).matches()) {
            showToast(getResources().getString(errorMsg))
            return false
        }
        return true
    }

    //For...show loading process during api call
    fun showLoading(msg: String?) {
        if (dialog != null) hideLoading()
        if (dialog == null) {
            dialog = ProgressDialog(this)
        }
        dialog!!.setMessage(if (!TextUtils.isEmpty(msg)) msg else "loading")
        if (!dialog!!.isShowing()) dialog!!.show()
    }

    //For...hide loading dialog after getting response
    fun hideLoading() {
        if (dialog != null && dialog!!.isShowing()) {
            dialog!!.dismiss()
        }
    }

    //For...change activity
    fun gotoActivity(className: Class<*>?, bundle: Bundle?, isClearStack: Boolean) {
        val intent = Intent(this, className)
        if (bundle != null) intent.putExtras(bundle)
        if (isClearStack) {
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(intent)
    }

    @SuppressLint("ResourceAsColor")
    fun showSnackBar(view: View, msg: String) {
        val snackBar = Snackbar.make(view, msg, Snackbar.LENGTH_SHORT)
        snackBar.setAction(getString(R.string.ok), View.OnClickListener {
            snackBar.dismiss()
        })
        snackBar.setActionTextColor(Color.WHITE)
        val snackBarView = snackBar.view
        snackBarView.setBackgroundColor(ContextCompat.getColor(context, R.color.lightCloud))
        val textView =
            snackBarView.findViewById(com.google.android.material.R.id.snackbar_text) as TextView
        textView.setTextColor(Color.WHITE)
        textView.textSize = 14f
        snackBar.animationMode = BaseTransientBottomBar.ANIMATION_MODE_SLIDE
        snackBar.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        hideLoading()
    }

    fun ArrayList<MultipartBody.Part>.printArrayRequestBody(): ArrayList<String> {
        val stringList = ArrayList<String>()
        forEach { part ->
            val buffer = Buffer()
            part.body?.writeTo(buffer)
            val partString = buffer.readUtf8()
            stringList.add(partString)
        }
        return stringList
    }

    fun RequestBody.printRequestBody(): String {
        return try {
            val buffer = Buffer()
            writeTo(buffer)
            buffer.readUtf8()
        } catch (e: IOException) {
            "Error reading RequestBody"
        }
    }

    fun readBytes(inputStream: InputStream): ByteArray? {
        // this dynamically extends to take the bytes you read
        val byteBuffer = ByteArrayOutputStream()

        // this is storage overwritten on each iteration with bytes
        val bufferSize = 1024
        val buffer = ByteArray(bufferSize)

        // we need to know how may bytes were read to write them to the byteBuffer
        var len = 0
        while (inputStream.read(buffer).also { len = it } != -1) {
            byteBuffer.write(buffer, 0, len)
        }

        // and then we can return your byte array.
        return byteBuffer.toByteArray()
    }

    fun getInputStreamByUri(cxt: Context, uri: Uri): InputStream? {
        //  context = cxt
        return try {
            inputStream = contentResolver.openInputStream(uri)
            inputStream
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            Log.e("TAG", "exception$e")
            null
        }
    }

    object IMAGE{
        fun resizeImage(context: Context?, bitmap: Bitmap, name: String?): String {
            val path: Uri
            val cameraFile: File
            val timeStamp = SimpleDateFormat(
                "yyyy_MM_dd_HH_mm_ss",
                Locale.getDefault()
            ).format(Date())

            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                cameraFile = File(
                    context?.cacheDir, File.separator
                            + "Compressor_" + System.currentTimeMillis() + "_" + timeStamp + ".jpg"
                )
                path = FileProvider.getUriForFile(
                    context!!,
                    context.packageName + ".provider",
                    cameraFile
                )
            } else {
                cameraFile = File(
                    context!!.cacheDir, File.separator
                            + "Compressor_" + System.currentTimeMillis() + "_" + timeStamp + ".jpg"
                )
                path = Uri.fromFile(cameraFile)
            }
            val os: OutputStream
            try {
                os = FileOutputStream(cameraFile)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os)
                os.flush()
                os.close()
            } catch (e: Exception) {
                Log.e("TAG", "Error writing bitmap", e)
            }
            Log.e("TAG", "resize Image $path")
            return path.toString();
        }

        fun scaleDown(realImage: Bitmap, maxImageSize: Float, filter: Boolean): Bitmap? {
            val ratio = (maxImageSize / realImage.width).coerceAtMost(maxImageSize / realImage.height)
            val width = (ratio * realImage.width).roundToInt()
            val height = (ratio * realImage.height).roundToInt()
            return Bitmap.createScaledBitmap(
                realImage, width,
                height, filter
            )
        }

        fun getLastBitFromUrl(url: String): String? {
            return url.replaceFirst(".*/([^/?]+).*".toRegex(), "$1")
        }

        private fun capitalize(s: String?): String? {
            if (s == null || s.length == 0) {
                return ""
            }
            val first = s[0]
            return if (Character.isUpperCase(first)) {
                s
            } else {
                first.uppercaseChar().toString() + s.substring(1)
            }
        }

        fun getDeviceName(): String? {
            val manufacturer = Build.MANUFACTURER
            val model = Build.MODEL
            return if (model.startsWith(manufacturer)) {
                capitalize(model)
            } else {
                capitalize(manufacturer) + " " + model
            }
        }
    }
}

