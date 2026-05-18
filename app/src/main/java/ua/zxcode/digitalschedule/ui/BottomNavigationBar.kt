package ua.zxcode.digitalschedule.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

sealed class BottomNavItem(val label: String, val icon: ImageVector) {
    object Home : BottomNavItem("Головна", Icons.Filled.Home)
    object Edit : BottomNavItem("Редагування", Icons.Filled.Edit)
    object Settings : BottomNavItem("Налаштування", Icons.Filled.Settings)
}

@Composable
fun BottomNavigationBar(accentColor: Color, selectedIndex: Int, onItemSelected: (Int) -> Unit) {
    val items = listOf(BottomNavItem.Home, BottomNavItem.Edit, BottomNavItem.Settings)
    val islandShape = RoundedCornerShape(28.dp)
    val islandBackgroundColor = Color(red = 0.98f, green = 0.98f, blue = 0.98f, alpha = 0.1f)

    val density = LocalDensity.current
    var maxWidthDp by remember { mutableStateOf(0.dp) }
    val buttonWidthDp = maxWidthDp / items.size

    val indicatorOffsetX by animateDpAsState(
        targetValue = buttonWidthDp * selectedIndex,
        label = "IndicatorOffset",
        animationSpec = tween(durationMillis = 250)
    )

    val indicatorColor by animateColorAsState(
        targetValue = islandBackgroundColor.copy(alpha = 0.22f),
        label = "IndicatorColor"
    )

    Box(
        modifier = Modifier
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 16.dp)
            .fillMaxWidth()
            .height(54.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .shadow(elevation = 14.dp, shape = islandShape, clip = false)
                .clip(islandShape)
                .background(islandBackgroundColor)
                .blur(20.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .clip(islandShape)
                .background(islandBackgroundColor)
                .border(width = 1.dp, color = accentColor.copy(alpha = 0.956f), shape = islandShape)
                .onSizeChanged { size ->
                    maxWidthDp = with(density) { size.width.toDp() }
                }
        ) {
            if (maxWidthDp > 0.dp) {
                Box(
                    modifier = Modifier
                        .width(buttonWidthDp)
                        .height(52.dp)
                        .align(Alignment.CenterStart)
                        .offset(x = indicatorOffsetX, y = 0.dp)
                        .padding(horizontal = 1.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(indicatorColor)
                        .border(
                            width = 2.dp,
                            color = accentColor.copy(alpha = 1f),
                            shape = RoundedCornerShape(24.dp)
                        )
                        .blur(20.dp)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth().fillMaxHeight(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEachIndexed { index, item ->
                    val isSelected = selectedIndex == index

                    val iconColor by animateColorAsState(
                        targetValue = if (isSelected) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        label = "IconColor"
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { onItemSelected(index) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.label,
                            tint = iconColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}