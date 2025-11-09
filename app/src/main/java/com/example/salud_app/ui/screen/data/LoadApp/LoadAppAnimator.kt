package com.example.salud_app.ui.screen.data.LoadApp

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import com.example.salud_app.R
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun SplashScreenWithAnimation(
    onAnimationComplete: () -> Unit
) {
    val logoAlpha = remember { Animatable(0f) }
    val logoScale = remember { Animatable(0.7f) }
    val logoOffsetY = remember { Animatable(-60f) }

    val textAlpha = remember { Animatable(0f) }
    val textOffsetY = remember { Animatable(40f) }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            Image(
                painter = painterResource(R.drawable.salud_logo),
                contentDescription = null,
                modifier = Modifier
                    .size(300.dp)
                    .alpha(logoAlpha.value)
                    .scale(logoScale.value)
                    .offset(y = logoOffsetY.value.dp)
            )
            Text(
                text = buildAnnotatedString {
                    withStyle(
                        style = SpanStyle(
                            color = Color(0xFF006EE9),
                            fontWeight = FontWeight.Bold,
                            fontStyle = FontStyle.Italic

                        )
                    ) {
                        append("Salud")
                    }

                    withStyle(
                        style = SpanStyle(
                            color = Color.Black,
                            fontWeight = FontWeight.Normal,
                            fontStyle = FontStyle.Normal
                        )
                    ) {
                        append(" sức khoẻ của bạn là niềm vui của chúng tôi")
                    }
                },
                fontSize = 25.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(start = 50.dp, end = 50.dp)
                    .alpha(textAlpha.value)
                    .offset(y = textOffsetY.value.dp)
            )

        }
    }

    LaunchedEffect(Unit) {
        logoAlpha.animateTo(1f, tween(300))
        logoOffsetY.animateTo(
            targetValue = 0f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )

        // Logo scale bounce
        logoScale.animateTo(
            1.15f,
            animationSpec = tween(150)
        )
        logoScale.animateTo(
            1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessVeryLow
            )
        )


        // Text fade + slide up
        textAlpha.animateTo(1f, tween(250))
        textOffsetY.animateTo(
            0f,
            spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )

        delay(450)
        onAnimationComplete()
    }
}

@Preview
@Composable
fun SplashScreenWithAnimationPreview() {
    SplashScreenWithAnimation(
        onAnimationComplete = {}
    )
}
