package id.my.eduface.ui.register

import android.app.Application
import android.net.Uri
import androidx.lifecycle.*
import id.my.eduface.data.model.*
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

class RegisterViewModel(application: Application, private val repository: AuthRepository) : AndroidViewModel(application) {

    private val context = application.applicationContext

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
            if (response is Resource.Success) {
                val content = response.data?.data
                if (content != null) {
                    _registerData.value = Resource.Success(content)
                } else {
                    _registerData.value = Resource.Error("Data kosong")
                }
            } else if (response is Resource.Error) {
                _registerData.value = Resource.Error(response.message ?: "Gagal memuat data")
            }
        }
    }

    fun processRegister(
        name: String, email: String, password: String, confirm: String,
        role: String, gender: String, phone: String, idNumber: String,
        classId: Int?, parentId: Int?, photoUri: Uri?
    ) {
        if (password != confirm) {
            _otpResult.value = Resource.Error("Password tidak cocok")
            return
        }

        _otpResult.value = Resource.Loading()
        viewModelScope.launch {
            try {
                val params = HashMap<String, RequestBody>()
                params["name"] = name.toPart()
                params["email"] = email.toPart()
                params["password"] = password.toPart()
                params["password_confirmation"] = confirm.toPart()
                params["role"] = role.toPart()
                params["gender"] = gender.toPart()
                params["phone"] = phone.toPart()

                if (role == "student") {
                    params["id_number"] = idNumber.toPart()
                    classId?.let { params["class_id"] = it.toString().toPart() }
                    parentId?.let { params["parent_id"] = it.toString().toPart() }
                } else if (role == "teacher") {
                    params["id_number"] = idNumber.toPart()
                }

                tempParams = params
                tempPhotoUri = photoUri

                val photoPart = prepareFilePart(photoUri)
                _otpResult.value = repository.sendOtp(params, photoPart)
            } catch (e: Exception) {
                _otpResult.value = Resource.Error(e.message ?: "Error")
            }
        }
    }

    fun verifyOtpAndRegister(otpCode: String) {
        val params = tempParams ?: return
        _finalRegisterResult.value = Resource.Loading()
        viewModelScope.launch {
            try {
                params["otp_code"] = otpCode.toPart()
                val photoPart = prepareFilePart(tempPhotoUri)
                _finalRegisterResult.value = repository.verifyAndRegister(params, photoPart)
            } catch (e: Exception) {
                _finalRegisterResult.value = Resource.Error(e.message ?: "Gagal")
            }
        }
    }

    fun resetOtpResult() {
        _otpResult.value = null
    }

    private fun String.toPart() = this.toRequestBody("text/plain".toMediaTypeOrNull())

    private fun prepareFilePart(uri: Uri?): MultipartBody.Part? {
        if (uri == null) return null
        val file = File(context.cacheDir, "upload_${System.currentTimeMillis()}.jpg")
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(file).use { output -> input.copyTo(output) }
        }
        return MultipartBody.Part.createFormData("photo", file.name, file.asRequestBody("image/*".toMediaTypeOrNull()))
    }
}