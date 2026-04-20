package off.kys.sniffer.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import off.kys.sniffer.R
import java.net.URL

@Composable
fun StreamBottomSheet(urls: List<String>) {
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .navigationBarsPadding()
    ) {
        item {
            Text(
                text = stringResource(R.string.detected_streams),
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        items(urls) { url ->
            StreamItem(
                url = url,
                onCopy = {
                    val clipboard =
                        context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("URL", url))
                }
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
        }
    }
}

@Composable
private fun StreamItem(
    url: String,
    onCopy: () -> Unit
) {
    val extension = url.substringAfterLast(".", "unknown").split("?").first().uppercase()
    val host = remember(url) {
        runCatching { URL(url).host }.getOrDefault("Unknown Host")
    }

    ListItem(
        overlineContent = {
            Text(
                text = "Format: $extension",
                color = MaterialTheme.colorScheme.primary
            )
        },
        headlineContent = { Text(url, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        supportingContent = {
            Text(
                text = stringResource(R.string.source, host),
                style = MaterialTheme.typography.bodySmall
            )
        },
        trailingContent = {
            IconButton(onClick = onCopy) {
                Icon(
                    painter = painterResource(R.drawable.round_content_copy_24),
                    contentDescription = stringResource(R.string.copy)
                )
            }
        },
        leadingContent = {
            Icon(
                painter = painterResource(
                    if (extension in listOf("M3U8", "MPD")) R.drawable.round_live_tv_24
                    else R.drawable.round_movie_24
                ),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary
            )
        }
    )
}