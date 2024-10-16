package ai.luxai.benchmarkingllm.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.text.ClickableText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration

@Composable
fun OpenLinkInBrowser(modifier: Modifier = Modifier, text: String, uri: String) {
    val context = LocalContext.current
    val annotatedString = AnnotatedString(text)

    ClickableText(
        modifier = modifier,
        text = annotatedString,
        style = TextStyle(
            color = Color(0xFF77cff8),
            textDecoration = TextDecoration.Underline
        ),
        onClick = {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
            context.startActivity(intent)
        }
    )
}
