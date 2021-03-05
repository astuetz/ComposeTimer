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

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import java.util.Timer
import java.util.TimerTask

class TimerViewModel : ViewModel() {

    private var timer: Timer? = null

    var isTimerActive by mutableStateOf(false)
        private set

    var initialTimerValue by mutableStateOf(30)
        private set

    var activeTimerValue by mutableStateOf(-1)
        private set

    fun decreaseTimerValue(delta: Int) {
        initialTimerValue = (initialTimerValue - delta).coerceAtLeast(MIN_VALUE)
    }

    fun increaseTimerValue(delta: Int) {
        initialTimerValue += delta
    }

    fun start() {

        if (timer != null) {
            // don't restart the timer
            return
        }

        isTimerActive = true

        activeTimerValue = initialTimerValue

        timer = Timer().apply {
            scheduleAtFixedRate(
                object : TimerTask() {
                    override fun run() {
                        activeTimerValue -= 1

                        if (activeTimerValue == -1) {
                            resetTimer()
                        }
                    }
                },
                1_000, 1_000
            )
        }
    }

    fun cancel() {
        resetTimer()
    }

    override fun onCleared() {
        super.onCleared()
        resetTimer()
    }

    private fun resetTimer() {
        activeTimerValue = -1
        isTimerActive = false
        timer?.cancel()
        timer = null
    }

    companion object {
        const val MIN_VALUE = 5
        const val CONTROL_STEP = 5
    }
}
