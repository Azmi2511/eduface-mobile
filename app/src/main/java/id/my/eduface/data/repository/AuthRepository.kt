package id.my.eduface.data.repository

import id.my.eduface.data.model.FaceRegisterResponse
import id.my.eduface.data.model.LoginResponse
import id.my.eduface.data.model.User
import id.my.eduface.data.network.ApiService
import id.my.eduface.data.network.SingleResponse
import id.my.eduface.utils.Resource
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response
import java.io.File

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
    suspend fun getMyProfile(): Resource<SingleResponse<User>> = safeApiCall {
        apiService.getMyProfile()
    }
    suspend fun updateProfile(name: String, email: String, phone: String?) = safeApiCall {
        apiService.updateProfile(name, email, phone)
    }
    suspend fun updatePassword(current: String, new: String, confirm: String) = safeApiCall {
        apiService.updatePassword(current, new, confirm)
    }
    suspend fun registerFace(pose: String, file: File): Response<FaceRegisterResponse> {

        val poseBody = pose.toRequestBody("text/plain".toMediaTypeOrNull())

        val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())

        val filePart = MultipartBody.Part.createFormData("file", file.name, requestFile)

        return apiService.registerFace(poseBody, filePart)
    }
}