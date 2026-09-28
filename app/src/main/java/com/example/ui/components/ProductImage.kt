package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.example.R
import com.example.data.OrderEntity
import com.example.data.RIYADH_NEIGHBORHOODS
import java.io.File
import java.io.FileOutputStream

@Composable
fun ProductVisualImage(
    imageKey: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    ProductImageThumbnail(
        imageKey = imageKey,
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = contentScale
    )
}

fun copyUriToInternalStorage(context: Context, uri: Uri): String? {
    return copyPickedImageToInternalStorage(context, uri)
}

@Composable
fun ProductImageThumbnail(
    imageKey: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    val context = LocalContext.current
    val customBitmap = remember(imageKey) {
        loadCustomProductBitmap(context, imageKey)
    }

    if (customBitmap != null) {
        Image(
            bitmap = customBitmap.asImageBitmap(),
            contentDescription = contentDescription,
            contentScale = contentScale,
            modifier = modifier
        )
    } else {
        val drawableRes = when (imageKey) {
            "pizza" -> R.drawable.img_pizza_feast
            "hero" -> R.drawable.img_hero_banner
            else -> R.drawable.img_burger_meal
        }
        Image(
            painter = painterResource(id = drawableRes),
            contentDescription = contentDescription,
            contentScale = contentScale,
            modifier = modifier
        )
    }
}

private fun loadCustomProductBitmap(context: Context, imageKey: String): android.graphics.Bitmap? {
    if (imageKey.isBlank() || imageKey == "burger" || imageKey == "pizza" || imageKey == "hero") {
        return null
    }
    return try {
        when {
            imageKey.startsWith("/") -> {
                val file = File(imageKey)
                if (file.exists()) BitmapFactory.decodeFile(file.absolutePath) else null
            }
            imageKey.startsWith("content://") || imageKey.startsWith("file://") -> {
                val uri = Uri.parse(imageKey)
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    BitmapFactory.decodeStream(stream)
                }
            }
            else -> null
        }
    } catch (_: Exception) {
        null
    }
}

fun copyPickedImageToInternalStorage(context: Context, uri: Uri): String? {
    return try {
        val destDir = File(context.filesDir, "product_images").apply {
            if (!exists()) mkdirs()
        }
        val destFile = File(destDir, "prod_${System.currentTimeMillis()}.jpg")
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(destFile).use { output ->
                input.copyTo(output)
            }
        }
        if (destFile.exists() && destFile.length() > 0L) {
            destFile.absolutePath
        } else {
            uri.toString()
        }
    } catch (_: Exception) {
        uri.toString()
    }
}

fun openGoogleMapsDirections(context: Context, order: OrderEntity) {
    val matchedNeighborhood = RIYADH_NEIGHBORHOODS.firstOrNull {
        it.nameAr == order.customerNeighborhood || order.customerNeighborhood.contains(it.nameAr.substringBefore(" -"))
    }
    val destLat = matchedNeighborhood?.latitude ?: (24.6600 + (1.0 - order.customerMapY) * 0.1600)
    val destLng = matchedNeighborhood?.longitude ?: (46.5900 + order.customerMapX * 0.1200)
    val addressQuery = Uri.encode("${order.customerNeighborhood} ${order.customerStreetDetails} الرياض".trim())

    // First try native Google Maps turn-by-turn navigation intent
    val navIntent = Intent(
        Intent.ACTION_VIEW,
        Uri.parse("google.navigation:q=$destLat,$destLng")
    ).apply {
        setPackage("com.google.android.apps.maps")
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    try {
        context.startActivity(navIntent)
    } catch (_: Exception) {
        // Fallback to universal Google Maps Directions URL (works in browser or any maps handler)
        val directionsUri = Uri.parse(
            "https://www.google.com/maps/dir/?api=1&destination=$destLat,$destLng&query=$addressQuery&travelmode=driving"
        )
        val webIntent = Intent(Intent.ACTION_VIEW, directionsUri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        runCatching { context.startActivity(webIntent) }
    }
}
