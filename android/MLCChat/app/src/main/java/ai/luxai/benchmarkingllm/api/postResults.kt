package ai.luxai.benchmarkingllm.api

import ai.luxai.benchmarkingllm.BuildConfig
import android.util.Log
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

data class Phone(
    val brand_name: String,
    val manufacturer: String,
    val phone_model: String,
    val total_ram: Int
)

data class LLMModel(
    val name: String
)

data class Measurement(
    val average: Double?,
    val median: Double?,
    val peak: Double?,
    val std: Double?,
)

data class PostResult(
    val phone: Phone,
    val llm_model: LLMModel,
    val load_time: Int?,
    val ram: Measurement,
    val cpu: Measurement,
    val gpu: Measurement,
    val prefill: Measurement,
    val decode: Measurement,
    val energyAverage: Double?,
    val powerAverage: Double?
)

interface ApiService {
    @POST("llmInference")
    suspend fun createPost(@Body encryptedData: Map<String, String>): Response<Any>
}

const val apiAdress = BuildConfig.API_ADRESS

val retrofit = Retrofit.Builder()
    .baseUrl("$apiAdress/")
    .addConverterFactory(GsonConverterFactory.create())
    .build()

val apiService = retrofit.create(ApiService::class.java)

const val secretKeyString = BuildConfig.API_KEY

fun encryptAndPostResult(postData: PostResult) {
    // Convert PostResult object to JSON using Gson
    val gson = Gson()
    val postDataJson = gson.toJson(postData)

    // Encrypt the JSON string
    val encryptedData = encryptData(postDataJson, secretKeyString)

    // Prepare the encrypted data to send in a JSON format
    val encryptedDataMap = mapOf("encryptedData" to encryptedData)

    // Send the encrypted data to the server
    GlobalScope.launch(Dispatchers.IO) {
        try {
            val response: Response<Any> = apiService.createPost(encryptedDataMap)
            if (response.isSuccessful) {
                Log.d("post", "Sent encrypted result over network")
            } else {
                Log.d("post", "Post failed " + response.code())
            }
        } catch (e: Exception) {
            Log.e("post", e.toString())
        }
    }
}


fun encryptData(plainText: String, stringKey: String): String {
    // Decode the Base64-encoded key string to get the key bytes
    val keyBytes = Base64.getDecoder().decode(stringKey)
    val secretKey = SecretKeySpec(keyBytes, "AES")  // Use the full keyBytes (should be 32 bytes for AES-256)

    val cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING")
    val iv = ByteArray(16)
    SecureRandom().nextBytes(iv)
    val ivParameterSpec = IvParameterSpec(iv)

    cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec)

    val encryptedData = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))

    val ivBase64 = Base64.getEncoder().encodeToString(iv)
    val encryptedDataBase64 = Base64.getEncoder().encodeToString(encryptedData)

    return "$ivBase64:$encryptedDataBase64"
}



