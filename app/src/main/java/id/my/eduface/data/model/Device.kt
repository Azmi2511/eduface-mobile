package id.my.eduface.data.model

import com.google.gson.annotations.SerializedName

data class Device(
    val id: Int,
    @SerializedName("device_name") val deviceName: String,
    val location: String,
    @SerializedName("api_token") val apiToken: String
)