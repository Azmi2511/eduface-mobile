package id.my.eduface.data.network

import android.content.Context
import id.my.eduface.utils.SessionManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class ApiClient {

    companion object {
        // Domain utama dari Ngrok
        const val SERVER_URL = "https://dialectologically-archeological-mayson.ngrok-free.dev"
        private const val API_URL = "$SERVER_URL/api/v1/"
        const val STORAGE_URL = "$SERVER_URL/storage/"

        /**
         * Instance dasar tanpa Auth Interceptor
         * Biasanya digunakan untuk Login atau Register
         */
        val instance: ApiService by lazy {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val client = OkHttpClient.Builder()
                .addInterceptor(logging)
                .addInterceptor { chain ->
                    val request = chain.request().newBuilder()
                        .addHeader("Accept", "application/json")
                        .addHeader("ngrok-skip-browser-warning", "true") // Bypass Ngrok
                        .build()
                    chain.proceed(request)
                }
                .build()

            Retrofit.Builder()
                .baseUrl(API_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiService::class.java)
        }

        /**
         * Instance utama dengan Auth Interceptor
         * Digunakan untuk request yang membutuhkan Token (Dashboard, Schedule, dll)
         */
        fun getApiService(context: Context): ApiService {
            val sessionManager = SessionManager(context)

            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val client = OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .addInterceptor { chain ->
                    val requestBuilder = chain.request().newBuilder()

                    // Tambahkan Token jika tersedia di Session
                    sessionManager.fetchAuthToken()?.let { token ->
                        requestBuilder.addHeader("Authorization", "Bearer $token")
                    }

                    // Header Wajib
                    requestBuilder.addHeader("Accept", "application/json")

                    // Header khusus Ngrok agar tidak mengembalikan halaman HTML warning
                    requestBuilder.addHeader("ngrok-skip-browser-warning", "true")

                    chain.proceed(requestBuilder.build())
                }
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build()

            return Retrofit.Builder()
                .baseUrl(API_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiService::class.java)
        }
    }
}