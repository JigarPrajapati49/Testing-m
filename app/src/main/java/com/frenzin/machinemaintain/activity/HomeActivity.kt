package com.frenzin.machinemaintain.activity

import android.os.Bundle
import android.util.Log
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI.setupWithNavController
import com.frenzin.machinemaintain.R
import com.frenzin.machinemaintain.base.BaseActivity
import com.frenzin.machinemaintain.databinding.ActivityHomeBinding
import com.frenzin.machinemaintain.utills.PreferenceKeys

class HomeActivity : BaseActivity() {

    private lateinit var binding : ActivityHomeBinding
    private lateinit var navController: NavController
    private var TAG = "HomeActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.e(TAG, "onCreate: ----FCM_TOKEN---- ${appPref?.getString(PreferenceKeys.FCM_TOKEN)}")
        Log.e(TAG, "onCreate: ----USER_TOKEN---- ${appPref?.getString(PreferenceKeys.USER_TOKEN)}")
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        navigateFragments()
    }

    private fun navigateFragments(){
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.navController) as NavHostFragment
        navController = navHostFragment.navController
        setupWithNavController(binding.bottomNavigation, navController)
    }
}