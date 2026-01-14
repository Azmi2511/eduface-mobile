package id.my.eduface.data.network

import id.my.eduface.data.model.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*
import retrofit2.Response
import retrofit2.Call

interface ApiService {

    @POST("login")
    suspend fun login(@Body request: Map<String, String>): Response<LoginResponse>

    @POST("logout")
    suspend fun logout(): Response<Map<String, String>>

    @GET("register-data")
    suspend fun getRegisterData(): Response<RegisterDataResponse>

    @Multipart
    @POST("face/register")
    suspend fun registerFace(
        @Part("pose") pose: RequestBody,
        @Part file: MultipartBody.Part
    ): Response<FaceRegisterResponse>

    @Multipart
    @POST("send-otp")
    suspend fun sendOtp(
        @PartMap params: Map<String, @JvmSuppressWildcards RequestBody>,
        @Part photo: MultipartBody.Part?
    ): Response<OtpResponse>

    @Multipart
    @POST("verify-register")
    suspend fun verifyAndRegister(
        @PartMap data: Map<String, @JvmSuppressWildcards RequestBody>,
        @Part photo: MultipartBody.Part?
    ): Response<RegisterResponse>

    @GET("dashboard")
    fun getDashboard(): Call<DashboardResponse>

    @GET("me")
    suspend fun getMyProfile(): Response<SingleResponse<User>>

    @FormUrlEncoded
    @POST("me/update")
    suspend fun updateProfile(
        @Field("full_name") fullName: String,
        @Field("email") email: String,
        @Field("phone") phone: String?,
        @Field("_method") method: String = "PUT"
    ): Response<SingleResponse<User>>

    @FormUrlEncoded
    @POST("me/password")
    suspend fun updatePassword(
        @Field("current_password") currentPass: String,
        @Field("password") newPass: String,
        @Field("password_confirmation") confirmPass: String,
        @Field("_method") method: String = "PUT"
    ): Response<SingleResponse<Any>>

    @GET("my-schedules/today")
    suspend fun getTodaySchedule(): Response<ListResponse<Schedule>>

    @GET("schedules")
    fun getSchedules(
        @Header("Authorization") token: String,
        @Query("class_id") classId: Int? = null
    ): Call<ScheduleBaseResponse>

    @GET("subjects")
    suspend fun getSubjects(): Response<ListResponse<Subject>>

    @GET("announcements")
    suspend fun getAnnouncements(): Response<ListResponse<Announcement>>

    @GET("notifications")
    fun getNotifications(
        @Query("unread_only") unreadOnly: String? = null
    ): Call<NotificationResponse>
    @GET("notifications/unread-count")
    fun getUnreadCount(): Call<UnreadCountResponse>
    @PATCH("notifications/{id}/read")
    fun markAsRead(@Path("id") id: Int): Call<NotificationResponse>
    @POST("notifications/mark-all-read")
    fun markAllRead(): Call<NotificationResponse>

    @GET("attendance")
    fun getAttendance(
        @Header("Authorization") token: String,
        @Query("nisn") nisn: String,
        @Query("date") date: String?
    ): Call<AttendanceWrapper>

    @GET("students")
    suspend fun getStudents(
        @Header("Authorization") token: String
    ): Response<StudentResponse>

    @GET("permissions")
    fun getPermissions(
        @Query("student_id") studentId: Int?
    ): Call<PermissionResponse>

    @Multipart
    @POST("permissions")
    fun storePermission(
        @Part("student_id") studentId: RequestBody,
        @Part("type") type: RequestBody,
        @Part("start_date") startDate: RequestBody,
        @Part("end_date") endDate: RequestBody,
        @Part("description") description: RequestBody,
        @Part proof_file: MultipartBody.Part?
    ): Call<PermissionItem>

    @FormUrlEncoded
    @POST("user/update-fcm")
    fun updateFcmToken(
        @Field("fcm_token") token: String
    ): Call<DefaultResponse>
}