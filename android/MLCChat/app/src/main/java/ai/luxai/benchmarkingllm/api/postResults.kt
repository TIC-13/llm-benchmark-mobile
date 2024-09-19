package ai.luxai.benchmarkingllm.api

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

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
    suspend fun createPost(@Body postData: PostResult): Response<Any>
}

val retrofit = Retrofit.Builder()
    .baseUrl("http://localhost:3030/")
    .addConverterFactory(GsonConverterFactory.create())
    .build()

val apiService = retrofit.create(ApiService::class.java)

fun postResult(postData: PostResult) {
    GlobalScope.launch(Dispatchers.IO) {
        try {
            val response: Response<Any> = apiService.createPost(postData)
            if (response.isSuccessful) {
                Log.d("post", "Sent result over network")
            } else {
                Log.d("post", "Post failed " + response.code())
            }
        } catch (e: Exception) {
            Log.e("post", e.toString())
        }
    }
}



