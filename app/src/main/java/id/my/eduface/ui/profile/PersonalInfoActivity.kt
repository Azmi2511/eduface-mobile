package id.my.eduface.ui.profile

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import id.my.eduface.data.network.ApiClient
import id.my.eduface.data.repository.AuthRepository
import id.my.eduface.databinding.ActivityPersonalInfoBinding
import id.my.eduface.ui.ViewModelFactory
import id.my.eduface.utils.SessionManager

class PersonalInfoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPersonalInfoBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var viewModel: ProfileViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPersonalInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        setupViewModel()
        setupToolbar()
        populateData()
        setupListeners()
    }

    private fun setupViewModel() {
        val repository = AuthRepository(ApiClient.getApiService(this))
        val factory = ViewModelFactory(application, repository)
        viewModel = ViewModelProvider(this, factory)[ProfileViewModel::class.java]
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener { onBackPressed() }
    }

    private fun populateData() {
        val user = sessionManager.getUser()
        user?.let {
            binding.etFullName.setText(it.fullName)
            binding.etEmail.setText(it.email)
            binding.etPhone.setText(it.phone ?: "")
            binding.etDob.setText(it.dob ?: "")
        }
    }

    private fun setupListeners() {
        binding.btnSave.setOnClickListener {
            val fullName = binding.etFullName.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val phone = binding.etPhone.text.toString().trim()

            if (fullName.isEmpty()) {
                binding.etFullName.error = "Nama wajib diisi"
                return@setOnClickListener
            }
            if (email.isEmpty()) {
                binding.etEmail.error = "Email wajib diisi"
                return@setOnClickListener
            }

            viewModel.updateUser(fullName, email, phone)
        }
    }
}