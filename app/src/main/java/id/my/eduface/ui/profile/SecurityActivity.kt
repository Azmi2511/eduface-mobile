package id.my.eduface.ui.profile

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import id.my.eduface.data.network.ApiClient
import id.my.eduface.data.repository.AuthRepository
import id.my.eduface.databinding.ActivitySecurityBinding
import id.my.eduface.ui.ViewModelFactory
import id.my.eduface.utils.Resource

class SecurityActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySecurityBinding
    private lateinit var viewModel: ProfileViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySecurityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViewModel()
        setupListeners()
        observeData()
    }

    private fun setupViewModel() {
        val repository = AuthRepository(ApiClient.getApiService(this))
        val factory = ViewModelFactory(application, repository)
        viewModel = ViewModelProvider(this, factory)[ProfileViewModel::class.java]
    }

    private fun setupListeners() {
        binding.toolbar.setNavigationOnClickListener { onBackPressed() }

        binding.btnChangePassword.setOnClickListener {
            val current = binding.etCurrentPassword.text.toString().trim()
            val newPass = binding.etNewPassword.text.toString().trim()
            val confirmPass = binding.etConfirmPassword.text.toString().trim()

            if (validateInput(current, newPass, confirmPass)) {
                // Kirim ke API: current, new, dan confirmation
                viewModel.updatePassword(current, newPass, confirmPass)
            }
        }
    }

    private fun observeData() {
        viewModel.passwordResult.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    setLoading(true)
                }
                is Resource.Success -> {
                    setLoading(false)
                    Toast.makeText(this, resource.data?.message ?: "Sandi berhasil diubah!", Toast.LENGTH_LONG).show()
                    finish()
                }
                is Resource.Error -> {
                    setLoading(false)
                    Toast.makeText(this, resource.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun validateInput(current: String, new: String, confirm: String): Boolean {
        if (current.isEmpty()) {
            binding.etCurrentPassword.error = "Sandi lama wajib diisi"
            return false
        }
        if (new.length < 8) {
            binding.etNewPassword.error = "Sandi baru minimal 8 karakter"
            return false
        }
        if (new != confirm) {
            binding.etConfirmPassword.error = "Konfirmasi sandi tidak cocok"
            return false
        }
        return true
    }

    private fun setLoading(isLoading: Boolean) {
        binding.btnChangePassword.isEnabled = !isLoading
        binding.btnChangePassword.text = if (isLoading) "Memproses..." else "Ubah Kata Sandi"
    }
}