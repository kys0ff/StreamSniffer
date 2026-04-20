package off.kys.sniffer.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import off.kys.sniffer.R

@Composable
fun BrowserSearchBar(
    urlInput: String,
    onUrlChange: (String) -> Unit,
    onSearch: () -> Unit
) {
    Surface(tonalElevation = 3.dp) {
        TextField(
            value = urlInput,
            onValueChange = onUrlChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            placeholder = { Text(stringResource(R.string.search_or_type_url)) },
            leadingIcon = {
                Icon(
                    painter = painterResource(R.drawable.round_search_24),
                    contentDescription = null
                )
            },
            trailingIcon = {
                if (urlInput.isNotEmpty()) {
                    IconButton(onClick = { onUrlChange("") }) {
                        Icon(
                            painter = painterResource(R.drawable.round_clear_24),
                            contentDescription = stringResource(R.string.clear)
                        )
                    }
                }
            },
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            shape = MaterialTheme.shapes.medium,
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
            keyboardActions = KeyboardActions(onGo = { onSearch() })
        )
    }
}