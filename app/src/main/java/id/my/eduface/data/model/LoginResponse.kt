package id.my.eduface.data.model

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    val status: Boolean,
    val message: String,
    @SerializedName("access_token")
    val accessToken: String?,
    @SerializedName("token_type")
    val tokenType: String?,
    val user: User?
)