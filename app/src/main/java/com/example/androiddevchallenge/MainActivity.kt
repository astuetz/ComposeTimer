/*
 * Copyright 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.androiddevchallenge

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.androiddevchallenge.ui.theme.MyTheme
import com.example.androiddevchallenge.ui.theme.timerFontFamily
import dev.chrisbanes.accompanist.insets.ProvideWindowInsets

@ExperimentalAnimationApi
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            MyTheme {
                ProvideWindowInsets {
                    TimerScreen()
                }
            }
        }
    }
}

@ExperimentalAnimationApi
@Composable
fun TimerScreen(vm: TimerViewModel = viewModel()) {

    val triggerAnimation = vm.activeTimerValue == vm.initialTimerValue

    val targetValue = if (triggerAnimation) {
        vm.activeTimerValue.toFloat() / vm.initialTimerValue.toFloat()
    } else {
        0f
    }

    val duration = if (vm.isTimerActive && vm.activeTimerValue == vm.initialTimerValue) {
        // timer started
        500
    } else if (!vm.isTimerActive) {
        // timer cancelled
        250
    } else {
        // timer active
        (vm.initialTimerValue - 1) * 1000
    }

    val easing = if (triggerAnimation || !vm.isTimerActive) {
        FastOutSlowInEasing
    } else {
        LinearEasing
    }

    val animatedFraction: Float by animateFloatAsState(
        targetValue = targetValue,
        animationSpec = tween(duration, easing = easing)
    )

    val displayedTimerValue = if (vm.isTimerActive) {
        vm.activeTimerValue.coerceAtLeast(0).toString()
    } else {
        vm.initialTimerValue.toString()
    }

    val valueTextSize: Int by animateIntAsState(
        targetValue = if (vm.isTimerActive) {
            when (vm.activeTimerValue) {
                3 -> 200
                2 -> 220
                1 -> 240
                0 -> 260
                else -> 180
            }
        } else {
            120
        },
        // having animationSpec with a startDelay in here breaks other animations :/
    )

    Box(
        Modifier.fillMaxSize(),
    ) {

        // decrease
        TimerControlButton(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 16.dp),
            visible = !vm.isTimerActive && vm.initialTimerValue > TimerViewModel.MIN_VALUE,
            enter = slideInHorizontally({ -(it * 2) }),
            imageVector = Icons.Default.Remove,
            contentDescription = "decrease",
            onClick = { vm.decreaseTimerValue(TimerViewModel.CONTROL_STEP) }
        )

        // increase
        TimerControlButton(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp),
            visible = !vm.isTimerActive,
            enter = slideInHorizontally({ it * 2 }),
            imageVector = Icons.Default.Add,
            contentDescription = "increase",
            onClick = { vm.increaseTimerValue(TimerViewModel.CONTROL_STEP) }
        )

        // progress
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(animatedFraction)
                .align(Alignment.BottomCenter),
            color = MaterialTheme.colors.primary,
        ) {}

        // value
        Text(
            modifier = Modifier
                .align(Alignment.Center),
            style = TextStyle(
                fontFamily = timerFontFamily,
                fontWeight = FontWeight.W700,
                fontSize = valueTextSize.sp,
            ),
            color = MaterialTheme.colors.onBackground,
            text = displayedTimerValue,
            textAlign = TextAlign.Center,
        )

        // bottom bar
        TimerBottomBar(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp),
            isTimerActive = vm.isTimerActive,
            visible = !vm.isTimerActive || vm.activeTimerValue > 0,
            onStartClick = { vm.start() },
            onCancelClick = { vm.cancel() },
        )
    }
}

@ExperimentalAnimationApi
@Composable
fun TimerControlButton(
    modifier: Modifier = Modifier,
    visible: Boolean,
    enter: EnterTransition,
    imageVector: ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {

    AnimatedVisibility(
        visible = visible,
        enter = enter,
        exit = slideOutHorizontally({ -(it * 2) }),
        modifier = modifier,
    ) {
        Surface(
            shape = CircleShape,
            modifier = Modifier.size(56.dp)
        ) {
            Icon(
                modifier = Modifier.clickable { onClick() },
                imageVector = imageVector,
                contentDescription = contentDescription,
                tint = MaterialTheme.colors.onBackground,
            )
        }
    }
}

@Composable
fun TimerBottomBar(
    modifier: Modifier = Modifier,
    isTimerActive: Boolean,
    visible: Boolean,
    onStartClick: () -> Unit,
    onCancelClick: () -> Unit,
) {

    if (visible) {
        Box(
            modifier = modifier,
        ) {
            if (!isTimerActive) {
                FloatingActionButton(
                    modifier = Modifier.size(64.dp),
                    onClick = onStartClick,
                    backgroundColor = MaterialTheme.colors.primary,
                ) {
                    Icon(
                        modifier = Modifier.size(32.dp),
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "start",
                    )
                }
            } else {
                OutlinedButton(onClick = onCancelClick) {
                    Text(text = "CANCEL")
                }
            }
        }
    }
}
