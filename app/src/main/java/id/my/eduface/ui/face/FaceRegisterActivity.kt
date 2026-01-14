package id.my.eduface.ui.face

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import id.my.eduface.data.network.ApiClient
import id.my.eduface.databinding.ActivityFaceRegisterBinding
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class FaceRegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFaceRegisterBinding
    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExecutor: ExecutorService

    private val poses = listOf("depan", "kiri", "kanan")
    private val instructions = listOf(
        "Hadapkan Wajah ke DEPAN",
        "Hadapkan Wajah Serong KIRI",
        "Hadapkan Wajah Serong KANAN"
    )
    private var currentStep = 0

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) startCamera() else finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFaceRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }

        binding.btnCapture.setOnClickListener { takePhoto() }
        cameraExecutor = Executors.newSingleThreadExecutor()
        updateUI()
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
            }
            imageCapture = ImageCapture.Builder().build()
            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
            } catch (exc: Exception) {
                Toast.makeText(this, "Gagal membuka kamera", Toast.LENGTH_SHORT).show()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return
        setLoading(true)

        val photoFile = File(
            externalCacheDir,
            SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US).format(System.currentTimeMillis()) + ".jpg"
        )

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    setLoading(false)
                    Toast.makeText(baseContext, "Gagal mengambil foto", Toast.LENGTH_SHORT).show()
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    uploadPhoto(photoFile)
                }
            }
        )
    }

    private fun uploadPhoto(file: File) {
        lifecycleScope.launch {
            try {
                val pose = poses[currentStep]
                val reqPose = pose.toRequestBody("text/plain".toMediaTypeOrNull())
                val reqFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
                val body = MultipartBody.Part.createFormData("file", file.name, reqFile)

                val response = ApiClient.getApiService(this@FaceRegisterActivity).registerFace(reqPose, body)

                if (response.isSuccessful && response.body()?.success == true) {
                    currentStep++
                    if (currentStep < poses.size) {
                        Toast.makeText(baseContext, "Berhasil! Lanjut ke pose berikutnya.", Toast.LENGTH_SHORT).show()
                        updateUI()
                    } else {
                        Toast.makeText(baseContext, "Pendaftaran Selesai!", Toast.LENGTH_LONG).show()
                        finish()
                    }
                } else {
                    val errorMsg = response.body()?.message ?: response.errorBody()?.string() ?: "Gagal Upload"
                    Toast.makeText(baseContext, errorMsg, Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(baseContext, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                setLoading(false)
                file.delete()
            }
        }
    }

    private fun updateUI() {
        if (currentStep < instructions.size) {
            binding.tvInstruction.text = instructions[currentStep]
        }
    }

    private fun setLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnCapture.isEnabled = !isLoading
        binding.btnCapture.alpha = if (isLoading) 0.5f else 1.0f
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}