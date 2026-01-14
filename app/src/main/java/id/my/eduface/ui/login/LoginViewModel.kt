package id.my.eduface.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.my.eduface.data.model.LoginResponse
import id.my.eduface.data.repository.AuthRepository
import id.my.eduface.utils.Resource
import kotlinx.coroutines.launch

class LoginViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _loginResult = MutableLiveData<Resource<LoginResponse>>()
    val loginResult: LiveData<Resource<LoginResponse>> = _loginResult

    fun login(loginInput: String, pass: String) {
        _loginResult.value = Resource.Loading()

        viewModelScope.launch {
            val request = mapOf(
                "login" to loginInput,
                "password" to pass
            )
            _loginResult.value = repository.login(request)
        }
    }
}