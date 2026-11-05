package com.example.campsiteapp

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Screen displaying the application policy and guidelines (US 01.11.01).
 *
 * @param onBack Callback triggered when the back button is pressed.
 * This should be replaced with navController later on.
 */
@Composable
fun Policy_GuidelinesScreen(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Back Button
        Button(
            onClick = onBack,
            modifier = Modifier.testTag("backButton")
        ) {
            Text("Back")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "App Policy and Guidelines",
            fontSize = 26.sp,
            modifier = Modifier.testTag("screenTitle")
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Scrollable text area for the policy
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                /** Not really sure what to put here, and do you guys think the guide and policy should be a separate ? */
                text = "RANDOM TEXT GO",
                fontSize = 16.sp
            )
        }
    }
}