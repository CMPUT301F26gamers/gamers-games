package com.example.campsiteapp

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.compose.ui.Modifier
import java.util.Date
import java.util.UUID

@Composable
fun CampSiteApp(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val profilesRepository = remember { ProfileRepository() }
    val campsiteRepo = remember { CampsiteRepository() }
    val lotteryRepo = remember { LotteryRepository() }

    NavHost(navController, startDestination = "welcome") {
        composable("welcome") { WelcomeScreen(navController) }
        composable("dashboard") { DashboardScreen(navController) }
        composable("browseProfiles") {
            BrowseProfilesScreen(
                modifier = modifier,
                navController = navController,
                profiles = profilesRepository.profiles,
                onDeleteProfile = { profile ->
                    profilesRepository.cascadeDeleteProfile(profile, campsiteRepo, lotteryRepo)
                }
            )
        }
        composable("profile") {
            val profile = AppUser.profile
            if (profile != null) {
                ProfileScreen(
                    name = profile.name,
                    email = profile.email,
                    phone = profile.phoneNumber,
                    // This photo implementation was provided via Codex (2026-11-05):
                    // "Provide implementation for profile viewing and saving a profile picture using BitMap, LocalContext, and the Gallery"
                    profilePicture = profile.profilePicture,
                    onSavePicture = { picture, onComplete ->
                        profilesRepository.saveProfilePicture(profile, picture, onComplete)
                    },
                    onSave = { name, email, phone ->
                        profile.updateProfileInformation(
                            name = name,
                            email = email,
                            phoneNumber = phone
                        )
                        // This photo implementation was provided via Codex (2026-11-05):
                        // "Provide implementation for profile viewing and saving a profile picture using BitMap, LocalContext, and the Gallery"
                        profilesRepository.saveContactInformation(profile)
                    },
                    onBack = { navController.popBackStack() },
                    onDelete = {
                        profilesRepository.deleteProfile(profile)
                        navController.navigate("welcome") {
                            popUpTo(navController.graph.id) {
                                inclusive = true
                            }
                        }
                    },
                    onViewPolicy = { navController.navigate("policy") }
                )
            }
        }
        composable("createLottery") {
            val profile = AppUser.profile
            if (profile != null) {
                LaunchedEffect(Unit) {
                    campsiteRepo.addCampsite(
                        Campsite(id = "1", name = "Camp A", ownerId = profile.uid)
                    )
                    campsiteRepo.addCampsite(
                        Campsite(id = "2", name = "Camp B", ownerId = profile.uid)
                    )
                    campsiteRepo.addCampsite(
                        Campsite(id = "3", name = "Camp C", ownerId = profile.uid)
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
        composable("ownerLotteries") {
            val profile = AppUser.profile
            if (profile != null) {
                LotteryListScreen(
                    profile = profile,
                    lotteryRepo = lotteryRepo,
                    campsiteRepo = campsiteRepo,
                    onEdit = { id -> navController.navigate("editLottery/$id") },
                    onBack = { navController.popBackStack() },
                )
            }
        }
        composable("editLottery/{lotteryId}") { entry ->
            val id = entry.arguments?.getString("lotteryId") ?: ""
            val profile = AppUser.profile
            if (profile != null) {
                val vm = remember(id) { EditLotteryViewModel(lotteryRepo, id) }
                EditLotteryScreen(
                    onBack = { navController.popBackStack() },
                    onSaved = { navController.popBackStack() },
                    campsites = campsiteRepo.getCampsitesByOwner(profile.uid),
                    vm = vm
                )
            }
        }
        composable("campsites") {
            val profile = AppUser.profile
            if (profile != null) {
                CampsiteScreen(
                    repository = campsiteRepo,
                    onAddCampsite = { campsiteRepo.addCampsite(it) },
                    onRemoveCampsite = { campsiteRepo.removeCampsite(it) },
                    onBack = { navController.popBackStack() },
                    isAdmin = profile.hasRole(Roles.ADMIN),
                    navController = navController
                )
            }
        }

        composable("policy") {
            Policy_GuidelinesScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable("ownerCampsites") {
            val profile = AppUser.profile
            if (profile != null) {
                OwnerCampsitesScreen(
                    campsites = campsiteRepo.getCampsitesByOwner(profile.uid),
                    onBack = { navController.popBackStack() },
                    onCampsiteClick = { campsite ->
                        // Navigate to details (you will need to make a route for this)
                        // For now, just print or pop back:
                        navController.navigate("campsiteDetails/${campsite.id}")
                    }
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
                                occupancy = maxGuests,
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
                    entrants = if (lottery != null) lotteryRepo.waitlistEntries(
                        lottery,
                        names
                    ) else listOf(),
                    capacity = lottery?.waitlistCapacity,
                    isDrawComplete = lottery?.drawn == true,
                    onBack = { navController.popBackStack() }
                )
            }
        }
        composable("campsiteDetails/{campsiteID}") { backStackEntry ->
            val campsiteID = backStackEntry.arguments?.getString("campsiteID")
            println(campsiteID)
            val campsite = campsiteRepo.get(campsiteID)
            val profile = AppUser.profile
            if (profile != null && campsite != null) {
                CampsiteDetailsScreen(
                    campsite = campsite,
                    profile = profile,
                    lotteries = lotteryRepo.getLotteries(campsite.id),
                    joinStateOf = { lotteryRepo.joinState(it, profile.uid, Date()) },
                    onJoin = { lottery, done -> lotteryRepo.enter(lottery, profile.uid, Date(), done) },
                    onLeave = { lottery, done -> lotteryRepo.leave(lottery, profile.uid, done) },
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
