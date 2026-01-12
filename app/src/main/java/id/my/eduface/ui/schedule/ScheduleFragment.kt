package id.my.eduface.ui.schedule

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import id.my.eduface.R
import id.my.eduface.data.network.RetrofitClient
import id.my.eduface.data.model.ScheduleBaseResponse
import id.my.eduface.data.model.ScheduleResponse
import id.my.eduface.databinding.FragmentScheduleBinding
import id.my.eduface.databinding.ItemTodayScheduleRowBinding
import id.my.eduface.databinding.DialogDetailScheduleBinding
import id.my.eduface.utils.SessionManager
import id.my.eduface.utils.ToastUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class ScheduleFragment : Fragment() {
    private var _binding: FragmentScheduleBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: ScheduleAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentScheduleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupDate()
        setupRecyclerView()
        loadData()
        binding.smartRefresh.setOnRefreshListener { loadData() }
    }

    private fun setupDate() {
        val dateFormat = SimpleDateFormat("EEEE, d MMMM yyyy", Locale("id", "ID"))
        binding.tvCurrentDate.text = dateFormat.format(Date())
    }

    private fun setupRecyclerView() {
        adapter = ScheduleAdapter(emptyList()) { item -> showScheduleDetailDialog(item) }
        binding.rvAllSchedule.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@ScheduleFragment.adapter
            isNestedScrollingEnabled = false
        }
    }

    private fun loadData() {
        binding.shimmerViewContainer.visibility = View.VISIBLE
        binding.shimmerViewContainer.startShimmer()
        binding.mainContentContainer.visibility = View.GONE
        binding.layoutEmpty.visibility = View.GONE

        val token = "Bearer ${SessionManager(requireContext()).fetchAuthToken()}"

        RetrofitClient.instance.getSchedules(token).enqueue(object : Callback<ScheduleBaseResponse> {
            override fun onResponse(call: Call<ScheduleBaseResponse>, response: Response<ScheduleBaseResponse>) {
                if (_binding == null) return

                stopLoading()
                if (response.isSuccessful) {
                    val list = response.body()?.data ?: emptyList()
                    binding.mainContentContainer.visibility = View.VISIBLE
                    if (list.isNotEmpty()) {
                        adapter.setData(list)
                        populateTodaySchedule(list)
                        binding.layoutEmpty.visibility = View.GONE
                    } else {
                        binding.layoutEmpty.visibility = View.VISIBLE
                    }
                } else {
                    binding.mainContentContainer.visibility = View.VISIBLE
                    binding.layoutEmpty.visibility = View.VISIBLE
                    ToastUtils.showError(requireContext(), "Gagal: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<ScheduleBaseResponse>, t: Throwable) {
                if (_binding == null) return
                stopLoading()
                binding.layoutEmpty.visibility = View.VISIBLE
                ToastUtils.showError(requireContext(), "Kesalahan Jaringan")
            }
        })
    }

    private fun stopLoading() {
        binding.root.postDelayed({
            if (_binding != null) {
                binding.shimmerViewContainer.stopShimmer()
                binding.shimmerViewContainer.visibility = View.GONE
                binding.mainContentContainer.visibility = View.VISIBLE
                binding.smartRefresh.finishRefresh()
            }
        }, 1000)
    }

    private fun populateTodaySchedule(list: List<ScheduleResponse>) {
        binding.containerTodaySchedule.removeAllViews()
        val currentDay = SimpleDateFormat("EEEE", Locale("id", "ID")).format(Date())
        val todayList = list.filter { it.dayOfWeek.equals(currentDay, ignoreCase = true) }

        if (todayList.isEmpty()) {
            val emptyTv = TextView(context).apply {
                text = "Tidak ada jadwal untuk hari ini"
                setTextColor(resources.getColor(R.color.text_gray))
                textSize = 13f
                setPadding(0, 20, 0, 0)
            }
            binding.containerTodaySchedule.addView(emptyTv)
            return
        }

        todayList.forEach { item ->
            val rowBinding = ItemTodayScheduleRowBinding.inflate(layoutInflater, binding.containerTodaySchedule, false)
            rowBinding.tvSubject.text = item.subject.name
            rowBinding.tvTime.text = item.timeRange
            rowBinding.root.setOnClickListener { showScheduleDetailDialog(item) }
            binding.containerTodaySchedule.addView(rowBinding.root)
        }
    }

    private fun showScheduleDetailDialog(item: ScheduleResponse) {
        val dialogBinding = DialogDetailScheduleBinding.inflate(layoutInflater)
        val dialog = MaterialAlertDialogBuilder(requireContext()).setView(dialogBinding.root).create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialogBinding.tvDialogSubject.text = item.subject.name
        dialogBinding.tvDialogTeacher.text = item.teacher.fullName
        dialogBinding.tvDialogClass.text = item.classInfo.name
        dialogBinding.tvDialogDay.text = item.dayOfWeek
        dialogBinding.tvDialogTime.text = item.timeRange

        dialogBinding.btnCloseDialog.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}