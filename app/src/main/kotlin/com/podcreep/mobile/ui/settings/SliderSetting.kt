package com.podcreep.mobile.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

@Composable
fun SliderSetting(
  minValue: Float,
  maxValue: Float,
  value: Flow<Float>,
  steps: Int,
  valueFormatter: (Float) -> String,
  valueHandler: (Float) -> Unit
) {
  Row(
    modifier = Modifier.height(IntrinsicSize.Min)
  ) {
    val sliderPosition = value.collectAsStateWithLifecycle(initialValue = minValue)

    Slider(
      value = sliderPosition.value,
      steps = steps,
      onValueChange = { valueHandler(it) },
      valueRange = minValue .. maxValue,
      modifier = Modifier
        .weight(1f)
        .padding(16.dp)
    )
    Text(
      text = valueFormatter(sliderPosition.value),
      textAlign = TextAlign.Center,
      fontWeight = FontWeight.Bold,
      fontSize = 20.sp,
      modifier = Modifier
        .width(100.dp)
        .fillMaxHeight()
        .wrapContentHeight(align = Alignment.CenterVertically)
    )
  }
}