package com.podcreep.mobile.util

import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetValue

/**
 * Align fraction states into single value
 *
 *  1.0f - Expanded
 *  0.0f - Collapsed
 */
@OptIn(ExperimentalMaterial3Api::class)
fun BottomSheetScaffoldState.currentFraction(totalHeight: Float): Float {
  if (!bottomSheetState.isVisible) {
    return 0f
  }

  val offset = kotlin.runCatching { bottomSheetState.requireOffset() }.getOrDefault(0f)
  val fraction = if (totalHeight <= 0f) 0f else 1f - (offset / totalHeight)
  val targetValue = bottomSheetState.targetValue
  val currentValue = bottomSheetState.currentValue

  return when {
    currentValue == SheetValue.PartiallyExpanded && targetValue == SheetValue.PartiallyExpanded -> 0f
    currentValue == SheetValue.Expanded && targetValue == SheetValue.Expanded -> 1f
    currentValue == SheetValue.PartiallyExpanded && targetValue == SheetValue.Expanded -> fraction
    else -> 1f - fraction
  }
}