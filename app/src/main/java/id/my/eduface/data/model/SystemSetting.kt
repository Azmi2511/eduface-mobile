package id.my.eduface.data.model

import com.google.gson.annotations.SerializedName

data class SystemSetting(
    val id: Int,
    @SerializedName("school_name") val schoolName: String?,
    val address: String?,
    val email: String?,
    val phone: String?,
    @SerializedName("entry_time") val entryTime: String?,
    @SerializedName("late_limit") val lateLimit: String?,
    @SerializedName("face_rec_enabled") val faceRecEnabled: Int
)