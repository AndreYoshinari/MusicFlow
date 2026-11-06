package br.com.musicflow.designsystem.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import br.com.musicflow.designsystem.theme.MusicFlowSpacing

@Composable
fun MusicFlowTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    leadingIcon: ImageVector? = null,
    leadingIconDescription: String? = null,
    trailingIcon: ImageVector? = null,
    trailingIconDescription: String? = null,
    onTrailingIconClick: (() -> Unit)? = null,
    enabled: Boolean = true,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        label = { Text(label) },
        placeholder = placeholder?.let { text ->
            { Text(text) }
        },
        leadingIcon = leadingIcon?.let { icon ->
            {
                Icon(
                    imageVector = icon,
                    contentDescription = leadingIconDescription,
                    modifier = Modifier.size(MusicFlowSpacing.large),
                )
            }
        },
        trailingIcon = trailingIcon?.let { icon ->
            {
                if (onTrailingIconClick != null) {
                    MusicFlowIconButton(
                        icon = icon,
                        contentDescription = trailingIconDescription.orEmpty(),
                        onClick = onTrailingIconClick,
                    )
                } else {
                    Icon(
                        imageVector = icon,
                        contentDescription = trailingIconDescription,
                        modifier = Modifier.size(MusicFlowSpacing.large),
                    )
                }
            }
        },
        singleLine = true,
        shape = MaterialTheme.shapes.medium,
        colors = MusicFlowComponentDefaults.textFieldColors(),
        textStyle = MaterialTheme.typography.bodyLarge,
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF09090B, widthDp = 360)
@Composable
private fun MusicFlowTextFieldPreview() {
    MusicFlowPreview {
        MusicFlowTextField(
            value = "",
            onValueChange = {},
            label = "Buscar",
            placeholder = "Música, artista ou álbum",
            leadingIcon = Icons.Outlined.Search,
        )
    }
}
