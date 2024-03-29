package com.frenzin.machinemaintain.activity

import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.frenzin.machinemaintain.base.BaseActivity
import com.frenzin.machinemaintain.databinding.ActivitySpalshBinding
import com.frenzin.machinemaintain.utills.PreferenceKeys

class SplashActivity : BaseActivity() {

    private lateinit var binding: ActivitySpalshBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySpalshBinding.inflate(layoutInflater)
        setContentView(binding.root)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        init()
    }

    private fun init(){
        Handler().postDelayed({
            gotoNext()
        },1000)
    }

    private fun gotoNext() {
        if (appPref?.getBoolean(PreferenceKeys.IS_LOGIN) == true) {
            gotoActivity(HomeActivity::class.java,null,true)
        }else{
            gotoActivity(LoginActivity::class.java,null,false)
        }
    }
}