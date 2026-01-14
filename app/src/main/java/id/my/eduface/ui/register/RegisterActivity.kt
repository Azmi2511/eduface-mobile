package id.my.eduface.ui.register

import android.app.AlertDialog
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import id.my.eduface.R
import id.my.eduface.data.model.ParentOption
import id.my.eduface.data.model.SchoolClass
import id.my.eduface.data.network.ApiClient
import id.my.eduface.data.repository.AuthRepository
import id.my.eduface.databinding.ActivityRegisterBinding
import id.my.eduface.ui.ViewModelFactory
import id.my.eduface.utils.Resource

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var viewModel: RegisterViewModel

    private var selectedRole = "student"
    private var selectedGender = "L"
    private var selectedClassId: Int? = null
    private var selectedParentId: Int? = null
    private var selectedPhotoUri: Uri? = null

    private var otpDialog: AlertDialog? = null

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            selectedPhotoUri = uri
            binding.tvPhotoName.text = "Foto terpilih!"
            binding.tvPhotoName.setTextColor(resources.getColor(R.color.teal_700, theme))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViewModel()
        setupUI()
        observeData()

        viewModel.fetchRegisterData()
    }

    private fun setupViewModel() {
        val repository = AuthRepository(ApiClient.getApiService(this))
        val factory = ViewModelFactory(application, repository)
        viewModel = ViewModelProvider(this, factory)[RegisterViewModel::class.java]
    }

    private fun setupUI() {
        val roles = arrayOf("Siswa", "Guru", "Wali Murid")
        val roleAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, roles)
        binding.spinnerRole.adapter = roleAdapter
        binding.spinnerRole.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                when (position) {
                    0 -> {
                        selectedRole = "student"
                        binding.layoutStudentExtras.visibility = View.VISIBLE
                        binding.layoutIdNumber.visibility = View.VISIBLE
                        binding.layoutIdNumber.hint = "NISN"
                    }
                    1 -> {
                        selectedRole = "teacher"
                        binding.layoutStudentExtras.visibility = View.GONE
                        binding.layoutIdNumber.visibility = View.VISIBLE
                        binding.layoutIdNumber.hint = "NIP"
                    }
                    2 -> {
                        selectedRole = "parent"
                        binding.layoutStudentExtras.visibility = View.GONE
                        binding.layoutIdNumber.visibility = View.GONE
                    }
                }
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }

        val genders = arrayOf("Laki-laki", "Perempuan")
        val genderAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, genders)
        binding.spinnerGender.adapter = genderAdapter
        binding.spinnerGender.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                selectedGender = if (position == 0) "L" else "P"
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }

        binding.btnUploadPhoto.setOnClickListener { pickImage.launch("image/*") }
        binding.btnRegister.setOnClickListener { validateAndSubmit() }
        binding.tvLoginLink.setOnClickListener { finish() }
    }

    private fun observeData() {
        viewModel.registerData.observe(this) { result ->
            when (result) {
                is Resource.Loading -> { }
                is Resource.Success -> {
                    result.data?.let { setupDynamicSpinners(it.classes, it.parents) }
                }
                is Resource.Error -> {
                    Toast.makeText(this, "Gagal memuat data: ${result.message}", Toast.LENGTH_LONG).show()
                }
            }
        }

        viewModel.otpResult.observe(this) { result ->
            when (result) {
                is Resource.Loading -> {
                    binding.btnRegister.isEnabled = false
                    binding.btnRegister.text = "Mengirim Data..."
                }
                is Resource.Success -> {
                    binding.btnRegister.isEnabled = true
                    binding.btnRegister.text = "Daftar"
                    showOtpDialog(result.data?.message ?: "OTP Terkirim")
                    viewModel.resetOtpResult()
                }
                is Resource.Error -> {
                    binding.btnRegister.isEnabled = true
                    binding.btnRegister.text = "Daftar"
                    Toast.makeText(this, result.message, Toast.LENGTH_LONG).show()
                }
            }
        }

        viewModel.finalRegisterResult.observe(this) { result ->
            when (result) {
                is Resource.Loading -> {
                    otpDialog?.findViewById<View>(R.id.btn_verify_otp)?.isEnabled = false
                }
                is Resource.Success -> {
                    otpDialog?.findViewById<View>(R.id.btn_verify_otp)?.isEnabled = true
                    otpDialog?.dismiss()
                    Toast.makeText(this, "Registrasi Berhasil! Silakan Login.", Toast.LENGTH_LONG).show()
                    finish()
                }
                is Resource.Error -> {
                    otpDialog?.findViewById<View>(R.id.btn_verify_otp)?.isEnabled = true
                    Toast.makeText(this, "Gagal: ${result.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun setupDynamicSpinners(classes: List<SchoolClass>, parents: List<ParentOption>) {
        val classAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, classes)
        binding.spinnerClass.adapter = classAdapter
        binding.spinnerClass.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, pos: Int, p3: Long) {
                selectedClassId = classes[pos].id
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }

        val parentAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, parents)
        binding.spinnerParent.adapter = parentAdapter
        binding.spinnerParent.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, pos: Int, p3: Long) {
                selectedParentId = parents[pos].id
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }
    }

    private fun validateAndSubmit() {
        val name = binding.etFullname.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val confirm = binding.etConfirmPassword.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim()
        val idNumber = binding.etIdNumber.text.toString().trim()

        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Harap isi semua field wajib", Toast.LENGTH_SHORT).show()
            return
        }

        viewModel.processRegister(
            name, email, password, confirm,
            selectedRole, selectedGender, phone, idNumber,
            if (selectedRole == "student") selectedClassId else null,
            if (selectedRole == "student") selectedParentId else null,
            selectedPhotoUri
        )
    }

    private fun showOtpDialog(msg: String) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_otp, null)

        val etOtp = dialogView.findViewById<EditText>(R.id.et_otp_code)
        val btnVerify = dialogView.findViewById<View>(R.id.btn_verify_otp)
        val btnCancel = dialogView.findViewById<View>(R.id.btn_cancel_otp)
        val tvResend = dialogView.findViewById<TextView>(R.id.tv_resend_otp)
        val tvDesc = dialogView.findViewById<TextView>(R.id.tv_otp_email_desc)

        tvDesc.text = msg

        otpDialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        otpDialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)

        btnVerify.setOnClickListener {
            val otp = etOtp.text.toString().trim()
            if (otp.length == 6) {
                viewModel.verifyOtpAndRegister(otp)
            } else {
                etOtp.error = "Masukkan 6 digit kode OTP"
            }
        }

        btnCancel.setOnClickListener {
            otpDialog?.dismiss()
        }

        tvResend.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            if (email.isNotEmpty()) {
                Toast.makeText(this, "Mengirim ulang kode OTP...", Toast.LENGTH_SHORT).show()
                validateAndSubmit()
            }
        }

        otpDialog?.show()
    }
}