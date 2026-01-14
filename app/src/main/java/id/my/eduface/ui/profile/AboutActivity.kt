package id.my.eduface.ui.profile

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import id.my.eduface.databinding.ActivityAboutBinding

class AboutActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAboutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { onBackPressed() }

        try {
            val pInfo = packageManager.getPackageInfo(packageName, 0)
            val version = pInfo.versionName
            binding.tvVersion.text = "Versi $version"
        } catch (e: Exception) {
            e.printStackTrace()
            binding.tvVersion.text = "Versi 1.0.0"
        }
    }
}