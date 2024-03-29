package com.frenzin.machinemaintain.activity

import android.os.Bundle
import android.util.Log
import com.frenzin.machinemaintain.R
import com.frenzin.machinemaintain.base.BaseActivity
import com.frenzin.machinemaintain.base.BaseActivity.IMAGE.getDeviceName
import com.frenzin.machinemaintain.databinding.ActivityLoginBinding
import com.frenzin.machinemaintain.model.reqModel.Login
import com.frenzin.machinemaintain.model.resModel.LoginRes
import com.frenzin.machinemaintain.utills.PreferenceKeys
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : BaseActivity() {

    private lateinit var binding: ActivityLoginBinding
    private var TAG = "LoginActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSignIn.setOnClickListener {
            if (isValid()){
                getLoginApiCall()
            }
        }
    }

    private fun getLoginApiCall() {
        if (isOnline()) {
            showLoading("Please wait...")
            FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                    hideLoading()
                    return@OnCompleteListener
                }

                val token = task.result
                val req = Login(binding.edtEmailOrNumber.text.toString(), binding.edtPassword.text.toString(),androidVersion,getDeviceName()!!,appVersion,token)

                Log.e(TAG, "getLoginApiCall API REQUEST :  ${Gson().toJson(req)}", )

                apiService?.getLogin(req)!!.enqueue(object : Callback<LoginRes?> {
                    override fun onResponse(call: Call<LoginRes?>, response: Response<LoginRes?>) {
                        hideLoading()
                        Log.e(TAG, "getLoginApiCall login onResponse : " + Gson().toJson(response.body()))
                        Log.e(TAG, "getLoginApiCall login onResponse : " + Gson().toJson(response.code()))

                        if (response.body() != null && response.code() == 200) {

                            appPref?.set(PreferenceKeys.USER_TOKEN, response.body()!!.token!!)
                            appPref?.set(PreferenceKeys.IS_LOGIN, true)
                            gotoActivity(HomeActivity::class.java, null, false)
                            finish()

                        } else {
                            showSnackBar(binding.root, "User is not Registered")
                        }
                    }

                    override fun onFailure(call: Call<LoginRes?>, t: Throwable) {
                        t.printStackTrace()
                        hideLoading()
                        Log.e(TAG, "getLoginApiCall login onFailure: ${t.message}")
                        showSnackBar(binding.root, t.message.toString())
                    }
                })
            })
        } else {
            hideLoading()
            showSnackBar(binding.root,getString(R.string.msg_no_network))
        }
    }

    private fun isValid(): Boolean {
        if (binding.edtEmailOrNumber.text.toString().isEmpty()) {
            showSnackBar(binding.root,"Please enter a email")
            return false
        }

        if (binding.edtPassword.text.toString().isEmpty()) {
            showSnackBar(binding.root,"Please enter a password")
            return false
        }
        return true
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
    }
}