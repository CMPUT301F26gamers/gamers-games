package com.example.campsiteapp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.campsiteapp.ui.theme.CampBackground
import com.example.campsiteapp.ui.theme.CampGreen

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
            .background(CampBackground)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(CampGreen)
                .height(56.dp)
        ) {
            TextButton(
                onClick = onBack,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .testTag("backButton")
            ) {
                Text(text = "←", color = Color.White, fontSize = 22.sp)
            }

            Text(
                text = "App Policy and Guidelines",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.Center)
                    .testTag("screenTitle")
            )
        }

        // Scrollable text area for the policy
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                // The following text is from Deepseek, "give me an app policy and guidelines to paste in", 2026-11-05
                text = """
                    Please read our guidelines and policies carefully before using the app.
                    
                    --- GUIDELINES ---
                    
                    1. Fairness & Lottery System
                    Our app uses a lottery system to ensure fairness. Joining a waitlist does not guarantee a reservation. Winners are drawn at random after the registration period ends. Do not attempt to create multiple accounts to increase your chances.
                    
                    2. Community Code of Conduct
                    Please treat campsite owners and other entrants with respect. Harassment, hate speech, or inappropriate behavior will result in immediate removal from the app.
                    
                    3. Booking Etiquette
                    If you win a lottery, you have a limited time window to accept and book your reservation. If you decline or do not respond in time, your spot will be forfeited and offered to the next person on the waitlist.
                    
                    --- POLICIES ---
                    
                    4. Cancellations by Owners
                    Campsite owners reserve the right to cancel reservations in the event of emergencies (e.g., extreme weather, forest fires, or maintenance issues). In such cases, entrants will receive a full refund.
                    
                    5. Privacy Policy
                    Your location data is only used to show you campsites near you and to provide map locations for listings. It is never sold to third parties. Your profile information is stored securely to facilitate the lottery and reservation systems.
                    
                    6. Liability
                    This app is a platform connecting users. The app is not responsible for weather conditions, physical injuries, property damage, or any issues incurred during your stay. Please camp responsibly!
                """.trimIndent(),
                fontSize = 16.sp,
                color = Color.White,
                lineHeight = 24.sp
            )
        }
    }
}