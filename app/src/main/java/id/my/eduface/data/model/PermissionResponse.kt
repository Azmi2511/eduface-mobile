package id.my.eduface.data.model

import com.google.gson.annotations.SerializedName

data class PermissionResponse(
    @SerializedName("data") val data: List<PermissionItem>
)

data class PermissionItem(
    val id: Int,
    val student: StudentPermission,
    val type: String?,
    @SerializedName("date_range") val dateRange: DateRange,
    val description: String?,
    @SerializedName("proof_url") val proofUrl: String?,
    @SerializedName("approval_status") val approvalStatus: String?,
    @SerializedName("created_at") val createdAt: String?
)

data class StudentPermission(
    val id: Int,
    @SerializedName("full_name") val fullName: String?
)

data class DateRange(
    val start: String?,
    val end: String?
)