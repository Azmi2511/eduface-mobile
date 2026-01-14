package id.my.eduface.ui.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import id.my.eduface.data.model.User
import id.my.eduface.data.network.SingleResponse
import id.my.eduface.data.repository.AuthRepository
import id.my.eduface.utils.Resource
import kotlinx.coroutines.launch

class ProfileViewModel(
    application: Application,
    private val repository: AuthRepository
) : AndroidViewModel(application) {

    private val _profileResult = MutableLiveData<Resource<SingleResponse<User>>>()
    val profileResult: LiveData<Resource<SingleResponse<User>>> = _profileResult
    private val _updateResult = MutableLiveData<Resource<SingleResponse<User>>>()
    val updateResult: LiveData<Resource<SingleResponse<User>>> = _updateResult
    private val _passwordResult = MutableLiveData<Resource<SingleResponse<Any>>>()
    val passwordResult: LiveData<Resource<SingleResponse<Any>>> = _passwordResult

    fun fetchProfile() {
        _profileResult.value = Resource.Loading()
        viewModelScope.launch {
            try {
                val response = repository.getMyProfile()
                _profileResult.value = response
            } catch (e: Exception) {
                _profileResult.value = Resource.Error(e.message ?: "Terjadi kesalahan")
            }
        }
    }

    fun updateUser(name: String, email: String, phone: String) {
        _updateResult.value = Resource.Loading()
        viewModelScope.launch {
            try {
                val response = repository.updateProfile(name, email, phone)
                _updateResult.value = response
            } catch (e: Exception) {
                _updateResult.value = Resource.Error(e.message ?: "Gagal update profil")
            }
        }
    }

    fun updatePassword(current: String, new: String, confirm: String) {
        _passwordResult.value = Resource.Loading()
        viewModelScope.launch {
            try {
                val response = repository.updatePassword(current, new, confirm)
                _passwordResult.value = response
            } catch (e: Exception) {
                _passwordResult.value = Resource.Error(e.message ?: "Gagal ubah sandi")
            }
        }
    }
}