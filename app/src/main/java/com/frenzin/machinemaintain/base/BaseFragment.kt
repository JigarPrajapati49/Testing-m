package com.frenzin.machinemaintain.base

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.ConnectivityManager
import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.frenzin.machinemaintain.R
import com.frenzin.machinemaintain.base.BaseActivity.Companion.apiService
import com.frenzin.machinemaintain.network.RetrofitClient
import com.frenzin.machinemaintain.utills.AppPref
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar

open class BaseFragment : Fragment() {

    lateinit var contex: Context
    protected var dialog: ProgressDialog? = null
    lateinit var appPref: AppPref

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appPref = AppPref.getInstance(contex)!!
        dialog = ProgressDialog(contex, R.style.MyAlertDialogStyle)
        apiService = RetrofitClient.getApiService()
        appPref = AppPref.getInstance(contex)!!
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        this.contex = context
    }

    override fun onAttachFragment(fragment: Fragment) {
        super.onAttachFragment(fragment)
    }

    //for....prefrences
    val pref: AppPref?
        get() = AppPref.getInstance(contex!!)

    //For....get BaseActivity
    var base: BaseActivity? = null
        get() = activity as BaseActivity?

    //For... show toast msg.
    fun showToast(msg: String) {
        Toast.makeText(context, msg + "", Toast.LENGTH_SHORT).show()
    }

    //For...check internet is on or off
    open fun isOnline(): Boolean {
        val manager = activity?.getSystemService(AppCompatActivity.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = manager.activeNetworkInfo
        var isAvailable = false
        if (networkInfo != null && networkInfo.isConnected) {
            isAvailable = true
        }
        if (!isAvailable) {

        }
        return isAvailable
    }

    //For...show loading process during api call
    fun showLoading(msg: String?) {
        try {
            if (dialog != null) hideLoading()
            if (dialog == null) {
                dialog = ProgressDialog(contex)
            }
            dialog!!.setMessage(if (!TextUtils.isEmpty(msg)) msg else resources.getString(R.string.loading))
            if (!dialog!!.isShowing()) dialog!!.show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    //For...hide loading dialog after getting response
    fun hideLoading() {
        if (dialog != null) {
            dialog!!.dismiss()
            dialog = null
        }
    }

    //For...if response is null from api.
    fun responseNull() {
        showToast(resources.getString(R.string.responseNull))
    }

    //For...if api calling is fail
    fun onFailure(message: String) {
        showToast(message)
    }

    //For....if set code in onSuccess.
    fun OnSuccess() {}

    //For....validation of EditText
    fun valid(editText: EditText, errorMsg: Int): Boolean {
        if (TextUtils.isEmpty(editText.getText().toString().trim { it <= ' ' })) {
            showToast(resources.getString(errorMsg))
            return false
        }
        return true
    }

    //For....validation of email
    fun validEmail(editText: EditText, errorMsg: Int): Boolean {
        if (!Patterns.EMAIL_ADDRESS.matcher(editText.getText().toString()).matches()) {
            showToast(resources.getString(errorMsg))
            return false
        }
        return true
    }

    @SuppressLint("ResourceAsColor")
    fun showSnackBar(view: View, msg: String) {
        val snackBar = Snackbar.make(view, msg, Snackbar.LENGTH_SHORT)
        snackBar.setAction(getString(R.string.ok), View.OnClickListener {
            snackBar.dismiss()
        })
        snackBar.setActionTextColor(Color.WHITE)
        val snackBarView = snackBar.view
        snackBarView.setBackgroundColor(ContextCompat.getColor(context!!, R.color.lightCloud))
        val textView = snackBarView.findViewById(com.google.android.material.R.id.snackbar_text) as TextView
        textView.setTextColor(Color.WHITE)
        textView.textSize = 14f
        snackBar.animationMode = BaseTransientBottomBar.ANIMATION_MODE_SLIDE
        snackBar.show()
    }

    //For...change activity
    fun gotoActivity(className: Class<*>?, bundle: Bundle?, isClearStack: Boolean) {
        val intent = Intent(context, className)
        if (bundle != null) intent.putExtras(bundle)
        if (isClearStack) {
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(intent)
    }

    fun getLastBitFromUrl(url: String): String? {
        return url.replaceFirst(".*/([^/?]+).*".toRegex(), "$1")
    }

    override fun onDestroy() {
        super.onDestroy()
        hideLoading()
    }

    companion object {
        private const val TAG = "BaseFragment"
    }

}