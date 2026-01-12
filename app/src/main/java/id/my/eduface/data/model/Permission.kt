package id.my.eduface.data.model

import com.google.gson.annotations.SerializedName

data class Permission(
    val id: Int,
    @SerializedName("student_id") val studentId: Int,
    val type: String,
    @SerializedName("start_date") val startDate: String,
    @SerializedName("end_date") val endDate: String,
    val description: String?,
    @SerializedName("proof_file_path") val proofFilePath: String?,
    @SerializedName("approval_status") val approvalStatus: String,
    val student: Student?
)