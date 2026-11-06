package com.example.campsiteapp

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.compose.ui.Modifier
import java.util.UUID

@Composable
fun CampSiteApp(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val profilesRepository = remember { ProfileRepository() }
    val campsiteRepo = remember { CampsiteRepository() }
    val lotteryRepo = remember { LotteryRepository() }

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
        composable("createLottery") {
            val profile = AppUser.profile
            if (profile != null) {
                LaunchedEffect(Unit) {
                    campsiteRepo.addCampsite(
                        Campsite(id = "test1", name = "Test Site", ownerId = profile.uid)
                    )
                }
                val vm = remember { CreateLotteryViewModel(lotteryRepo) }
                CreateLotteryScreen(
                    onBack = { navController.popBackStack() },
                    onCreated = { navController.popBackStack() },
                    ownerId = profile.uid,
                    campsites = campsiteRepo.getCampsitesByOwner(profile.uid),
                    vm = vm
                )
            }
        }
        composable("registerCampsite") {
            val profile = AppUser.profile
            if (profile != null) {
                RegisterCampsiteScreen(
                    onRegister = { name, location, maxGuests, description, tags ->
                        campsiteRepo.addCampsite(
                            Campsite(
                                id = UUID.randomUUID().toString(),
                                name = name,
                                location = location,
                                numGuests = maxGuests,
                                ownerId = profile.uid,
                                description = description,
                                tags = tags
                            )
                        )
                        navController.popBackStack()
                    },
                    onBack = { navController.popBackStack() }
                )
            }
        }
        composable("ownerWaitlist") {
            val profile = AppUser.profile
            if (profile != null) {
                val lottery = lotteryRepo.getLotteriesByOwner(profile.uid).lastOrNull()
                val names = profilesRepository.profiles.associate { it.uid to it.name }
                OwnerLotteryListScreen(
                    entrants = if (lottery != null) lotteryRepo.waitlistEntries(lottery, names) else listOf(),
                    capacity = lottery?.waitlistCapacity,
                    isDrawComplete = lottery?.drawn == true,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
