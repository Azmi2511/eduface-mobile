package id.my.eduface.utils

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import id.my.eduface.R

object ToastUtils {

    fun showSuccess(context: Context, message: String) {
        showCustomToast(context, message, R.drawable.ic_check_circle, "#4CAF50", Toast.LENGTH_SHORT)
    }

    fun showError(context: Context, message: String) {
        showCustomToast(context, message, R.drawable.ic_error, "#F44336", Toast.LENGTH_LONG)
    }

    fun showInfo(context: Context, message: String) {
        showCustomToast(context, message, R.drawable.ic_info, "#2196F3", Toast.LENGTH_SHORT)
    }

    fun showWarning(context: Context, message: String) {
        showCustomToast(context, message, R.drawable.ic_warning, "#FF9800", Toast.LENGTH_SHORT)
    }

    fun showShort(context: Context, message: String) {
        showCustomToast(context, message, R.drawable.ic_info, "#334155", Toast.LENGTH_SHORT)
    }

    private fun showCustomToast(
        context: Context,
        message: String,
        iconRes: Int,
        colorHex: String,
        duration: Int
    ) {
        try {
            val inflater = LayoutInflater.from(context)
            val layout = inflater.inflate(R.layout.layout_custom_toast, null)

            val text: TextView = layout.findViewById(R.id.tv_toast_message)
            text.text = message

            val icon: ImageView = layout.findViewById(R.id.iv_toast_icon)
            icon.setImageResource(iconRes)

            val card: CardView = layout.findViewById(R.id.card_toast)
            card.setCardBackgroundColor(Color.parseColor(colorHex))

            Toast(context.applicationContext).apply {
                setDuration(duration)
                view = layout
                show()
            }
        } catch (e: Exception) {
            // Fallback ke Toast standar jika terjadi error pada custom view
            Toast.makeText(context, message, duration).show()
        }
    }
}