package com.rhappdeveloper.dreidelgameapp.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.rhappdeveloper.dreidelgameapp.model.DreidelRuleSet
import com.rhappdeveloper.dreidelgameapp.mvi.DreidelEffect
import com.rhappdeveloper.dreidelgameapp.mvi.DreidelIntent
import com.rhappdeveloper.dreidelgameapp.mvi.DreidelViewModel
import com.rhappdeveloper.dreidelgameapp.ui.util.AnimatedDreidel
import com.rhappdeveloper.dreidelgameapp.ui.util.AnimatedPot
import com.rhappdeveloper.dreidelgameapp.ui.util.SystemSoundPlayer

@Composable
fun DreidelScreen(
    systemPaddingValues: PaddingValues,
    viewModel: DreidelViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    // Add local state for rule toggle
    var ruleSet by rememberSaveable {
        mutableStateOf(DreidelRuleSet.CLASSIC)
    }

    val message = when {
        state.isSpinning -> "Spinning..."
        state.lastSide != null ->
            state.lastSide?.message(
                potDelta = state.potDelta,
                potBefore = state.potBefore
            ) ?: "Tap spin to play"

        else -> "Tap spin to play"
    }


    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                DreidelEffect.ResultSound -> {
                    // play result sound
                    SystemSoundPlayer.playResult()
                }

                DreidelEffect.SpinSound -> {
                    // play spin sound
                    SystemSoundPlayer.playSpin()
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(systemPaddingValues),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {
            Text(
                text = if (ruleSet == DreidelRuleSet.CLASSIC) "Classic" else "Israel",
                fontSize = 16.sp,
                modifier = Modifier
                    .padding(end = 8.dp)
                    .clickable {
                        ruleSet = if (ruleSet == DreidelRuleSet.CLASSIC) DreidelRuleSet.ISRAEL
                        else DreidelRuleSet.CLASSIC
                    }
            )
            Switch(
                checked = ruleSet == DreidelRuleSet.ISRAEL,
                onCheckedChange = {
                    ruleSet = if (it) DreidelRuleSet.ISRAEL else DreidelRuleSet.CLASSIC
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Dreidel Game", fontSize = 28.sp)
        Spacer(Modifier.height(height = 16.dp))

        AnimatedPot(
            pot = state.pot,
            potDelta = state.potDelta
        )
        // Text("Pot: ${state.pot}", fontSize = 20.sp)

        AnimatedDreidel(side = state.lastSide, isSpinning = state.isSpinning)

        Spacer(Modifier.height(height = 16.dp))

        Text(
            text = message,
            fontSize = 22.sp
        )

        Spacer(Modifier.height(height = 24.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            Button(
                modifier = Modifier.testTag("reset_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                ),
                enabled = !state.isSpinning,
                onClick = { viewModel.onIntent(intent = DreidelIntent.Reset) },
            ) {
                Text(text = "Reset Game")
            }
            Button(
                modifier = Modifier.testTag("spin_button"),
                onClick = { viewModel.onIntent(intent = DreidelIntent.Spin(ruleSet)) },
                enabled = !state.isSpinning
            ) {
                Text(text = if (state.isSpinning) "Spinning..." else "Spin Dreidel")
            }
        }
    }
}