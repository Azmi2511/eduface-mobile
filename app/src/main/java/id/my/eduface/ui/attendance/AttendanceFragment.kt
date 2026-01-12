package id.my.eduface.ui.attendance

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.datepicker.MaterialDatePicker
import id.my.eduface.R
import id.my.eduface.data.model.AttendanceItem
import id.my.eduface.data.model.AttendanceWrapper
import id.my.eduface.data.network.RetrofitClient
import id.my.eduface.databinding.DialogAttendanceDetailBinding
import id.my.eduface.databinding.FragmentAttendanceBinding
import id.my.eduface.databinding.ItemTodayScheduleRowBinding
import id.my.eduface.utils.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.SocketTimeoutException
import java.text.SimpleDateFormat
import java.util.*

class AttendanceFragment : Fragment() {

    private var _binding: FragmentAttendanceBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: AttendanceAdapter
    private lateinit var session: SessionManager
    private var selectedFilterDate: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAttendanceBinding.inflate(inflater, container, false)
        session = SessionManager(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupDateHeader()

        binding.btnFilterDate.setOnClickListener { showDatePicker() }

        binding.btnClearFilter.setOnClickListener {
            selectedFilterDate = null
            binding.tvFilterStatus.text = "Riwayat Terkini"
            binding.btnClearFilter.visibility = View.GONE
            loadHistoryData(null)
        }

        binding.smartRefresh.setOnRefreshListener {
            loadTodayHighlight()
            loadHistoryData(selectedFilterDate)
        }
    }

    override fun onResume() {
        super.onResume()
        val currentNisn = session.fetchSelectedStudentNisn() ?: ""

        if (currentNisn.isNotEmpty()) {
            loadTodayHighlight()
            loadHistoryData(selectedFilterDate)
        } else {
            binding.layoutEmpty.visibility = View.VISIBLE
        }
    }

    private fun setupDateHeader() {
        val sdf = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("id", "ID"))
        binding.tvCurrentDate.text = sdf.format(Date())
    }

    private fun setupRecyclerView() {
        adapter = AttendanceAdapter(emptyList()) { item -> showAttendanceDetail(item) }
        binding.rvAttendanceHistory.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@AttendanceFragment.adapter
            isNestedScrollingEnabled = false
        }
    }

    private fun loadTodayHighlight() {
        val token = "Bearer ${session.fetchAuthToken()}"
        val nisn = session.fetchSelectedStudentNisn() ?: ""
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

        if (nisn.isEmpty()) return

        RetrofitClient.instance.getAttendance(token, nisn, today).enqueue(object : Callback<AttendanceWrapper> {
            override fun onResponse(call: Call<AttendanceWrapper>, response: Response<AttendanceWrapper>) {
                if (_binding == null) return
                if (response.isSuccessful) {
                    val list = response.body()?.data ?: emptyList()
                    populateTodayHighlight(list)
                }
            }

            override fun onFailure(call: Call<AttendanceWrapper>, t: Throwable) {
            }
        })
    }

    private fun loadHistoryData(date: String?) {
        if (!isAdded) return

        val nisn = session.fetchSelectedStudentNisn() ?: ""
        val token = session.fetchAuthToken() ?: ""

        if (!binding.smartRefresh.isRefreshing) {
            binding.shimmerViewContainer.visibility = View.VISIBLE
            binding.mainContentContainer.visibility = View.GONE
            binding.shimmerViewContainer.startShimmer()
        }

        RetrofitClient.instance.getAttendance("Bearer $token", nisn, date).enqueue(object : Callback<AttendanceWrapper> {
            override fun onResponse(call: Call<AttendanceWrapper>, response: Response<AttendanceWrapper>) {
                if (_binding == null) return
                stopAllLoading()

                if (response.isSuccessful) {
                    val list = response.body()?.data ?: emptyList()
                    adapter.setData(list)

                    binding.layoutEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
                    binding.rvAttendanceHistory.visibility = if (list.isEmpty()) View.GONE else View.VISIBLE
                } else {
                    Toast.makeText(context, "Gagal memuat data", Toast.LENGTH_SHORT).show()
                    binding.layoutEmpty.visibility = View.VISIBLE
                }
            }

            override fun onFailure(call: Call<AttendanceWrapper>, t: Throwable) {
                if (_binding == null) return
                stopAllLoading()

                if (t is SocketTimeoutException) {
                    Toast.makeText(context, "Koneksi lambat, silakan coba lagi", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, "Terjadi kesalahan koneksi", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun stopAllLoading() {
        binding.shimmerViewContainer.stopShimmer()
        binding.shimmerViewContainer.visibility = View.GONE
        binding.mainContentContainer.visibility = View.VISIBLE
        binding.smartRefresh.finishRefresh()
    }

    private fun populateTodayHighlight(list: List<AttendanceItem>) {
        binding.containerTodayAttendance.removeAllViews()
        if (list.isEmpty()) {
            val emptyTv = TextView(context).apply {
                text = "Belum ada absensi hari ini"
                textSize = 13f
                gravity = android.view.Gravity.CENTER
                setPadding(0, 20, 0, 20)
            }
            binding.containerTodayAttendance.addView(emptyTv)
            return
        }

        list.take(2).forEach { item ->
            val row = ItemTodayScheduleRowBinding.inflate(layoutInflater, binding.containerTodayAttendance, false)
            row.tvSubject.text = item.schedule.subject ?: "Masuk Sekolah"
            row.tvTime.text = "${item.attendance.status} - ${item.attendance.time}"
            row.root.setOnClickListener { showAttendanceDetail(item) }
            binding.containerTodayAttendance.addView(row.root)
        }
    }

    private fun showDatePicker() {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Pilih Tanggal")
            .build()

        datePicker.addOnPositiveButtonClickListener { selection ->
            val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
            calendar.timeInMillis = selection
            val apiDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(calendar.time)
            val uiDate = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID")).format(calendar.time)

            selectedFilterDate = apiDate
            binding.tvFilterStatus.text = "Riwayat: $uiDate"
            binding.btnClearFilter.visibility = View.VISIBLE
            loadHistoryData(apiDate)
        }
        datePicker.show(childFragmentManager, "DATE_PICKER")
    }

    private fun showAttendanceDetail(item: AttendanceItem) {
        val dialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme)
        val dialogBinding = DialogAttendanceDetailBinding.inflate(layoutInflater)

        dialog.setContentView(dialogBinding.root)

        dialogBinding.apply {
            tvDialogSubject.text = item.schedule.subject ?: "Masuk Sekolah"
            tvDialogDay.text = item.attendance.date
            tvDialogTime.text = item.attendance.time
            tvDialogTeacher.text = item.schedule.teacher ?: "Sistem / Wali Kelas"
            tvDialogClass.text = item.schedule.className ?: "Kelas Utama"

            btnCloseDialog.setOnClickListener {
                dialog.dismiss()
            }
        }

        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}