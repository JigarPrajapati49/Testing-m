package com.frenzin.machinemaintain.activity

import android.os.Bundle
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.SnapHelper
import com.frenzin.machinemaintain.R
import com.frenzin.machinemaintain.adapter.CalendarAdapter
import com.frenzin.machinemaintain.adapter.HistoryDetailAdapter
import com.frenzin.machinemaintain.adapter.MyTaskDetailAdapter
import com.frenzin.machinemaintain.base.BaseActivity
import com.frenzin.machinemaintain.databinding.ActivityHistoryBinding
import com.frenzin.machinemaintain.model.resModel.CalendarDateModel
import com.frenzin.machinemaintain.model.resModel.HistoryDetail
import com.frenzin.machinemaintain.model.resModel.MyTask
import java.text.SimpleDateFormat
import java.util.*

class HistoryActivity : BaseActivity(), CalendarAdapter.onItemClickListener {

    private lateinit var binding: ActivityHistoryBinding
    private lateinit var adapter: CalendarAdapter
    private lateinit var historyDetailAdapter: HistoryDetailAdapter
    private val sdf = SimpleDateFormat("MMMM yyyy", Locale.ENGLISH)
    private val cal = Calendar.getInstance(Locale.ENGLISH)
    private val currentDate = Calendar.getInstance(Locale.ENGLISH)
    private val dates = ArrayList<Date>()
    private val calendarList2 = ArrayList<CalendarDateModel>()
    private var historyDetailList = ArrayList<HistoryDetail>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setUpAdapter()
        setUpClickListener()
        setUpCalendar()
        setMyTaskDetailData()
        setAdapter()

        binding.imgBack.setOnClickListener {
            onBackPressed()
        }
    }

    override fun onItemClick(text: String, date: String, day: String) {
    }

    /**
     * Set up click listener
     */
    private fun setUpClickListener() {
        binding.ivCalendarNext.setOnClickListener {
            cal.add(Calendar.MONTH, 1)
            setUpCalendar()
        }
        binding.ivCalendarPrevious.setOnClickListener {
            cal.add(Calendar.MONTH, -1)
            if (cal == currentDate)
                setUpCalendar()
            else
                setUpCalendar()
        }
    }

    /**
     * Setting up adapter for recyclerview
     */
    private fun setUpAdapter() {
        val snapHelper: SnapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(binding.recyclerView)
        adapter = CalendarAdapter { _: CalendarDateModel, position: Int ->
            calendarList2.forEachIndexed { index, calendarModel ->
                calendarModel.isSelected = index == position
            }
            adapter.setData(calendarList2)
            adapter.setOnItemClickListener(this)
        }
        binding.recyclerView.adapter = adapter
    }

    /**
     * Function to setup calendar for every month
     */
    private fun setUpCalendar() {
        val calendarList = ArrayList<CalendarDateModel>()
        binding.textDateMonth.text = sdf.format(cal.time)
        val monthCalendar = cal.clone() as Calendar
        val maxDaysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        dates.clear()
        monthCalendar.set(Calendar.DAY_OF_MONTH, 1)
        while (dates.size < maxDaysInMonth) {
            dates.add(monthCalendar.time)
            calendarList.add(CalendarDateModel(monthCalendar.time))
            monthCalendar.add(Calendar.DAY_OF_MONTH, 1)
        }
        calendarList2.clear()
        calendarList2.addAll(calendarList)
        adapter.setOnItemClickListener(this)
        adapter.setData(calendarList)
    }

    private fun setAdapter() {
        binding.rvHistory.layoutManager = LinearLayoutManager(this)
        historyDetailAdapter = HistoryDetailAdapter(historyDetailList)
        binding.rvHistory.adapter = historyDetailAdapter
//        binding.rvHistory.layoutAnimation = AnimationUtils.loadLayoutAnimation(this, R.anim.layout_right_to_left)
//        binding.rvHistory.scheduleLayoutAnimation()
        binding.rvHistory.setHasFixedSize(true)
    }

    private fun setMyTaskDetailData() {
        historyDetailList.apply {
            add(HistoryDetail("Mahesh Kumar", "Pipe Cutting"))
            add(HistoryDetail("Rahul Kumar", "Hydraulic Shearing"))
            add(HistoryDetail("Mitesh Kumar", "Mechanical Shearing"))
            add(HistoryDetail("Keval Kumar", "Hydraulic Shearing"))
            add(HistoryDetail("Jaydeep Kumar", "Pipe Cutting"))
            add(HistoryDetail("Jigar Kumar", "Mechanical Shearing"))
            add(HistoryDetail("Kamlesh Kumar", "Pipe Cutting"))
            add(HistoryDetail("Bipin Kumar", "Hydraulic Shearing"))
            add(HistoryDetail("Brijesh Kumar", "Mechanical Shearing"))
            add(HistoryDetail("Jeel Kumar", "Hydraulic Shearing"))
            add(HistoryDetail("Jay Kumar", "Pipe Cutting"))
            add(HistoryDetail("Nirav Kumar", "Mechanical Shearing"))
            add(HistoryDetail("Taksh Kumar", "Pipe Cutting"))
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}