package id.my.eduface.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import id.my.eduface.R
import id.my.eduface.data.model.*
import id.my.eduface.data.network.ApiClient
import id.my.eduface.databinding.FragmentHomeBinding
import id.my.eduface.utils.SessionManager
import id.my.eduface.utils.ToastUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager
    private lateinit var attendanceLogAdapter: AttendanceLogAdapter

    private var childrenList: List<Child> = ArrayList()
    private var allAttendanceLogs: List<AttendanceLog> = ArrayList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager = SessionManager(requireContext())

        binding.smartRefresh.setEnableScrollContentWhenLoaded(true)
        binding.smartRefresh.setOnRefreshListener { fetchDashboardData() }

        setupRecyclerView()
        showInitialLoading()
        fetchDashboardData()
    }

    private fun setupRecyclerView() {
        attendanceLogAdapter = AttendanceLogAdapter(emptyList())
        binding.rvNotifications.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = attendanceLogAdapter
        }
    }

    private fun showInitialLoading() {
        binding.shimmerViewContainer.visibility = View.VISIBLE
        binding.shimmerViewContainer.startShimmer()
        binding.mainContentContainer.visibility = View.GONE
        binding.layoutEmptyState.visibility = View.GONE
    }

    private fun fetchDashboardData() {
        val token = sessionManager.fetchAuthToken()
        if (token == null) {
            stopLoading()
            return
        }

        ApiClient.getApiService(requireContext()).getDashboard()
            .enqueue(object : Callback<DashboardResponse> {
                override fun onResponse(call: Call<DashboardResponse>, response: Response<DashboardResponse>) {
                    stopLoading()

                    if (response.isSuccessful && response.body() != null) {
                        val data = response.body()!!
                        allAttendanceLogs = data.attendanceLogs ?: emptyList()
                        updateDashboardUI(data)
                    }
                }

                override fun onFailure(call: Call<DashboardResponse>, t: Throwable) {
                    if (_binding == null) return
                    stopLoading()
                    ToastUtils.showError(requireContext(), "Gagal memuat data")
                }
            })
    }

    private fun updateDashboardUI(data: DashboardResponse) {
        val role = sessionManager.getUserRole()?.lowercase()
        binding.tvUserName.text = sessionManager.getUserName()

        if (role == "student") {
            binding.spinnerAnak.visibility = View.GONE
            binding.cvStudentSelector.visibility = View.GONE

            updateStatisticsUI(data.statistics)
            displayLogs(allAttendanceLogs)

        } else if (role == "parent" && data.children.isNotEmpty()) {
            binding.spinnerAnak.visibility = View.VISIBLE
            binding.cvStudentSelector.visibility = View.VISIBLE

            if (data.children.isNotEmpty()) {
                childrenList = data.children

                val firstChild = childrenList[0]
                sessionManager.saveSelectedStudentNisn(firstChild.nisn ?: "")
                sessionManager.saveSelectedClassId(firstChild.classId)

                val childNames = childrenList.map { it.name }
                val adapter = ArrayAdapter(requireContext(), R.layout.item_spinner_poppins, childNames)
                adapter.setDropDownViewResource(R.layout.item_spinner_poppins)
                binding.spinnerAnak.adapter = adapter

                updateStatisticsUI(firstChild.statistics)
                filterLogsByChild(firstChild)
                setupSpinnerListener()
            }
        } else{
            binding.cvStudentSelector.visibility = View.GONE
            updateStatisticsUI(data.statistics)
        }
    }

    private fun displayLogs(logs: List<AttendanceLog>) {
        if (logs.isEmpty()) {
            binding.rvNotifications.visibility = View.GONE
            binding.layoutEmptyState.visibility = View.VISIBLE
        } else {
            binding.rvNotifications.visibility = View.VISIBLE
            binding.layoutEmptyState.visibility = View.GONE
            attendanceLogAdapter = AttendanceLogAdapter(logs)
            binding.rvNotifications.adapter = attendanceLogAdapter
        }
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

    private fun setupSpinnerListener() {
        binding.spinnerAnak.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (childrenList.isNotEmpty()) {
                    val selectedChild = childrenList[position]
                    val nisnToSave = selectedChild.nisn ?: ""

                    sessionManager.saveSelectedClassId(selectedChild.classId)
                    sessionManager.saveSelectedStudentNisn(nisnToSave)
                    sessionManager.saveSelectedChildId(selectedChild.id)

                    updateStatisticsUI(selectedChild.statistics)
                    filterLogsByChild(selectedChild)
                }
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }
    }

    private fun updateStatisticsUI(stats: Statistics) {
        binding.tvStatAttendance.text = stats.attendancePercentage
        binding.tvStatLate.text = stats.lateCount.toString()
        binding.tvStatAbsent.text = stats.absentCount.toString()
        binding.tvStatPermission.text = stats.permissionCount.toString()

        sessionManager.saveAttendanceRate(stats.attendancePercentage)
    }

    private fun filterLogsByChild(selectedChild: Child) {
        val filtered = allAttendanceLogs.filter { log ->
            log.studentName == selectedChild.name
        }

        if (filtered.isEmpty()) {
            binding.rvNotifications.visibility = View.GONE
            binding.layoutEmptyState.visibility = View.VISIBLE
        } else {
            binding.rvNotifications.visibility = View.VISIBLE
            binding.layoutEmptyState.visibility = View.GONE
            attendanceLogAdapter = AttendanceLogAdapter(filtered)
            binding.rvNotifications.adapter = attendanceLogAdapter
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}