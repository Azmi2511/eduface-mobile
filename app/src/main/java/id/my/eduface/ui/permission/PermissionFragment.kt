package id.my.eduface.ui.permission

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import java.util.Date
import java.text.SimpleDateFormat
import java.util.Locale
import id.my.eduface.R
import id.my.eduface.data.model.PermissionItem
import id.my.eduface.data.model.PermissionResponse
import id.my.eduface.data.network.ApiClient
import id.my.eduface.databinding.FragmentPermissionBinding
import id.my.eduface.utils.SessionManager
import id.my.eduface.utils.ToastUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PermissionFragment : Fragment() {

    private var _binding: FragmentPermissionBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPermissionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionManager = SessionManager(requireContext())

        setupRecyclerView()
        setupHeaderView()

        binding.smartRefresh.setOnRefreshListener { fetchPermissions() }

        val role = sessionManager.getUserRole()?.lowercase()

        if (role == "student") {
            binding.fabAddPermission.visibility = View.GONE
        } else {
            binding.fabAddPermission.visibility = View.VISIBLE
            binding.fabAddPermission.setOnClickListener {
                parentFragmentManager.beginTransaction()
                    .replace(R.id.nav_host_fragment, AddPermissionFragment())
                    .addToBackStack(null)
                    .commit()
            }
        }

        showLoading(true)
        fetchPermissions()
    }

    private fun setupRecyclerView() {
        binding.rvPermission.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun setupHeaderView() {
        val dateNow = Date()
        val format = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("id", "ID"))
        binding.tvCurrentDate.text = format.format(dateNow)
    }

    private fun fetchPermissions() {
        val role = sessionManager.getUserRole()?.lowercase()
        val selectedId = sessionManager.fetchSelectedChildId()

        if (role == "parent" && selectedId == 0) {
            showLoading(false)
            binding.layoutEmpty.visibility = View.VISIBLE
            binding.rvPermission.visibility = View.GONE
            return
        }

        val queryId: Int? = if (role == "student") null else selectedId

        ApiClient.getApiService(requireContext()).getPermissions(queryId)
            .enqueue(object : Callback<PermissionResponse> {
                override fun onResponse(call: Call<PermissionResponse>, response: Response<PermissionResponse>) {
                    showLoading(false)

                    if (response.isSuccessful && response.body() != null) {
                        val list = response.body()!!.data

                        updateTodayStatus(list)

                        if (list.isEmpty()) {
                            binding.layoutEmpty.visibility = View.VISIBLE
                            binding.rvPermission.visibility = View.GONE
                        } else {
                            binding.layoutEmpty.visibility = View.GONE
                            binding.rvPermission.visibility = View.VISIBLE

                            val adapter = PermissionAdapter(list)
                            binding.rvPermission.adapter = adapter
                        }
                    } else {
                        binding.layoutEmpty.visibility = View.VISIBLE
                        binding.rvPermission.visibility = View.GONE
                        ToastUtils.showError(requireContext(), "Gagal memuat: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<PermissionResponse>, t: Throwable) {
                    showLoading(false)
                    binding.layoutEmpty.visibility = View.VISIBLE
                    binding.rvPermission.visibility = View.GONE
                    ToastUtils.showError(requireContext(), "Koneksi gagal. Periksa internet.")
                }
            })
    }

    private fun updateTodayStatus(list: List<PermissionItem>) {
        if (_binding == null) return

        binding.containerTodayPermission.removeAllViews()

        val apiFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val todayStr = apiFormat.format(Date())

        val todayPermission = list.find {
            val start = it.dateRange?.start ?: ""
            val end = it.dateRange?.end ?: ""
            (todayStr >= start) && (todayStr <= end) && (it.approvalStatus == "Approved")
        }

        val statusTextView = TextView(requireContext())
        statusTextView.textSize = 14f
        statusTextView.typeface = ResourcesCompat.getFont(requireContext(), R.font.poppins_medium)

        if (todayPermission != null) {
            val reason = todayPermission.description ?: "Tanpa Keterangan"
            statusTextView.text = "Sedang ${todayPermission.type}: $reason"
            statusTextView.setTextColor(ContextCompat.getColor(requireContext(), R.color.warning_orange))
        } else {
            statusTextView.text = "Tidak ada izin (Hadir/Masuk)"
            statusTextView.setTextColor(ContextCompat.getColor(requireContext(), R.color.success_green))
        }

        binding.containerTodayPermission.addView(statusTextView)
    }

    private fun showLoading(isLoading: Boolean) {
        if (_binding == null) return

        if (isLoading) {
            binding.shimmerViewContainer.visibility = View.VISIBLE
            binding.mainContentContainer.visibility = View.GONE
        } else {
            binding.shimmerViewContainer.visibility = View.GONE
            binding.mainContentContainer.visibility = View.VISIBLE
            binding.smartRefresh.finishRefresh()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}