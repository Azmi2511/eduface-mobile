package id.my.eduface.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import id.my.eduface.R
import id.my.eduface.data.model.User
import id.my.eduface.data.network.ApiClient
import id.my.eduface.data.repository.AuthRepository
import id.my.eduface.databinding.FragmentProfileBinding
import id.my.eduface.ui.ViewModelFactory
import id.my.eduface.ui.login.LoginActivity
import id.my.eduface.utils.Resource
import id.my.eduface.utils.SessionManager

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager
    private lateinit var viewModel: ProfileViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager = SessionManager(requireContext())
        setupViewModel()
        initView()
        initMenu()
        observeData()
        viewModel.fetchProfile()
    }

    private fun setupViewModel() {
        val repository = AuthRepository(ApiClient.getApiService(requireContext()))
        val factory = ViewModelFactory(requireActivity().application, repository)
        viewModel = ViewModelProvider(this, factory)[ProfileViewModel::class.java]
    }

    private fun initView() {
        sessionManager.getUser()?.let { updateUI(it) }

        binding.btnLogout.setOnClickListener {
            sessionManager.logout()
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    private fun observeData() {
        viewModel.profileResult.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> { }
                is Resource.Success -> {
                    resource.data?.data?.let {
                        sessionManager.saveUser(it)
                        updateUI(it)
                    }
                }
                is Resource.Error -> {
                    Toast.makeText(context, resource.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun updateUI(user: User) {
        binding.apply {
            tvName.text = sessionManager.getUserName()
            tvUserEmail.text = sessionManager.getUserEmail()
            val role = sessionManager.getUserRole() ?: "Tamu"

            when (role.lowercase()) {
                "student" -> {
                    statsCard.visibility = View.VISIBLE
                    layoutChildCount.visibility = View.GONE
                    tvAttendanceRate.text = sessionManager.getAttendanceRate()
                }
                "parent" -> {
                    statsCard.visibility = View.VISIBLE
                    layoutChildCount.visibility = View.VISIBLE
                    tvChildCount.text = sessionManager.getChildCount().toString()
                    tvAttendanceRate.text = sessionManager.getAttendanceRate()
                }
                else -> {
                    statsCard.visibility = View.GONE
                }
            }

            Glide.with(this@ProfileFragment)
                .load(ApiClient.STORAGE_URL + sessionManager.getUserPhoto())
                .placeholder(R.drawable.ic_user)
                .error(R.drawable.ic_user)
                .circleCrop()
                .into(ivUser)
        }
    }

    private fun initMenu() {
        binding.menuInfo.apply {
            rowTitle.text = "Informasi Pribadi"
            rowIcon.setImageResource(R.drawable.ic_user)
            root.setOnClickListener {
                startActivity(Intent(requireContext(), PersonalInfoActivity::class.java))
            }
        }

        binding.menuSecurity.apply {
            rowTitle.text = "Keamanan & Sandi"
            rowIcon.setImageResource(R.drawable.ic_security)
            root.setOnClickListener {
                startActivity(Intent(requireContext(), SecurityActivity::class.java))
            }
        }

        binding.menuAbout.apply {
            rowTitle.text = "Tentang Aplikasi"
            rowIcon.setImageResource(R.drawable.ic_info)
            root.setOnClickListener {
                startActivity(Intent(requireContext(), AboutActivity::class.java))
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}