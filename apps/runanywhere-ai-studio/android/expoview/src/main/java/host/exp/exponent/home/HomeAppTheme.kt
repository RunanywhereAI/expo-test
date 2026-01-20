package host.exp.exponent.home

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import host.exp.exponent.services.ThemeSetting

// RunAnywhere Brand Colors
object RAColors {
  val PrimaryAccent = Color(0xFFFF5500)
  val PrimaryBlue = Color(0xFF3B82F6)
  val PrimaryGreen = Color(0xFF10B981)
  val PrimaryRed = Color(0xFFEF4444)
  val PrimaryYellow = Color(0xFFEAB308)

  // Dark Theme
  val BackgroundPrimaryDark = Color(0xFF0F172A)
  val BackgroundSecondaryDark = Color(0xFF1A1F2E)
  val BackgroundTertiaryDark = Color(0xFF252B3A)
  val TextPrimaryDark = Color(0xFFE6EDF3)
  val TextSecondaryDark = Color(0xFF94A3B8)
  val TextTertiaryDark = Color(0xFF64748B)

  // Light Theme
  val BackgroundPrimaryLight = Color(0xFFF8FAFC)
  val BackgroundSecondaryLight = Color(0xFFFFFFFF)
  val BackgroundTertiaryLight = Color(0xFFF1F5F9)
  val TextPrimaryLight = Color(0xFF0F172A)
  val TextSecondaryLight = Color(0xFF475569)
  val TextTertiaryLight = Color(0xFF94A3B8)
}

val Typography = Typography(
  bodyLarge = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Normal,
    fontSize = 16.sp,
    lineHeight = 24.sp,
    letterSpacing = 0.5.sp
  ),
  titleLarge = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Bold,
    fontSize = 22.sp,
    lineHeight = 28.sp
  ),
  titleMedium = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.SemiBold,
    fontSize = 18.sp,
    lineHeight = 24.sp
  ),
  labelLarge = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Medium,
    fontSize = 14.sp,
    lineHeight = 20.sp
  ),
  labelSmall = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Medium,
    fontSize = 11.sp,
    lineHeight = 16.sp
  )
)

val Shapes = Shapes(
  small = RoundedCornerShape(4.dp),
  medium = RoundedCornerShape(8.dp),
  large = RoundedCornerShape(12.dp)
)

// RunAnywhere Light Theme
private val LightColors = lightColorScheme(
  primary = RAColors.PrimaryAccent,
  onPrimary = Color.White,
  primaryContainer = RAColors.PrimaryAccent.copy(alpha = 0.1f),
  onPrimaryContainer = RAColors.PrimaryAccent,
  secondary = RAColors.PrimaryBlue,
  onSecondary = Color.White,
  tertiary = RAColors.PrimaryGreen,
  background = RAColors.BackgroundPrimaryLight,
  onBackground = RAColors.TextPrimaryLight,
  surface = RAColors.BackgroundSecondaryLight,
  onSurface = RAColors.TextPrimaryLight,
  surfaceVariant = RAColors.BackgroundTertiaryLight,
  onSurfaceVariant = RAColors.TextSecondaryLight,
  outline = Color(0xFFE2E8F0),
  error = RAColors.PrimaryRed,
  errorContainer = RAColors.PrimaryRed.copy(alpha = 0.1f),
  onError = Color.White
)

// RunAnywhere Dark Theme
private val DarkColors = darkColorScheme(
  primary = RAColors.PrimaryAccent,
  onPrimary = Color.White,
  primaryContainer = RAColors.PrimaryAccent.copy(alpha = 0.15f),
  onPrimaryContainer = RAColors.PrimaryAccent,
  secondary = RAColors.PrimaryBlue,
  onSecondary = Color.White,
  tertiary = RAColors.PrimaryGreen,
  background = RAColors.BackgroundPrimaryDark,
  onBackground = RAColors.TextPrimaryDark,
  surface = RAColors.BackgroundSecondaryDark,
  onSurface = RAColors.TextPrimaryDark,
  surfaceVariant = RAColors.BackgroundTertiaryDark,
  onSurfaceVariant = RAColors.TextSecondaryDark,
  outline = Color(0xFF30363D),
  error = RAColors.PrimaryRed,
  errorContainer = RAColors.PrimaryRed.copy(alpha = 0.15f),
  onError = Color.White
)

@Composable
fun HomeAppTheme(
  themeSetting: ThemeSetting,
  content: @Composable () -> Unit
) {
  val colorScheme = when (themeSetting) {
    ThemeSetting.Automatic -> if (isSystemInDarkTheme()) {
      DarkColors
    } else {
      LightColors
    }

    ThemeSetting.Dark -> DarkColors
    ThemeSetting.Light -> LightColors
  }

  MaterialTheme(
    colorScheme = colorScheme,
    shapes = Shapes,
    typography = Typography,
    content = content
  )
}
