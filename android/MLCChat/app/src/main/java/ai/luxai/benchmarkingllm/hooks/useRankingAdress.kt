package ai.luxai.benchmarkingllm.hooks

import ai.luxai.benchmarkingllm.BuildConfig
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

data class RankingAddress(
    val address: String,
    val isValid: Boolean
)

@Composable
fun useRankingAddress(): RankingAddress {
    val rankingAddress = remember {
        BuildConfig.RANKING_ADDRESS
    }
    val rankingIsValid = remember {
        rankingAddress.startsWith("http")
    }

    return RankingAddress(
        address = rankingAddress,
        isValid = rankingIsValid
    )
}