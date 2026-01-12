package id.my.eduface.data.model

import com.google.gson.annotations.SerializedName

data class DefaultResponse(
    @SerializedName("message")
    val message: String? = null,

    @SerializedName("status")
    val status: String? = null,

    @SerializedName("data")
    val data: Any? = null
)