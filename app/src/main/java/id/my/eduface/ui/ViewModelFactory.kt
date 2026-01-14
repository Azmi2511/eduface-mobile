package id.my.eduface.ui

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import id.my.eduface.data.repository.AuthRepository
import id.my.eduface.ui.login.LoginViewModel
import id.my.eduface.ui.register.RegisterViewModel

class ViewModelFactory(private val app: Application, private val repo: AuthRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(LoginViewModel::class.java) -> LoginViewModel(repo) as T
            modelClass.isAssignableFrom(RegisterViewModel::class.java) -> RegisterViewModel(app, repo) as T
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}