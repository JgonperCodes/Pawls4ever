package com.example.pawls4ever.navigation

import android.app.VoiceInteractor
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.google.gson.Gson
import com.google.gson.JsonObject
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import java.io.IOException

//He tenido que hacer un objeto para llamarlo en funciones no COMPOSABLE
object ImageUploader {
    fun uploadImageToImgur(imageBytes: ByteArray, onResult: (String?) -> Unit) {
        val client = OkHttpClient()

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                "image",
                "file.png",
                RequestBody.create("image/png".toMediaType(), imageBytes)
            )
            .build()

        val request = Request.Builder()
            .url("https://api.imgur.com/3/image")
            .addHeader(
                "Authorization",
                "Client-ID 3ea6f610cfd81d4" //MI CLIENTE DE IMGUR
            )
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("ImgurUpload", "La subida de imagen ha fallado: ${e.message}")
                onResult(null)
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    val errorBody = response.body?.string()
                    Log.e("ImgurUpload", "La subida de imagen ha fallado: ${response.code} - ${response.message}")
                    Log.e("ImgurUpload", "Detalles: $errorBody")
                    onResult(null)
                    return
                }
                try {
                    val jsonResponse = Gson().fromJson(response.body?.string(), JsonObject::class.java)
                    val uploadedImageUrl = jsonResponse["data"]?.asJsonObject?.get("link")?.asString
                    onResult(uploadedImageUrl)
                } catch (e: Exception) {
                    Log.e("ImgurUpload", "Fallo al recibir respuesta: ${e.message}")
                    onResult(null)
                }
            }
        })
    }
}

//Funcion para subir composable
@Composable
fun uploadImageToImgurComposable(imageUri: Uri, onResult: (String?) -> Unit) {
    val context = LocalContext.current
    val contentResolver = context.contentResolver

    LaunchedEffect(imageUri) {
        try {
            val inputStream = contentResolver.openInputStream(imageUri)
            val imageBytes = inputStream?.readBytes()
            inputStream?.close()

            if (imageBytes == null) {
                Log.e("ImgurUpload", "Fallo al leer los bytes a URL")
                onResult(null)
                return@LaunchedEffect
            }

            // Para comprobar el tamaño de la imagen, maximo 10MB
            if (imageBytes.size > 10 * 1024 * 1024) { // 10 MB
                Log.e("ImgurUpload", "La imagen excede el tamaño permitido")
                onResult(null)
                return@LaunchedEffect
            }

            // Call the upload logic
            ImageUploader.uploadImageToImgur(imageBytes, onResult)
        } catch (e: Exception) {
            Log.e("ImgurUpload", "Error a la hora de manejar la subida: ${e.message}")
            onResult(null)
        }
    }
}