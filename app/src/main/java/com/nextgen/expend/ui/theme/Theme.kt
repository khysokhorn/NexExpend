package com.nextgen.expend.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Monolith Finance design system – strict monochromatic grayscale.
 * Primary = Black (#000000), zero drop shadows, 4px corner radius on interactive elements.
 */
private val MonolithColorScheme = lightColorScheme(
    primary                = MonolithBlack,
    onPrimary              = MonolithWhite,
    primaryContainer       = Color(0xFF1B1B1B),
    onPrimaryContainer     = MonolithGray60,
    secondary              = MonolithGray37,
    onSecondary            = MonolithWhite,
    secondaryContainer     = MonolithGray87,
    onSecondaryContainer   = MonolithGray60,
    tertiary               = MonolithBlack,
    onTertiary             = MonolithWhite,
    tertiaryContainer      = Color(0xFF1B1B1B),
    onTertiaryContainer    = MonolithGray60,
    background             = MonolithGray95,
    onBackground           = MonolithGray11,
    surface                = MonolithGray95,
    onSurface              = MonolithGray11,
    surfaceVariant         = MonolithGray87,
    onSurfaceVariant       = MonolithGray30,
    outline                = MonolithOutline,
    outlineVariant         = MonolithOutlineVar,
    inverseSurface         = MonolithGray18,
    inverseOnSurface       = Color(0xFFF1F1F1),
    inversePrimary         = MonolithGray70,
    surfaceTint            = MonolithGray37,
    error                  = MonolithRed,
    onError                = MonolithWhite,
    errorContainer         = MonolithRedLight,
    onErrorContainer       = Color(0xFF93000A),
    scrim                  = MonolithBlack,
)

private val MonolithShapes = Shapes(
    extraSmall = RoundedCornerShape(2.dp),   // 0.125rem
    small      = RoundedCornerShape(4.dp),   // 0.25rem  – primary radius
    medium     = RoundedCornerShape(6.dp),   // 0.375rem
    large      = RoundedCornerShape(8.dp),   // 0.5rem
    extraLarge = RoundedCornerShape(12.dp),  // 0.75rem
)

@Composable
fun NexExpendTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = MonolithColorScheme,
        typography  = Typography,
        shapes      = MonolithShapes,
        content     = content
    )
}