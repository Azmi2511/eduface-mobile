package id.my.eduface.ui.register

import android.app.AlertDialog
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import id.my.eduface.R // Sesuaikan R import
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

    // Variables
    private var selectedRole = "student"
    private var selectedGender = "L"
    private var selectedClassId: Int? = null
    private var selectedParentId: Int? = null
    private var selectedPhotoUri: Uri? = null

    // Image Picker
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

        // Ambil data dropdown saat activity mulai
        viewModel.fetchRegisterData()
    }

    private fun setupViewModel() {
        val factory = ViewModelFactory(AuthRepository(ApiClient.getApiService(this)))
        viewModel = ViewModelProvider(this, factory)[RegisterViewModel::class.java]
    }

    private fun setupUI() {
        // 1. Setup Role Spinner
        val roles = arrayOf("Siswa", "Guru", "Wali Murid")
        val roleAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, roles)
        binding.spinnerRole.adapter = roleAdapter
        binding.spinnerRole.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                when (position) {
                    0 -> { // Siswa
                        selectedRole = "student"
                        binding.layoutStudentExtras.visibility = View.VISIBLE
                        binding.layoutIdNumber.visibility = View.VISIBLE
                        binding.etIdNumber.hint = "NISN"
                    }
                    1 -> { // Guru
                        selectedRole = "teacher"
                        binding.layoutStudentExtras.visibility = View.GONE
                        binding.layoutIdNumber.visibility = View.VISIBLE
                        binding.etIdNumber.hint = "NIP"
                    }
                    2 -> { // Wali
                        selectedRole = "parent"
                        binding.layoutStudentExtras.visibility = View.GONE
                        binding.layoutIdNumber.visibility = View.GONE // Wali tidak butuh ID Number di backend
                    }
                }
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }

        // 2. Setup Gender Spinner
        val genders = arrayOf("Laki-laki", "Perempuan")
        val genderAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, genders)
        binding.spinnerGender.adapter = genderAdapter
        binding.spinnerGender.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                selectedGender = if (position == 0) "L" else "P"
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }

        // 3. Button Actions
        binding.btnUploadPhoto.setOnClickListener { pickImage.launch("image/*") }
        binding.btnRegister.setOnClickListener { validateAndSubmit() }
        binding.tvLoginLink.setOnClickListener { finish() }
    }

    private fun observeData() {
        // Observer: Data Dropdown (Kelas & Wali)
        viewModel.registerData.observe(this) { result ->
            when (result) {
                is Resource.Loading -> { /* Show loading skeleton if needed */ }
                is Resource.Success -> {
                    setupDynamicSpinners(result.data!!.classes, result.data.parents)
                }
                is Resource.Error -> {
                    Toast.makeText(this, "Gagal memuat data: ${result.message}", Toast.LENGTH_LONG).show()
                }
            }
        }

        // Observer: Hasil Request OTP
        viewModel.otpResult.observe(this) { result ->
            when (result) {
                is Resource.Loading -> {
                    binding.btnRegister.isEnabled = false
                    binding.btnRegister.text = "Mengirim Data..."
                }
                is Resource.Success -> {
                    binding.btnRegister.isEnabled = true
                    binding.btnRegister.text = "Daftar"
                    // Tampilkan Dialog OTP
                    showOtpDialog(result.data?.message ?: "OTP Terkirim")
                }
                is Resource.Error -> {
                    binding.btnRegister.isEnabled = true
                    binding.btnRegister.text = "Daftar"
                    Toast.makeText(this, result.message, Toast.LENGTH_LONG).show()
                }
            }
        }

        // Observer: Hasil Register Final
        viewModel.finalRegisterResult.observe(this) { result ->
            when (result) {
                is Resource.Loading -> { /* Loading di Dialog OTP handled manual/dismiss */ }
                is Resource.Success -> {
                    Toast.makeText(this, "Registrasi Berhasil! Silakan Login.", Toast.LENGTH_LONG).show()
                    finish() // Kembali ke Login
                }
                is Resource.Error -> {
                    Toast.makeText(this, result.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun setupDynamicSpinners(classes: List<SchoolClass>, parents: List<ParentOption>) {
        // Spinner Kelas
        val classAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, classes)
        binding.spinnerClass.adapter = classAdapter
        binding.spinnerClass.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, pos: Int, p3: Long) {
                selectedClassId = classes[pos].id
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }

        // Spinner Wali
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
        val idNumber = binding.etIdNumber.text.toString().trim() // NISN atau NIP

        // Validasi Dasar
        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Harap isi semua field wajib", Toast.LENGTH_SHORT).show()
            return
        }

        viewModel.processRegister(
            this, name, email, password, confirm,
            selectedRole, selectedGender, phone, idNumber,
            if (selectedRole == "student") selectedClassId else null,
            if (selectedRole == "student") selectedParentId else null,
            selectedPhotoUri
        )
    }

    private fun showOtpDialog(msg: String) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_otp, null)
        val etOtp = dialogView.findViewById<EditText>(R.id.et_otp_code)

        val dialog = AlertDialog.Builder(this)
            .setTitle("Verifikasi Email")
            .setMessage("$msg\nSilakan masukkan kode OTP yang dikirim ke email Anda.")
            .setView(dialogView)
            .setPositiveButton("Verifikasi", null)
            .setNegativeButton("Batal") { d, _ -> d.dismiss() }
            .create()

        dialog.show()

        // Override onClick agar dialog tidak tertutup jika OTP kosong
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val otp = etOtp.text.toString().trim()
            if (otp.length == 6) {
                viewModel.verifyOtpAndRegister(this, otp)
                dialog.dismiss() // Tutup dialog, biarkan Loading muncul (bisa tambah loading dialog terpisah)
            } else {
                etOtp.error = "Masukkan 6 digit kode OTP"
            }
        }
    }
}