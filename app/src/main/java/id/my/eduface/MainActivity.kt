package id.my.eduface

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.ViewGroup
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.FirebaseApp
import id.my.eduface.data.model.DashboardResponse
import id.my.eduface.data.model.DefaultResponse
import id.my.eduface.data.model.NotificationResponse
import id.my.eduface.data.model.NotificationItem
import id.my.eduface.data.model.UnreadCountResponse
import id.my.eduface.data.network.ApiClient
import id.my.eduface.ui.login.LoginActivity
import id.my.eduface.utils.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private lateinit var tvUserName: TextView
    private lateinit var tvUserRole: TextView
    private lateinit var sivProfilePicture: ShapeableImageView
    private lateinit var ivNotification: ImageView
    private lateinit var cvProfileChip: MaterialCardView
    private lateinit var tvNotificationBadge: TextView

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) syncFcmToken()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebaseApp.initializeApp(this)

        setContentView(R.layout.activity_main)

        sessionManager = SessionManager(this)
        tvUserName = findViewById(R.id.tv_user_name)
        tvUserRole = findViewById(R.id.tv_user_role)
        sivProfilePicture = findViewById(R.id.siv_profile_picture)
        ivNotification = findViewById(R.id.iv_notification)
        cvProfileChip = findViewById(R.id.cv_profile_chip)
        tvNotificationBadge = findViewById(R.id.tv_notification_badge)

        setupHeaderData()
        setupProfileDropdown()
        setupNotificationPopup()
        checkNotificationPermission()
        fetchUnreadCount()
        initLogic()

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav)
        bottomNav.setupWithNavController(navController)
    }

    private fun initLogic(){
        val role = sessionManager.getUserRole()?.lowercase()

        if (role == "parent"){
            fetchChildCount()
        }
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                syncFcmToken()
            } else {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            syncFcmToken()
        }
    }

    private fun syncFcmToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("FCM_TOKEN", "Fetching FCM registration token failed", task.exception)
                return@addOnCompleteListener
            }

            val token = task.result

            ApiClient.getApiService(this).updateFcmToken(token)
                .enqueue(object : Callback<DefaultResponse> {
                    override fun onResponse(call: Call<DefaultResponse>, response: Response<DefaultResponse>) {
                        if (response.isSuccessful) Log.d("FCM", "Token synced: $token")
                    }
                    override fun onFailure(call: Call<DefaultResponse>, t: Throwable) {
                        Log.e("FCM", "Sync failed: ${t.message}")
                    }
                })
        }
    }

    private fun setupHeaderData() {
        tvUserName.text = sessionManager.getUserName() ?: "Pengguna"
        tvUserRole.text = when (sessionManager.getUserRole()?.lowercase()) {
            "parent" -> "Orang Tua"
            "teacher" -> "Guru"
            "student" -> "Siswa"
            else -> "Pengguna"
        }

        Glide.with(this)
            .load(ApiClient.STORAGE_URL + sessionManager.getUserPhoto())
            .placeholder(R.drawable.img_user_placeholder)
            .into(sivProfilePicture)
    }

    private fun setupProfileDropdown() {
        cvProfileChip.setOnClickListener {
            val popupView = layoutInflater.inflate(R.layout.layout_user_menu, null)
            val popupWindow = PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true)
            popupWindow.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
            val navController = navHostFragment.navController

            popupView.findViewById<LinearLayout>(R.id.menu_profile).setOnClickListener {
                navController.navigate(R.id.menu_profile)
                popupWindow.dismiss()
            }

            popupView.findViewById<LinearLayout>(R.id.menu_logout).setOnClickListener {
                sessionManager.logout()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
            popupWindow.showAsDropDown(cvProfileChip, 0, 20, Gravity.END)
        }
    }

    private fun setupNotificationPopup() {
        ivNotification.setOnClickListener { view ->
            val popupView = layoutInflater.inflate(R.layout.layout_popup_notification, null)
            val popupWindow = PopupWindow(popupView, 700, ViewGroup.LayoutParams.WRAP_CONTENT, true)
            popupWindow.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            val container = popupView.findViewById<LinearLayout>(R.id.container_notifications)
            val tvEmpty = popupView.findViewById<LinearLayout>(R.id.tv_empty_notification)
            val progressBar = popupView.findViewById<android.widget.ProgressBar>(R.id.progress_bar)

            val btnMarkAllRead = popupView.findViewById<TextView>(R.id.tv_mark_all_read)
            btnMarkAllRead?.setOnClickListener {
                markAllRead(popupWindow)
            }

            ApiClient.getApiService(this).getNotifications()
                .enqueue(object : Callback<NotificationResponse> {
                    override fun onResponse(call: Call<NotificationResponse>, response: Response<NotificationResponse>) {
                        progressBar.visibility = View.GONE
                        if (response.isSuccessful && response.body() != null) {
                            val notifications = response.body()!!.data
                            if (notifications.isEmpty()) {
                                tvEmpty.visibility = View.VISIBLE
                            } else {
                                populateNotificationList(container, notifications, popupWindow)
                            }
                        }
                    }
                    override fun onFailure(call: Call<NotificationResponse>, t: Throwable) {
                        progressBar.visibility = View.GONE
                        Log.e("Notification", "Error: ${t.message}")
                    }
                })

            popupWindow.showAsDropDown(view, -500, 20, Gravity.END)
        }
    }

    private fun populateNotificationList(container: LinearLayout, list: List<NotificationItem>, popup: PopupWindow) {
        container.removeAllViews()
        list.take(5).forEach { item ->
            val itemView = layoutInflater.inflate(R.layout.item_notification_row, container, false)
            val tvMessage = itemView.findViewById<TextView>(R.id.tv_notification_message)
            val tvTime = itemView.findViewById<TextView>(R.id.tv_notification_time)
            val indicator = itemView.findViewById<View>(R.id.view_unread_indicator)

            tvMessage.text = item.message
            tvTime.text = item.timeAgo
            indicator.visibility = if (item.isRead) View.GONE else View.VISIBLE

            itemView.setOnClickListener {
                if (!item.isRead) markAsRead(item.id)
                popup.dismiss()
            }

            container.addView(itemView)
        }
    }

    private fun fetchChildCount() {
        if (sessionManager.getUserRole()?.lowercase() == "parent") {
            ApiClient.getApiService(this).getDashboard()
                .enqueue(object : Callback<DashboardResponse> {
                    override fun onResponse(call: Call<DashboardResponse>, response: Response<DashboardResponse>) {
                        if (response.isSuccessful) {
                            val listAnak = response.body()?.children ?: emptyList()
                            val jumlahAnak = listAnak.size

                            sessionManager.saveChildCount(jumlahAnak)

                            if (listAnak.isNotEmpty() && sessionManager.fetchSelectedChildId() == 0) {
                                sessionManager.saveSelectedChildId(listAnak[0].id)
                                sessionManager.saveSelectedStudentNisn(listAnak[0].nisn ?: "")
                            }
                        }
                    }

                    override fun onFailure(call: Call<DashboardResponse>, t: Throwable) {
                        Log.e("EduFace", "Koneksi gagal saat ambil data anak: ${t.message}")
                    }
                })
        }
    }
    private fun fetchUnreadCount() {
        ApiClient.getApiService(this).getUnreadCount()
            .enqueue(object : Callback<UnreadCountResponse> {
                override fun onResponse(call: Call<UnreadCountResponse>, response: Response<UnreadCountResponse>) {
                    if (response.isSuccessful && response.body() != null) {
                        val count = response.body()!!.unreadCount
                        updateNotificationBadge(count)
                    }
                }
                override fun onFailure(call: Call<UnreadCountResponse>, t: Throwable) {
                    Log.e("Notification", "Gagal mengambil jumlah notifikasi: ${t.message}")
                }
            })
    }

    private fun updateNotificationBadge(count: Int) {
        if (count > 0) {
            tvNotificationBadge.visibility = View.VISIBLE
            tvNotificationBadge.text = if (count > 9) "9+" else count.toString()
        } else {
            tvNotificationBadge.visibility = View.GONE
        }
    }

    private fun markAsRead(id: Int) {
        ApiClient.getApiService(this).markAsRead(id)
            .enqueue(object : Callback<NotificationResponse> {
                override fun onResponse(call: Call<NotificationResponse>, response: Response<NotificationResponse>) {
                    if (response.isSuccessful) {
                        fetchUnreadCount()
                    }
                }
                override fun onFailure(call: Call<NotificationResponse>, t: Throwable) {
                    Log.e("Notification", "Gagal menandai baca: ${t.message}")
                }
            })
    }
    private fun markAllRead(popupWindow: PopupWindow) {
        ApiClient.getApiService(this).markAllRead()
            .enqueue(object : Callback<NotificationResponse> {
                override fun onResponse(call: Call<NotificationResponse>, response: Response<NotificationResponse>) {
                    if (response.isSuccessful) {
                        fetchUnreadCount()
                        popupWindow.dismiss()
                        Log.d("Notification", "Semua berhasil ditandai baca")
                    }
                }

                override fun onFailure(call: Call<NotificationResponse>, t: Throwable) {
                    Log.e("Notification", "Gagal tandai semua: ${t.message}")
                }
            })
    }
}