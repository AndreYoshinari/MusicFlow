package br.com.musicflow.designsystem.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

data class MusicFlowDestination(
    val label: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector = icon,
)

@Composable
fun MusicFlowBottomNavigation(
    destinations: List<MusicFlowDestination>,
    selectedIndex: Int,
    onDestinationSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    NavigationBar(
        modifier = modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        destinations.forEachIndexed { index, destination ->
            val selected = index == selectedIndex
            NavigationBarItem(
                selected = selected,
                onClick = { onDestinationSelected(index) },
                icon = {
                    Icon(
                        imageVector = if (selected) destination.selectedIcon else destination.icon,
                        contentDescription = null,
                    )
                },
                label = {
                    Text(
                        text = destination.label,
                        style = MaterialTheme.typography.labelMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                alwaysShowLabel = true,
                colors = MusicFlowComponentDefaults.navigationItemColors(),
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF09090B, widthDp = 360)
@Preview(showBackground = true, backgroundColor = 0xFF09090B, widthDp = 840, name = "Largo")
@Composable
private fun MusicFlowBottomNavigationPreview() {
    MusicFlowPreview(padding = 0.dp) {
        MusicFlowBottomNavigation(
            destinations = listOf(
                MusicFlowDestination("Início", Icons.Outlined.Home, Icons.Filled.Home),
                MusicFlowDestination("Busca", Icons.Outlined.Search, Icons.Filled.Search),
                MusicFlowDestination("Biblioteca", Icons.Outlined.LibraryMusic),
            ),
            selectedIndex = 0,
            onDestinationSelected = {},
        )
    }
}
