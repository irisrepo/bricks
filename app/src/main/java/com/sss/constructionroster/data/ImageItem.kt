import android.net.Uri

data class ImageItem(
    val imageRes: Int,
    val title: String,
    val imageUri: Uri? = null
) 