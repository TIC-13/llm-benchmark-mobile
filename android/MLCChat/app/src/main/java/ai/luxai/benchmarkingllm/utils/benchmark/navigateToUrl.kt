package ai.luxai.benchmarkingllm.utils.benchmark

import android.content.Context
import android.content.Intent
import android.net.Uri

fun navigateToUrl(context: Context, uri: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
    context.startActivity(intent)
}