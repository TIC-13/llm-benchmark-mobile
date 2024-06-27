package ai.mlc.mlcchat

import ai.mlc.mlcchat.interfaces.BenchmarkingResult
import android.app.Application
import androidx.lifecycle.AndroidViewModel

class ResultViewModel(application: Application) : AndroidViewModel(application) {
    var results = arrayListOf<BenchmarkingResult>()
    fun resetResults() {
        results = arrayListOf<BenchmarkingResult>()
    }
}