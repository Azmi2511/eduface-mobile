package id.my.eduface.utils

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import id.my.eduface.data.model.User

class SessionManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("eduface_session", Context.MODE_PRIVATE)
    private val editor: SharedPreferences.Editor = prefs.edit()
    private val gson = Gson()

    companion object {
        const val KEY_IS_LOGIN = "is_login"
        const val KEY_USER_DATA = "user_data"
        const val KEY_NAME = "user_name"
        const val KEY_ROLE = "user_role"
        const val KEY_EMAIL = "user_email"
        const val KEY_AUTH_TOKEN = "auth_token"
        const val KEY_PHOTO_URL = "user_photo"
        const val KEY_SELECTED_CLASS_ID = "selected_class_id"
        const val STUDENT_NISN = "student_nisn"
        const val SELECTED_CHILD_ID = "selected_child_id"
        const val CHILD_COUNT = "child_count"
        const val KEY_ATTENDANCE_RATE = "attendance_rate"
    }

    fun saveAuthToken(token: String) {
        editor.putString(KEY_AUTH_TOKEN, token)
        editor.apply()
    }

    fun fetchAuthToken(): String? {
        return prefs.getString(KEY_AUTH_TOKEN, null)
    }

    fun saveSelectedClassId(classId: Int) {
        editor.putInt(KEY_SELECTED_CLASS_ID, classId)
        editor.apply()
    }

    fun saveSelectedStudentNisn(nisn: String) {
        val editor = prefs.edit()
        editor.putString(STUDENT_NISN, nisn)
        editor.apply()
    }

    fun saveSelectedChildId(id: Int) {
        val editor = prefs.edit()
        editor.putInt(SELECTED_CHILD_ID, id)
        editor.apply()
    }
    fun saveChildCount(count: Int) {
        editor.putInt(CHILD_COUNT, count)
        editor.apply()
    }
    fun saveAttendanceRate(rate: String) {
        editor.putString(KEY_ATTENDANCE_RATE, rate)
        editor.apply()
    }
    fun fetchSelectedStudentNisn(): String? {
        val nisn = prefs.getString(STUDENT_NISN, null)
        return nisn
    }

    fun fetchSelectedChildId(): Int {
        return prefs.getInt(SELECTED_CHILD_ID, 0)
    }

    fun getSelectedClassId(): Int {
        return prefs.getInt(KEY_SELECTED_CLASS_ID, -1)
    }

    fun saveUser(user: User) {
        val userJson = gson.toJson(user)
        editor.putString(KEY_USER_DATA, userJson)
        editor.putBoolean(KEY_IS_LOGIN, true)
        editor.apply()
    }

    fun getUser(): User? {
        val json = prefs.getString(KEY_USER_DATA, null)
        return if (json != null) {
            try {
                gson.fromJson(json, User::class.java)
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    }

    fun createLoginSession(name: String, role: String, email: String, photoUrl: String?, childCount: Int) {
        editor.putBoolean(KEY_IS_LOGIN, true)
        editor.putString(KEY_NAME, name)
        editor.putString(KEY_ROLE, role)
        editor.putString(KEY_EMAIL, email)
        editor.putString(KEY_PHOTO_URL, photoUrl)
        editor.putInt(CHILD_COUNT, childCount)
        editor.apply()
    }

    fun saveFcmToken(token: String) {
        prefs.edit().putString("fcm_token", token).apply()
    }

    fun fetchFcmToken(): String? = prefs.getString("fcm_token", null)

    fun getUserName(): String? = prefs.getString(KEY_NAME, "Pengguna")
    fun getUserRole(): String? = prefs.getString(KEY_ROLE, "Tamu")
    fun getUserPhoto(): String? = prefs.getString(KEY_PHOTO_URL, null)
    fun getUserEmail(): String? = prefs.getString(KEY_EMAIL, "user@eduface.id")
    fun getChildCount(): Int = prefs.getInt(CHILD_COUNT, 0)
    fun getAttendanceRate(): String = prefs.getString(KEY_ATTENDANCE_RATE, "0%") ?: "0%"

    fun logout() {
        editor.clear()
        editor.apply()
    }
}