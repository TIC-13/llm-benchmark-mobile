package ai.luxai.benchmarkingllm.utils.benchmark
import ai.luxai.benchmarkingllm.interfaces.BenchmarkingResult
import ai.luxai.benchmarkingllm.interfaces.toBenchmarkingResult
import ai.luxai.benchmarkingllm.interfaces.toJson
import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore("post_result_datastore")

suspend fun savePostResult(context: Context, postResult: BenchmarkingResult) {
    val key = getPostResultKey(postResult.name)
    context.dataStore.edit { preferences ->
        preferences[key] = postResult.toJson()
    }
}

fun getPostResult(context: Context, llmModelName: String): Flow<Unit> {
    val key = getPostResultKey(llmModelName)
    return context.dataStore.data.map { preferences ->
        preferences[key]?.toBenchmarkingResult()
    }
}

// Retrieve all PostResult entries from DataStore
fun getAllPostResults(context: Context): Flow<List<BenchmarkingResult>> {
    return context.dataStore.data.map { preferences ->
        preferences.asMap().values.mapNotNull { value ->
            (value as? String)?.toBenchmarkingResult()
        }
    }
}

fun getPostResultKey(llmModelName: String) = stringPreferencesKey("post_result_$llmModelName")
