package id.my.eduface.data.repository

import id.my.eduface.data.model.LoginResponse
import id.my.eduface.data.network.ApiService
import id.my.eduface.utils.Resource
import okhttp3.MultipartBody
import okhttp3.RequestBody

class AuthRepository(private val apiService: ApiService) : BaseRepository() {

    suspend fun login(request: Map<String, String>): Resource<LoginResponse> = safeApiCall {
        apiService.login(request)
    }

    suspend fun logout() = apiService.logout()

    suspend fun getRegisterData() = safeApiCall {
        apiService.getRegisterData()
    }

    suspend fun sendOtp(data: Map<String, RequestBody>, photo: MultipartBody.Part?) = safeApiCall {
        apiService.sendOtp(data, photo)
    }

    suspend fun verifyAndRegister(data: Map<String, RequestBody>, photo: MultipartBody.Part?) = safeApiCall {
        apiService.verifyAndRegister(data, photo)
    }
}