package com.podcreep.mobile.ui.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.LottieComposition
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieAnimatable
import com.airbnb.lottie.compose.rememberLottieComposition
import com.podcreep.mobile.R

@Composable
public fun AnimatedPlayPauseButton(
    onPlayClick: () -> Unit,
    onPauseClick: () -> Unit,
    playing: Boolean,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    iconSize: Dp = 32.dp,
    backgroundColor: Color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.10f),
    progress: @Composable () -> Unit = {},
) {
    val compositionResult = rememberLottieComposition(
        spec = LottieCompositionSpec.Asset(
            "lottie/PlayPause.json",
        ),
    )
    val lottieProgress =
        animateLottieProgressAsState(playing = playing, composition = compositionResult.value)
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(backgroundColor),
        contentAlignment = Alignment.Center,
    ) {
        progress()
        val pauseContentDescription =
            stringResource(id = R.string.pause_button_content_description)
        val playContentDescription =
            stringResource(id = R.string.play_button_content_description)

        Button(
            onClick = { if (playing) onPauseClick() else onPlayClick() },
            modifier = modifier
                .semantics {
                    contentDescription = if (playing) {
                        pauseContentDescription
                    } else {
                        playContentDescription
                    }
                },
            enabled = enabled,
        ) {
            val contentModifier = Modifier
                .size(iconSize)
                //.align(alignment = Alignment.Center)
                //.graphicsLayer(alpha = LocalContentAlpha.current)

            LottieAnimationWithPlaceholder(
                lottieCompositionResult = compositionResult,
                progress = { lottieProgress.value },
                placeholder =
                    if (playing) ImageVector.vectorResource(R.drawable.ic_pause_black_24dp)
                    else ImageVector.vectorResource(R.drawable.ic_play_arrow_black_24dp),
                contentDescription = if (playing) pauseContentDescription else playContentDescription,
                modifier = contentModifier,
            )
        }
    }
}

@Composable
private fun animateLottieProgressAsState(
    playing: Boolean,
    composition: LottieComposition?,
): State<Float> {
    val lottieProgress = rememberLottieAnimatable()
    var firstTime by remember { mutableStateOf(true) }

    // Ensures lottie initializes to the correct progress with the playing state.
    LaunchedEffect(firstTime) {
        firstTime = false
        if (playing) {
            lottieProgress.snapTo(progress = 1f)
        } else {
            lottieProgress.snapTo(progress = 0f)
        }
    }

    LaunchedEffect(playing) {
        val targetValue = if (playing) 1f else 0f
        if (lottieProgress.progress < targetValue) {
            lottieProgress.animate(composition, speed = 1f)
        } else if (lottieProgress.progress > targetValue) {
            lottieProgress.animate(composition, speed = -1f)
        }
    }

    return lottieProgress
}
/*
@Composable
public fun AnimatedPlayPauseProgressButton(
    onPlayClick: () -> Unit,
    onPauseClick: () -> Unit,
    playing: Boolean,
    trackPositionUiModel: TrackPositionUiModel,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: ButtonColors = ButtonDefaults.iconButtonColors(),
    iconSize: Dp = 30.dp,
    tapTargetSize: DpSize = DpSize(60.dp, 60.dp),
    progressStrokeWidth: Dp = 4.dp,
    progressColor: Color = MaterialTheme.colors.primary,
    trackColor: Color = MaterialTheme.colors.onSurface.copy(alpha = 0.10f),
    backgroundColor: Color = MaterialTheme.colors.onBackground.copy(alpha = 0.10f),
    rotateProgressIndicator: Flow<Unit> = flowOf(),
) {
    val animatedProgressColor = animateColorAsState(
        targetValue = progressColor,
        animationSpec = tween(450, 0, LinearEasing),
        "Progress Colour",
    )

    AnimatedPlayPauseButton(
        onPlayClick = onPlayClick,
        onPauseClick = onPauseClick,
        enabled = enabled,
        playing = playing,
        modifier = modifier,
        colors = colors,
        iconSize = iconSize,
        backgroundColor = backgroundColor,
    ) {
        if (trackPositionUiModel.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.fillMaxSize(),
                indicatorColor = animatedProgressColor.value,
                trackColor = trackColor,
                strokeWidth = progressStrokeWidth,
            )
        } else if (trackPositionUiModel.showProgress) {
            val progress = ProgressStateHolder.fromTrackPositionUiModel(trackPositionUiModel)

            CircularProgressIndicatorFast(
                modifier = Modifier
                    .fillMaxSize()
                    .rotate(animateChangeAsRotation(rotateProgressIndicator)),
                progress = { progress.value },
                indicatorColor = animatedProgressColor.value,
                trackColor = trackColor,
                strokeWidth = progressStrokeWidth,
                tapTargetSize = tapTargetSize,
            )
        }
    }
}

@Composable
private fun animateChangeAsRotation(rotateProgressIndicator: Flow<Unit>): Float {
    // False positive - https://issuetracker.google.com/issues/349411310
    @Suppress("ProduceStateDoesNotAssignValue")
    val progressIndicatorRotation by produceState(0f, rotateProgressIndicator) {
        rotateProgressIndicator.collectLatest { value += 360 }
    }
    val animatedProgressIndicatorRotation by animateFloatAsState(
        targetValue = progressIndicatorRotation,
        animationSpec = PLAYBACK_PROGRESS_ANIMATION_SPEC,
        label = "Progress Indicator Rotation",
    )
    return animatedProgressIndicatorRotation
}

// From https://cs.android.com/androidx/platform/frameworks/support/+/androidx-main:wear/tiles/tiles-material/src/main/java/androidx/wear/tiles/material/CircularProgressIndicator.java?q=CircularProgressIndicator
@Composable
private fun CircularProgressIndicatorFast(
    progress: () -> Float,
    modifier: Modifier = Modifier,
    startAngle: Float = 270f,
    endAngle: Float = startAngle,
    indicatorColor: Color = MaterialTheme.colors.primary,
    trackColor: Color = MaterialTheme.colors.onBackground.copy(alpha = 0.1f),
    strokeWidth: Dp = ProgressIndicatorDefaults.StrokeWidth,
    tapTargetSize: DpSize,
) {
    val progressSteps = with(LocalDensity.current) {
        (tapTargetSize.width.toPx() * Math.PI).roundToInt()
    }
    val truncatedProgress by remember {
        derivedStateOf {
            roundProgress(
                progress = progress(),
                progressSteps = progressSteps,
            )
        }
    }

    val stroke = with(LocalDensity.current) {
        Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
    }

    Canvas(
        modifier
            .progressSemantics({ progress() })
            .focusable(),
    ) {
        val backgroundSweep = 360f - ((startAngle - endAngle) % 360 + 360) % 360
        val progressSweep = backgroundSweep * truncatedProgress
        // Draw a background
        drawCircularIndicator(
            startAngle,
            backgroundSweep,
            trackColor,
            stroke,
        )

        // Draw a progress
        drawCircularIndicator(
            startAngle,
            progressSweep,
            indicatorColor,
            stroke,
        )
    }
}

@Stable
private fun Modifier.progressSemantics(
    value: () -> Float,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    @IntRange(from = 0) steps: Int = 0,
): Modifier {
    // Copy of with a lambda
    // https://cs.android.com/androidx/platform/frameworks/support/+/androidx-main:compose/foundation/foundation/src/commonMain/kotlin/androidx/compose/foundation/ProgressSemantics.kt?q=progressSemantics
    return semantics(mergeDescendants = true) {
        progressBarRangeInfo =
            ProgressBarRangeInfo(value().coerceIn(valueRange), valueRange, steps)
    }
}

private fun roundProgress(progress: Float, progressSteps: Int) = if (progress == 0f) {
    0f
} else {
    (
            floor(
                progress * progressSteps,
            ) / progressSteps
            ).coerceIn(0.001f..1f)
}

private fun DrawScope.drawCircularIndicator(
    startAngle: Float,
    sweep: Float,
    color: Color,
    stroke: Stroke,
) {
    // To draw this circle we need a rect with edges that line up with the midpoint of the stroke.
    // To do this we need to remove half the stroke width from the total diameter for both sides.
    val diameter = min(size.width, size.height)
    val diameterOffset = stroke.width / 2
    val arcDimen = diameter - 2 * diameterOffset
    drawArc(
        color = color,
        startAngle = startAngle,
        sweepAngle = sweep,
        useCenter = false,
        topLeft = Offset(
            diameterOffset + (size.width - diameter) / 2,
            diameterOffset + (size.height - diameter) / 2,
        ),
        size = Size(arcDimen, arcDimen),
        style = stroke,
    )
}
*/