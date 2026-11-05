package com.example.campsiteapp

import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.compose.ui.Modifier

@Composable
fun CampSiteApp(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val profilesRepository = ProfileRepository()

    NavHost(navController, startDestination = "welcome") {
        composable("welcome") { WelcomeScreen( navController) }
        composable("dashboard") { DashboardScreen(navController) }
        composable("browseProfiles") {
            BrowseProfilesScreen(
                modifier = modifier,
                navController = navController,
                profiles = profilesRepository.profiles,
                onDeleteProfile = { profile -> profilesRepository.removeProfile(profile) }
            )
        }
        composable("profile") {
            val profile = AppUser.profile
            if (profile != null) {
                ProfileScreen(
                    name = profile.name,
                    email = profile.email,
                    phone = profile.phoneNumber,
                    onSave = { name, email, phone ->
                        profile.updateProfileInformation(name = name, email = email, phoneNumber = phone)
                        profilesRepository.addProfile(profile)
                    },
                    onBack = { navController.popBackStack() },
                    onDelete = {
                        profilesRepository.deleteProfile(profile)
                        navController.navigate("welcome") {
                            popUpTo(navController.graph.id) {
                                inclusive = true
                            }
                        }
                    }
                )
            }
        }
    }
}
