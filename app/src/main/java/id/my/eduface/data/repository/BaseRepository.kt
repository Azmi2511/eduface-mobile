package id.my.eduface.data.repository

import id.my.eduface.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

abstract class BaseRepository {

    suspend fun <T> safeApiCall(apiCall: suspend () -> Response<T>): Resource<T> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiCall()
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        return@withContext Resource.Success(body)
                    }
                }
                return@withContext error("Error Code: ${response.code()} ${response.message()}")
            } catch (e: Exception) {
                return@withContext error(e.message ?: "Terjadi kesalahan tidak diketahui")
            }
        }
    }

    private fun <T> error(message: String): Resource<T> {
        return Resource.Error("Koneksi gagal: $message")
    }
}