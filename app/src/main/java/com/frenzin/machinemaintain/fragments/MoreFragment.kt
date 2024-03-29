package com.frenzin.machinemaintain.fragments

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.navigation.fragment.findNavController
import com.frenzin.machinemaintain.R
import com.frenzin.machinemaintain.activity.HistoryActivity
import com.frenzin.machinemaintain.activity.LoginActivity
import com.frenzin.machinemaintain.activity.ProfileActivity
import com.frenzin.machinemaintain.base.BaseFragment
import com.frenzin.machinemaintain.databinding.FragmentMoreBinding

class MoreFragment : BaseFragment() {

    private lateinit var binding: FragmentMoreBinding
    private lateinit var contextProfile: Context

    override fun onAttach(context: Context) {
        super.onAttach(context)
        contextProfile = context
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMoreBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.llMyProfile.setOnClickListener {
            gotoActivity(ProfileActivity::class.java,null,false)
        }

        binding.llHistory.setOnClickListener {
            gotoActivity(HistoryActivity::class.java,null,false)
        }

        binding.llHelp.setOnClickListener {
            showSnackBar(binding.root,"Click on Help")
        }

        binding.llLogout.setOnClickListener {
            dialogLogoutSuccessful()
        }

        binding.imgBack.setOnClickListener {
            findNavController().navigate(R.id.homeFragment)
        }
    }

    private fun dialogLogoutSuccessful() {
        val dialog = AlertDialog.Builder(contextProfile).create()
        val view = layoutInflater.inflate(R.layout.dialog_logout, null)

        view.apply {

            val btnComplete = findViewById<AppCompatButton>(R.id.btnComplete)
            val close = findViewById<AppCompatImageView>(R.id.imgCancel)

            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.setView(view)
            dialog.setCanceledOnTouchOutside(false)

            btnComplete.setOnClickListener {
                gotoActivity(LoginActivity::class.java, null, true)
                dialog.dismiss()
            }

            close.setOnClickListener {
                dialog.dismiss()
            }

            dialog.show()

        }
    }
}