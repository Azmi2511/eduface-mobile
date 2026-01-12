package id.my.eduface.ui.permission

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.ArrayAdapter
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import id.my.eduface.R
import id.my.eduface.data.model.PermissionItem
import id.my.eduface.data.network.ApiClient
import id.my.eduface.databinding.FragmentAddPermissionBinding
import id.my.eduface.utils.SessionManager
import id.my.eduface.utils.ToastUtils
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.util.*

class AddPermissionFragment : Fragment(R.layout.fragment_add_permission) {

    private var _binding: FragmentAddPermissionBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager
    private var selectedImageUri: Uri? = null

    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            selectedImageUri = result.data?.data
            binding.ivPreview.setImageURI(selectedImageUri)
            binding.ivPreview.visibility = View.VISIBLE
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAddPermissionBinding.bind(view)
        sessionManager = SessionManager(requireContext())

        setupSpinners()
        setupDatePickers()
        setupBackNavigation()

        binding.btnSelectImage.setOnClickListener { openGallery() }
        binding.btnSubmit.setOnClickListener { validateAndSubmit() }
    }

    private fun setupBackNavigation() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (isFormDirty()) {
                    showExitConfirmationDialog(this) // Kirim callback ini sebagai parameter
                } else {
                    remove() // Hapus callback ini dari dispatcher
                    requireActivity().onBackPressedDispatcher.onBackPressed() // Panggil back default
                }
            }
        })
    }

    private fun showExitConfirmationDialog(callback: OnBackPressedCallback) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Batal Mengajukan?")
            .setMessage("Data yang Anda masukkan belum disimpan. Apakah Anda yakin ingin keluar?")
            .setPositiveButton("Ya, Keluar") { _, _ ->
                callback.remove() // Hapus pengaman agar bisa keluar
                parentFragmentManager.popBackStack() // Langsung tutup fragment
            }
            .setNegativeButton("Lanjutkan Mengisi", null)
            .show()
    }

    private fun isFormDirty(): Boolean {
        val start = binding.etStartDate.text.toString()
        val end = binding.etEndDate.text.toString()
        val desc = binding.etDescription.text.toString()
        return start.isNotEmpty() || end.isNotEmpty() || desc.isNotEmpty() || selectedImageUri != null
    }

    private fun setupSpinners() {
        val types = listOf("Izin", "Sakit")
        val typeAdapter = ArrayAdapter(requireContext(), R.layout.item_spinner_poppins, types)
        binding.spinnerType.adapter = typeAdapter
    }

    private fun setupDatePickers() {
        binding.etStartDate.setOnClickListener { showDatePicker { date -> binding.etStartDate.setText(date) } }
        binding.etEndDate.setOnClickListener { showDatePicker { date -> binding.etEndDate.setText(date) } }
    }

    private fun showDatePicker(onDateSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(requireContext(), { _, year, month, day ->
            val date = String.format("%04d-%02d-%02d", year, month + 1, day)
            onDateSelected(date)
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        imagePickerLauncher.launch(intent)
    }

    private fun validateAndSubmit() {
        val type = binding.spinnerType.selectedItem.toString()
        val start = binding.etStartDate.text.toString()
        val end = binding.etEndDate.text.toString()
        val desc = binding.etDescription.text.toString()
        val studentId = sessionManager.fetchSelectedChildId()

        if (start.isEmpty() || end.isEmpty() || desc.isEmpty()) {
            ToastUtils.showError(requireContext(), "Harap isi semua bidang")
            return
        }

        submitData(studentId.toString(), type, start, end, desc)
    }

    private fun submitData(studentId: String, type: String, start: String, end: String, desc: String) {
        binding.btnSubmit.isEnabled = false
        binding.progressBar.visibility = View.VISIBLE

        val studentIdPart = studentId.toRequestBody("text/plain".toMediaTypeOrNull())
        val typePart = type.toRequestBody("text/plain".toMediaTypeOrNull())
        val startPart = start.toRequestBody("text/plain".toMediaTypeOrNull())
        val endPart = end.toRequestBody("text/plain".toMediaTypeOrNull())
        val descPart = desc.toRequestBody("text/plain".toMediaTypeOrNull())

        var imagePart: MultipartBody.Part? = null
        selectedImageUri?.let { uri ->
            val filePath = getRealPathFromURI(uri)
            if (filePath.isNotEmpty()) {
                val file = File(filePath)
                val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                imagePart = MultipartBody.Part.createFormData("proof_file", file.name, requestFile)
            }
        }

        ApiClient.getApiService(requireContext())
            .storePermission(studentIdPart, typePart, startPart, endPart, descPart, imagePart)
            .enqueue(object : Callback<PermissionItem> {
                override fun onResponse(call: Call<PermissionItem>, response: Response<PermissionItem>) {
                    binding.btnSubmit.isEnabled = true
                    binding.progressBar.visibility = View.GONE
                    if (response.isSuccessful) {
                        ToastUtils.showSuccess(requireContext(), "Izin berhasil dikirim")
                        parentFragmentManager.popBackStack()
                    } else {
                        android.util.Log.e("EdufaceDebug", "Error: ${response.code()} ${response.errorBody()?.string()}")
                    }
                }

                override fun onFailure(call: Call<PermissionItem>, t: Throwable) {
                    binding.btnSubmit.isEnabled = true
                    binding.progressBar.visibility = View.GONE
                    ToastUtils.showError(requireContext(), "Terjadi kesalahan jaringan")
                }
            })
    }

    private fun getRealPathFromURI(contentUri: Uri): String {
        val cursor = requireContext().contentResolver.query(contentUri, null, null, null, null)
        cursor?.moveToFirst()
        val idx = cursor?.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
        val path = if (idx != null && idx != -1) cursor.getString(idx) else ""
        cursor?.close()
        return path ?: ""
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}