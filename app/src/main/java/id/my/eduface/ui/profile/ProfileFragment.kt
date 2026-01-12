package id.my.eduface.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.content.Intent
import androidx.fragment.app.Fragment
import id.my.eduface.R
import id.my.eduface.data.network.ApiClient
import id.my.eduface.databinding.FragmentProfileBinding
import id.my.eduface.utils.SessionManager
import id.my.eduface.ui.login.LoginActivity

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager = SessionManager(requireContext())

        initView()
        initMenu()
    }

    private fun initView() {
        binding.tvName.text = sessionManager.getUserName()
        binding.tvUserEmail.text = sessionManager.getUserEmail()
        binding.tvChildCount.text = sessionManager.getChildCount().toString()

        val attendanceRate = sessionManager.getAttendanceRate()
        val photoUrl = ApiClient.STORAGE_URL + sessionManager.getUserPhoto()

        binding.tvAttendanceRate.text = attendanceRate

        com.bumptech.glide.Glide.with(this)
            .load(photoUrl)
            .placeholder(R.drawable.ic_user)
            .error(R.drawable.ic_user)
            .circleCrop()
            .into(binding.ivUser)

        binding.btnLogout.setOnClickListener {
            sessionManager.logout()
             val intent = Intent(requireContext(), LoginActivity::class.java)
             intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
             startActivity(intent)
        }
    }

    private fun initMenu() {
        binding.menuInfo.apply {
            rowTitle.text = "Informasi Pribadi"
            rowIcon.setImageResource(R.drawable.ic_user)
            root.setOnClickListener { }
        }

        binding.menuSecurity.apply {
            rowTitle.text = "Keamanan & Sandi"
            rowIcon.setImageResource(R.drawable.ic_security)
            root.setOnClickListener { }
        }

        binding.menuAbout.apply {
            rowTitle.text = "Tentang Aplikasi"
            rowIcon.setImageResource(R.drawable.ic_notifications)
            root.setOnClickListener { }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}