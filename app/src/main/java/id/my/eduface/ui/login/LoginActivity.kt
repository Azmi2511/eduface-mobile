package id.my.eduface.ui.login

import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModelProvider
import id.my.eduface.MainActivity
import id.my.eduface.R
import id.my.eduface.data.network.ApiClient
import id.my.eduface.data.repository.AuthRepository
import id.my.eduface.databinding.ActivityLoginBinding
import id.my.eduface.ui.ViewModelFactory
import id.my.eduface.ui.register.RegisterActivity
import id.my.eduface.utils.Resource
import id.my.eduface.utils.SessionManager
import id.my.eduface.utils.ToastUtils

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var viewModel: LoginViewModel
    private lateinit var sessionManager: SessionManager
    private var isPasswordVisible = false
    private val TAG = "LoginActivity_Debug"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        if (sessionManager.fetchAuthToken() != null) {
            navigateToHome()
        }

        setupViewModel()
        setupAction()
    }

    private fun setupViewModel() {
        val apiService = ApiClient.getApiService(this)
        val repository = AuthRepository(apiService)
        val factory = ViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[LoginViewModel::class.java]
    }

    private fun setupAction() {
        binding.ivTogglePassword.setOnClickListener {
            togglePasswordVisibility()
        }

        binding.tvRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        binding.btnLogin.setOnClickListener {
            val loginInput = binding.etUsername.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (loginInput.isNotEmpty() && password.isNotEmpty()) {
                viewModel.login(loginInput, password)
            } else {
                ToastUtils.showError(this, "Username dan Password harus diisi!")
            }
        }

        viewModel.loginResult.observe(this) { result ->
            when (result) {
                is Resource.Loading -> showLoading(true)
                is Resource.Success -> {
                    showLoading(false)
                    val response = result.data
                    val token = response?.accessToken
                    val user = response?.user

                    Log.d(TAG, "Login Berhasil - User: ${user?.fullName}, Role: ${user?.role}")

                    Log.d(TAG, "Data Student: ${user?.student}")
                    Log.d(TAG, "Data Parent: ${user?.parent}")

                    if (token != null && user != null) {
                        sessionManager.saveAuthToken(token)

                        sessionManager.createLoginSession(
                            name = user.fullName ?: "User",
                            role = user.role ?: "User",
                            email = user.email ?: "user@eduface.id",
                            photoUrl = user.profilePicture,
                            childCount = 0
                        )

                        when (user.role?.lowercase()) {
                            "student" -> {
                                val studentNisn = user.student?.nisn ?: ""
                                sessionManager.saveSelectedStudentNisn(studentNisn)
                                Log.d("Login", "Siswa terdeteksi, NISN disimpan: $studentNisn")
                            }
                            "teacher" -> {
                                val teacherId = user.teacher?.id ?: 0
                                // sessionManager.saveTeacherId(teacherId)
                            }
                        }

                        ToastUtils.showSuccess(this, "Login Berhasil! Halo ${user.fullName}")
                        navigateToHome()
                    }
                }
                is Resource.Error -> {
                    showLoading(false)
                    Log.e(TAG, "LOGIN ERROR DETECTED!")
                    Log.e(TAG, "Message: ${result.message}")
                    ToastUtils.showError(this, result.message ?: "Terjadi kesalahan")
                }
            }
        }
    }

    private fun togglePasswordVisibility() {
        if (isPasswordVisible) {
            binding.etPassword.transformationMethod = PasswordTransformationMethod.getInstance()
            binding.ivTogglePassword.setImageResource(R.drawable.ic_eye_visible)
        } else {
            binding.etPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
            binding.ivTogglePassword.setImageResource(R.drawable.ic_eye_off)
        }
        isPasswordVisible = !isPasswordVisible
        binding.etPassword.setSelection(binding.etPassword.text.length)
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.progressBar.visibility = View.VISIBLE
            binding.btnLogin.isEnabled = false
            binding.btnLogin.alpha = 0.5f
        } else {
            binding.progressBar.visibility = View.GONE
            binding.btnLogin.isEnabled = true
            binding.btnLogin.alpha = 1.0f
        }
    }

    private fun navigateToHome() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}