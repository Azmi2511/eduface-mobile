package id.my.eduface.ui.register

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.my.eduface.data.model.OtpResponse
import id.my.eduface.data.model.RegisterDataContent
import id.my.eduface.data.model.RegisterResponse
import id.my.eduface.data.repository.AuthRepository
import id.my.eduface.utils.Resource
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream

class RegisterViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _registerData = MutableLiveData<Resource<RegisterDataContent>>()
    val registerData: LiveData<Resource<RegisterDataContent>> = _registerData

    private val _otpResult = MutableLiveData<Resource<OtpResponse>>()
    val otpResult: LiveData<Resource<OtpResponse>> = _otpResult

    private val _finalRegisterResult = MutableLiveData<Resource<RegisterResponse>>()
    val finalRegisterResult: LiveData<Resource<RegisterResponse>> = _finalRegisterResult

    private var tempParams: HashMap<String, RequestBody>? = null
    private var tempPhotoUri: Uri? = null

    fun fetchRegisterData() {
        _registerData.value = Resource.Loading()
        viewModelScope.launch {
            val response = repository.getRegisterData()
            // Mapping response agar sesuai Resource wrapper Anda
            if (response is Resource.Success) {
                _registerData.value = Resource.Success(response.data!!.data)
            } else if (response is Resource.Error) {
                _registerData.value = Resource.Error(response.message ?: "Gagal memuat data")
            }
        }
    }

    fun processRegister(
        context: Context,
        name: String, email: String, password: String, confirm: String,
        role: String, gender: String, phone: String, idNumber: String,
        classId: Int?, parentId: Int?, photoUri: Uri?
    ) {
        if (password != confirm) {
            _otpResult.value = Resource.Error("Konfirmasi password tidak sesuai")
            return
        }

        _otpResult.value = Resource.Loading()

        viewModelScope.launch {
            try {
                val params = HashMap<String, RequestBody>()
                params["name"] = createPart(name)
                params["email"] = createPart(email)
                params["password"] = createPart(password)
                params["password_confirmation"] = createPart(confirm)
                params["role"] = createPart(role)
                params["gender"] = createPart(gender)
                params["phone"] = createPart(phone)

                if (role == "student") {
                    params["id_number"] = createPart(idNumber)
                    classId?.let { params["class_id"] = createPart(it.toString()) }
                    parentId?.let { params["parent_id"] = createPart(it.toString()) }
                } else if (role == "teacher") {
                    params["id_number"] = createPart(idNumber)
                }

                tempParams = params
                tempPhotoUri = photoUri

                val photoPart = prepareFilePart(context, photoUri)

                val response = repository.sendOtp(params, photoPart)
                _otpResult.value = response

            } catch (e: Exception) {
                _otpResult.value = Resource.Error(e.message ?: "Terjadi kesalahan")
            }
        }
    }

    fun verifyOtpAndRegister(context: Context, otpCode: String) {
        if (tempParams == null) {
            _finalRegisterResult.value = Resource.Error("Data registrasi hilang, silakan ulangi.")
            return
        }

        _finalRegisterResult.value = Resource.Loading()

        viewModelScope.launch {
            try {
                tempParams!!["otp_code"] = createPart(otpCode)

                val photoPart = prepareFilePart(context, tempPhotoUri)

                val response = repository.verifyAndRegister(tempParams!!, photoPart)
                _finalRegisterResult.value = response

            } catch (e: Exception) {
                _finalRegisterResult.value = Resource.Error(e.message ?: "Gagal Verifikasi")
            }
        }
    }

    // --- Helpers ---

    private fun createPart(value: String): RequestBody {
        return value.toRequestBody("text/plain".toMediaTypeOrNull())
    }

    private fun prepareFilePart(context: Context, uri: Uri?): MultipartBody.Part? {
        if (uri == null) return null
        val file = uriToFile(context, uri)
        val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
        return MultipartBody.Part.createFormData("photo", file.name, requestFile)
    }

    private fun uriToFile(context: Context, uri: Uri): File {
        val inputStream = context.contentResolver.openInputStream(uri)
        val file = File(context.cacheDir, "temp_${System.currentTimeMillis()}.jpg")
        val outputStream = FileOutputStream(file)
        inputStream?.copyTo(outputStream)
        inputStream?.close()
        outputStream.close()
        return file
    }
}