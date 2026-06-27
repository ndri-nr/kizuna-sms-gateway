package com.kizunagateway.core.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.kizunagateway.core.ui.theme.KizunaColors

@Composable
fun PowerSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    val scale by animateFloatAsState(
        targetValue = if (checked) 1.05f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "scale"
    )
    
    val containerColor by animateColorAsState(
        targetValue = if (checked) KizunaColors.Primary.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.2f),
        animationSpec = tween(500),
        label = "containerColor"
    )

    val iconColor by animateColorAsState(
        targetValue = if (checked) KizunaColors.Primary else KizunaColors.Muted,
        animationSpec = tween(500),
        label = "iconColor"
    )

    val interactionSource = remember { MutableInteractionSource() }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .size(280.dp)
                .scale(scale)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = { onCheckedChange(!checked) }
                ),
            contentAlignment = Alignment.Center
        ) {
            // Outer Glow (Animated when checked)
            if (checked) {
                Box(
                    modifier = Modifier
                        .size(250.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    KizunaColors.Primary.copy(alpha = glowAlpha),
                                    Color.Transparent
                                )
                            )
                        )
                )
            }

            // Outer Ring
            Box(
                modifier = Modifier
                    .size(210.dp)
                    .clip(CircleShape)
                    .background(containerColor)
            )

            // Main Button
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                KizunaColors.Surface.copy(alpha = 0.9f),
                                KizunaColors.Surface
                            )
                        )
                    )
                    .padding(2.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PowerSettingsNew,
                    contentDescription = if (checked) "Turn Off" else "Turn On",
                    modifier = Modifier.size(80.dp),
                    tint = iconColor
                )
            }
        }
    }
}
