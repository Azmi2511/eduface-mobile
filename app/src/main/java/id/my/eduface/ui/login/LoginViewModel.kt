package id.my.eduface.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.my.eduface.data.model.LoginResponse
import id.my.eduface.data.repository.AuthRepository
import id.my.eduface.utils.Resource
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class LoginViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _loginResult = MutableLiveData<Resource<LoginResponse>>()
    val loginResult: LiveData<Resource<LoginResponse>> = _loginResult

    fun login(loginInput: String, pass: String) {
        _loginResult.value = Resource.Loading()

        viewModelScope.launch {
            try {
                val request = mapOf(
                    "login" to loginInput,
                    "password" to pass
                )

                val response = repository.login(request)

                if (response.isSuccessful) {
                    response.body()?.let { result ->
                        _loginResult.value = Resource.Success(result)
                    } ?: run {
                        _loginResult.value = Resource.Error("Response kosong")
                    }
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Gagal Login"
                    _loginResult.value = Resource.Error(errorMsg)
                }
            } catch (e: Exception) {
                val message = when (e) {
                    is IOException -> "Koneksi internet bermasalah"
                    is HttpException -> "Kesalahan server"
                    else -> e.message ?: "Terjadi kesalahan"
                }
                _loginResult.value = Resource.Error(message)
            }
        }
    }
}