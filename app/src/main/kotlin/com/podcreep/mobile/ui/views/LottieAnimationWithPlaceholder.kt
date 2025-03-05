package com.podcreep.mobile.ui.views

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionResult
import com.airbnb.lottie.compose.LottieDynamicProperties

@Composable
fun LottieAnimationWithPlaceholder(
    lottieCompositionResult: LottieCompositionResult,
    progress: () -> Float,
    placeholder: ImageVector,
    contentDescription: String,
    modifier: Modifier = Modifier,
    dynamicProperties: LottieDynamicProperties? = null,
) {
    // False positive - https://issuetracker.google.com/issues/349411310
    @Suppress("ProduceStateDoesNotAssignValue")
    val isCompositionReady by produceState(initialValue = false, producer = {
        lottieCompositionResult.await()
        value = true
    })

    if (isCompositionReady) {
        LottieAnimation(
            modifier = modifier,
            composition = lottieCompositionResult.value,
            progress = progress,
            dynamicProperties = dynamicProperties,
        )
    } else {
        Icon(
            modifier = modifier,
            imageVector = placeholder,
            contentDescription = contentDescription,
        )
    }
}