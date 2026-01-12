package id.my.eduface

import android.app.Application
import com.google.firebase.FirebaseApp

class EdufaceApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
}