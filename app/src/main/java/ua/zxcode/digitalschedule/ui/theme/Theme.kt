package ua.zxcode.digitalschedule.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
fun DigitalScheduleTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    accentColor: ua.zxcode.digitalschedule.model.AccentColor = ua.zxcode.digitalschedule.model.AccentColor.BLUE,
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val accent = when (accentColor) {
        ua.zxcode.digitalschedule.model.AccentColor.RED -> androidx.compose.ui.graphics.Color(0xFFFF1744)
        ua.zxcode.digitalschedule.model.AccentColor.ORANGE -> androidx.compose.ui.graphics.Color(0xFFFF9100)
        ua.zxcode.digitalschedule.model.AccentColor.YELLOW -> androidx.compose.ui.graphics.Color(0xFFFFEA00)
        ua.zxcode.digitalschedule.model.AccentColor.GREEN -> androidx.compose.ui.graphics.Color(0xFF00E676)
        ua.zxcode.digitalschedule.model.AccentColor.BLUE -> androidx.compose.ui.graphics.Color(0xFF2979FF)
        ua.zxcode.digitalschedule.model.AccentColor.INDIGO -> androidx.compose.ui.graphics.Color(0xFF651FFF)
        ua.zxcode.digitalschedule.model.AccentColor.VIOLET -> androidx.compose.ui.graphics.Color(0xFFD500F9)
    }
    MaterialTheme(
        colorScheme = colorScheme.copy(primary = accent),
        typography = Typography,
        content = content
    )
}