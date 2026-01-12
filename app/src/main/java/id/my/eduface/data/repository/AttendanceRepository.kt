package id.my.eduface.data.repository

import android.content.Context
import id.my.eduface.data.network.ApiService

class AttendanceRepository(
    private val apiService: ApiService,
    private val context: Context
) {
    suspend fun getStudents(token: String) =
        apiService.getStudents("Bearer $token")

    suspend fun getAttendanceLogs(token: String, nisn: String, date: String) =
        apiService.getAttendance("Bearer $token", nisn, date)
}