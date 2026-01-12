package id.my.eduface.ui.notif

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import id.my.eduface.R
import id.my.eduface.data.model.NotificationItem
import id.my.eduface.data.model.NotificationResponse
import id.my.eduface.data.network.ApiClient
import id.my.eduface.databinding.FragmentNotificationBinding
import id.my.eduface.databinding.DialogNotificationDetailBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NotificationFragment : Fragment() {

    private var _binding: FragmentNotificationBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: NotificationAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()

        binding.smartRefresh.setOnRefreshListener { loadNotifications() }
        binding.btnMarkAllRead.setOnClickListener { markAllNotificationsAsRead() }

        loadNotifications()
    }

    private fun setupRecyclerView() {
        adapter = NotificationAdapter(emptyList()) { showNotificationDetail(it) }
        binding.rvNotifications.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@NotificationFragment.adapter
        }
    }

    private fun loadNotifications() {
        if (!isAdded) return

        binding.shimmerViewContainer.visibility = View.VISIBLE
        binding.mainContentContainer.visibility = View.GONE

        ApiClient.getApiService(requireContext()).getNotifications()
            .enqueue(object : Callback<NotificationResponse> {
                override fun onResponse(call: Call<NotificationResponse>, response: Response<NotificationResponse>) {
                    if (_binding == null) return
                    stopLoading()

                    if (response.isSuccessful && response.body() != null) {
                        val list = response.body()!!.data
                        adapter.setData(list)

                        binding.layoutEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE

                        val unreadCount = list.count { !it.isRead }
                        binding.tvUnreadSummary.text = if (unreadCount > 0) "$unreadCount Pesan Baru" else "Tidak Ada Pesan"
                    }
                }

                override fun onFailure(call: Call<NotificationResponse>, t: Throwable) {
                    if (_binding == null) return
                    stopLoading()
                    binding.layoutEmpty.visibility = View.VISIBLE
                }
            })
    }

    private fun markAllNotificationsAsRead() {
        ApiClient.getApiService(requireContext()).markAllRead()
            .enqueue(object : Callback<NotificationResponse> {
                override fun onResponse(call: Call<NotificationResponse>, response: Response<NotificationResponse>) {
                    if (response.isSuccessful) {
                        Toast.makeText(context, "Semua pesan telah dibaca", Toast.LENGTH_SHORT).show()
                        loadNotifications()
                    }
                }
                override fun onFailure(call: Call<NotificationResponse>, t: Throwable) {}
            })
    }

    private fun showNotificationDetail(item: NotificationItem) {
        val dialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme)
        val dialogBinding = DialogNotificationDetailBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)

        dialogBinding.apply {
            tvDialogTitle.text = "Detail Notifikasi"
            tvDialogMessage.text = item.message
            tvDialogTime.text = item.timeAgo
            btnCloseDialog.setOnClickListener { dialog.dismiss() }
        }

        if (!item.isRead) markAsRead(item.id)
        dialog.show()
    }

    private fun markAsRead(id: Int) {
        ApiClient.getApiService(requireContext()).markAsRead(id).enqueue(object : Callback<NotificationResponse> {
            override fun onResponse(call: Call<NotificationResponse>, response: Response<NotificationResponse>) {
                if (response.isSuccessful) loadNotifications()
            }
            override fun onFailure(call: Call<NotificationResponse>, t: Throwable) {}
        })
    }

    private fun stopLoading() {
        binding.shimmerViewContainer.stopShimmer()
        binding.shimmerViewContainer.visibility = View.GONE
        binding.mainContentContainer.visibility = View.VISIBLE
        binding.smartRefresh.finishRefresh()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}