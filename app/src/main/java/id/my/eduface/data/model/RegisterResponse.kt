package id.my.eduface.data.model

import com.google.gson.annotations.SerializedName
data class RegisterDataResponse(
    @SerializedName("status") val status: String,
    @SerializedName("data") val data: RegisterDataContent
)

data class RegisterDataContent(
    @SerializedName("classes") val classes: List<SchoolClass>,
    @SerializedName("parents") val parents: List<ParentOption>
)

data class ParentOption(
    @SerializedName("id") val id: Int,
    @SerializedName("full_name") val fullName: String,
    @SerializedName("email") val email: String
) {
    override fun toString(): String = "$fullName ($email)"
}
data class RegisterResponse(
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String,
    @SerializedName("username") val username: String?,
    @SerializedName("user") val user: User?
)
data class OtpResponse(
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String
)