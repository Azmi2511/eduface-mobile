package id.my.eduface.data.model

import com.google.gson.annotations.SerializedName
data class DashboardResponse(
    @SerializedName("parent_name") val parentName: String?,
    @SerializedName("children") val children: List<Child>,
    @SerializedName("statistics") val statistics: Statistics,
    @SerializedName("attendance_logs") val attendanceLogs: List<AttendanceLog>?
)

data class Child(
    @SerializedName("id") val id: Int,
    @SerializedName("nisn") val nisn: String,
    @SerializedName("name") val name: String,
    @SerializedName("statistics") val statistics: Statistics,
    @SerializedName("class_id") val classId: Int,
)

data class Statistics(
    @SerializedName("attendancePercentage") val attendancePercentage: String,
    @SerializedName("presentCount") val presentCount: Int,
    @SerializedName("lateCount") val lateCount: Int,
    @SerializedName("absentCount") val absentCount: Int,
    @SerializedName("permissionCount") val permissionCount: Int
)