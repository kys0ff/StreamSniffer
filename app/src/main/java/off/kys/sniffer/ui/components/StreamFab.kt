package off.kys.sniffer.ui.components

import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import off.kys.sniffer.R

@Composable
fun StreamFab(
    badgeCount: Int,
    onClick: () -> Unit
) {
    BadgedBox(
        badge = {
            if (badgeCount > 0) Badge { Text("$badgeCount") }
        }
    ) {
        FloatingActionButton(onClick = onClick) {
            Icon(
                painter = painterResource(R.drawable.round_stream_24),
                contentDescription = stringResource(R.string.streams)
            )
        }
    }
}