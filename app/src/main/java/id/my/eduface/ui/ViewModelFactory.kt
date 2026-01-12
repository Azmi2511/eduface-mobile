package id.my.eduface.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import id.my.eduface.data.repository.AuthRepository
import id.my.eduface.ui.login.LoginViewModel
import id.my.eduface.ui.register.RegisterViewModel

@Suppress("UNCHECKED_CAST")
class ViewModelFactory(private val repository: AuthRepository) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            return LoginViewModel(repository) as T
        } else if (modelClass.isAssignableFrom(RegisterViewModel::class.java)) {
            return RegisterViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: " + modelClass.name)
    }
}