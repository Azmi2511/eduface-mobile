package id.my.eduface.data.model

import com.google.gson.annotations.SerializedName

data class User(
    val id: Int,
    val username: String?,
    val email: String?,
    @SerializedName("full_name") val fullName: String,
    val phone: String?,
    val dob: String?,
    val gender: String?,
    @SerializedName("profile_picture") val profilePicture: String?,
    val role: String,
    @SerializedName("is_active") val isActive: Boolean,
    val student: Student?,
    val teacher: Teacher?,
    @SerializedName("parent_profile") val parent: Parent
)