package com.frenzin.machinemaintain.activity

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageView
import com.frenzin.machinemaintain.R
import com.frenzin.machinemaintain.base.BaseActivity
import com.frenzin.machinemaintain.databinding.ActivityProfileBinding

class ProfileActivity : BaseActivity() {

    private lateinit var binding : ActivityProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.llLogOut.setOnClickListener {
            dialogLogoutSuccessful()
        }

        binding.imgBack.setOnClickListener {
            finish()
        }
    }

    private fun dialogLogoutSuccessful() {
        val dialog = AlertDialog.Builder(this).create()
        val view = layoutInflater.inflate(R.layout.dialog_logout, null)

        view.apply {

            val btnComplete = findViewById<AppCompatButton>(R.id.btnComplete)
            val close = findViewById<AppCompatImageView>(R.id.imgCancel)

            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.setView(view)
            dialog.setCanceledOnTouchOutside(false)

            btnComplete.setOnClickListener {
                appPref?.clearSession()
                gotoActivity(LoginActivity::class.java, null, true)
                dialog.dismiss()
            }

            close.setOnClickListener {
                dialog.dismiss()
            }

            dialog.show()

        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}