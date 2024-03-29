package com.frenzin.machinemaintain.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.GridLayoutManager
import com.frenzin.machinemaintain.R
import com.frenzin.machinemaintain.adapter.BookedDetailAdapter
import com.frenzin.machinemaintain.base.BaseActivity
import com.frenzin.machinemaintain.databinding.ActivityBookedDetailBinding
import com.frenzin.machinemaintain.model.resModel.BookDetailsImage
import com.frenzin.machinemaintain.utills.Constant
import java.text.SimpleDateFormat

class BookedDetailActivity : BaseActivity() {

    private lateinit var binding: ActivityBookedDetailBinding
    private lateinit var bookDetailAdapter: BookedDetailAdapter
    private var bookDetailImageList = ArrayList<BookDetailsImage>()
    private var machineImageList = ArrayList<String>()
    private var isBookedQr = ""
    private var TAG = "BookedDetailActivity"

    @SuppressLint("SimpleDateFormat")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookedDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        intent.extras?.let {
            isBookedQr = it.getString(Constant.IS_QR_BOOKED).toString()
            binding.txtBookedMachineName.text = "Book ${it.getString(Constant.MACHINE_NAME).toString()}"
            binding.txtCustomerName.text = it.getString(Constant.CUSTOMER_NAME).toString()
            val date = SimpleDateFormat("yyyy-MM-dd").parse(it.getString(Constant.DATE_OF_BOOK).toString())
            binding.txtDateOfBook.text = SimpleDateFormat("dd MMM yyyy").format(date!!)
            binding.txtMobileNumber.text = it.getString(Constant.MOBILE_NUMBER).toString()
            binding.txtTaxInvoiceNumber.text = it.getString(Constant.TAX_INVOICE_NUMBER).toString()
            binding.txtLrNumber.text = it.getString(Constant.LR_NUMBER).toString()
            binding.txtPiNumber.text = it.getString(Constant.PI_NUMBER).toString()
            binding.txtAddress.text = it.getString(Constant.ADDRESS).toString()
            binding.txtDifNumber.text = it.getString(Constant.DIF_NUMBER).toString()
            machineImageList = it.getStringArrayList(Constant.MACHINE_IMAGES) as ArrayList<String>
            Log.e(TAG, "onCreate: --------machineImageList------- $machineImageList", )
        }

        setBookDetailImageData()
        setAdapter()

        binding.imgBack.setOnClickListener {
            onBackPressed()
        }
    }

    private fun setAdapter() {
        binding.rvBookDetailImages.layoutManager = GridLayoutManager(this, 4)
        bookDetailAdapter = BookedDetailAdapter(bookDetailImageList)
        binding.rvBookDetailImages.adapter = bookDetailAdapter
        binding.rvBookDetailImages.setHasFixedSize(true)
    }

    private fun setBookDetailImageData() {
        bookDetailImageList.clear()
        for (i in 0 until machineImageList.size) {
            bookDetailImageList.add(BookDetailsImage(machineImageList[i]))
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if (isBookedQr == "isBooked") {
            gotoActivity(HomeActivity::class.java, null, true)
        } else {
            onBackPressedDispatcher.onBackPressed()
        }
    }
}