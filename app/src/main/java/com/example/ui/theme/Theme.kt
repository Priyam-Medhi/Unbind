package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val SaharaLightColorScheme = lightColorScheme(
    primary = SaharaSienna,
    onPrimary = SaharaAlabaster,
    primaryContainer = SaharaSiennaContainer,
    onPrimaryContainer = SaharaSiennaDark,
    secondary = SaharaTaupe,
    onSecondary = SaharaAlabaster,
    secondaryContainer = SaharaParchment,
    onSecondaryContainer = SaharaEspresso,
    tertiary = SaharaDustyRose,
    onTertiary = SaharaAlabaster,
    tertiaryContainer = SaharaDustyRoseContainer,
    onTertiaryContainer = SaharaDustyRose,
    background = SaharaLinen,
    onBackground = SaharaEspresso,
    surface = SaharaAlabaster,
    onSurface = SaharaEspresso,
    surfaceVariant = SaharaSandLow,
    onSurfaceVariant = SaharaTaupe,
    surfaceContainerLowest = SaharaAlabaster,
    surfaceContainerLow = SaharaSandLow,
    surfaceContainer = SaharaParchment,
    surfaceContainerHigh = SaharaStoneHigh,
    outline = SaharaHairlineSolid,
    outlineVariant = SaharaHairline,
    error = SaharaDustyRose,
    onError = SaharaAlabaster,
    errorContainer = SaharaDustyRoseContainer,
    onErrorContainer = SaharaDustyRose,
)

private val SaharaDarkColorScheme = darkColorScheme(
    primary = SaharaSiennaLight,
    onPrimary = SaharaDarkBackground,
    primaryContainer = SaharaDarkSiennaContainer,
    onPrimaryContainer = SaharaSiennaContainer,
    secondary = SaharaDarkOnSurfaceVariant,
    onSecondary = SaharaDarkBackground,
    secondaryContainer = SaharaDarkSurfaceHigh,
    onSecondaryContainer = SaharaDarkOnSurface,
    tertiary = Color(0xFFD88484),
    onTertiary = SaharaDarkBackground,
    tertiaryContainer = SaharaDarkRoseContainer,
    onTertiaryContainer = Color(0xFFF6D2D2),
    background = SaharaDarkBackground,
    onBackground = SaharaDarkOnSurface,
    surface = SaharaDarkSurface,
    onSurface = SaharaDarkOnSurface,
    surfaceVariant = SaharaDarkSurfaceLow,
    onSurfaceVariant = SaharaDarkOnSurfaceVariant,
    surfaceContainerLowest = SaharaDarkSurface,
    surfaceContainerLow = SaharaDarkSurfaceLow,
    surfaceContainer = SaharaDarkSurfaceHigh,
    surfaceContainerHigh = Color(0xFF473B34),
    outline = SaharaDarkOutline,
    outlineVariant = SaharaDarkOutline,
    error = Color(0xFFE08B8B),
    onError = SaharaDarkBackground,
    errorContainer = SaharaDarkRoseContainer,
    onErrorContainer = Color(0xFFF8D6D6),
)

@Immutable
data class SaharaExtendedColors(
    val softWash: Color,
    val success: Color,
    val successContainer: Color,
    val subtleShadow: Color,
    val cardBorder: Color,
)

val LocalSaharaColors = staticCompositionLocalOf {
    SaharaExtendedColors(
        softWash = SaharaSiennaSoftWash,
        success = SaharaSuccessSage,
        successContainer = SaharaSuccessContainer,
        subtleShadow = Color(0x0A3A302A),
        cardBorder = SaharaHairline,
    )
}

private val UnbindShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

@Composable
fun UnbindTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) SaharaDarkColorScheme else SaharaLightColorScheme
    val extendedColors = if (darkTheme) {
        SaharaExtendedColors(
            softWash = Color(0xFF33261E),
            success = Color(0xFF9EC095),
            successContainer = Color(0xFF263324),
            subtleShadow = Color(0x22000000),
            cardBorder = SaharaDarkOutline,
        )
    } else {
        SaharaExtendedColors(
            softWash = SaharaSiennaSoftWash,
            success = SaharaSuccessSage,
            successContainer = SaharaSuccessContainer,
            subtleShadow = Color(0x0A3A302A),
            cardBorder = SaharaHairline,
        )
    }

    CompositionLocalProvider(LocalSaharaColors provides extendedColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = UnbindTypography,
            shapes = UnbindShapes,
            content = content,
        )
    }
}
