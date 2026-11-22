package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.model.LauncherAccent

private val LauncherShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(22.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(34.dp)
)

@Composable
fun NLauncherTheme(
    darkTheme: Boolean = true,
    accent: LauncherAccent = LauncherAccent.CORAL,
    content: @Composable () -> Unit
) {
    val (accentColor, darkContainer, lightContainer) = when (accent) {
        LauncherAccent.CORAL -> Triple(ElectricCoral, ElectricCoralContainer, ElectricCoralContainerLight)
        LauncherAccent.COBALT -> Triple(CobaltPulse, CobaltContainer, CobaltContainerLight)
        LauncherAccent.MINT -> Triple(SageMint, SageMintContainer, SageMintContainerLight)
        LauncherAccent.AMBER -> Triple(AmberGlow, AmberContainer, AmberContainerLight)
    }

    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = accentColor,
            onPrimary = Color.White,
            primaryContainer = darkContainer,
            onPrimaryContainer = accentColor,
            secondary = SageMint,
            onSecondary = ObsidianBg,
            secondaryContainer = ObsidianSurfaceHigh,
            onSecondaryContainer = ObsidianTextPrimary,
            tertiary = AmberGlow,
            background = ObsidianBg,
            onBackground = ObsidianTextPrimary,
            surface = ObsidianSurface,
            onSurface = ObsidianTextPrimary,
            surfaceVariant = ObsidianSurfaceHigh,
            onSurfaceVariant = ObsidianTextSecondary,
            outline = ObsidianBorder
        )
    } else {
        lightColorScheme(
            primary = accentColor,
            onPrimary = Color.White,
            primaryContainer = lightContainer,
            onPrimaryContainer = accentColor,
            secondary = CobaltPulse,
            onSecondary = Color.White,
            secondaryContainer = SandstoneSurfaceHigh,
            onSecondaryContainer = SandstoneTextPrimary,
            tertiary = AmberGlow,
            background = SandstoneBg,
            onBackground = SandstoneTextPrimary,
            surface = SandstoneSurface,
            onSurface = SandstoneTextPrimary,
            surfaceVariant = SandstoneSurfaceHigh,
            onSurfaceVariant = SandstoneTextSecondary,
            outline = SandstoneBorder
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = LauncherShapes,
        content = content
    )
}
