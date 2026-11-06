package com.example.campsiteapp

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.Date

class CampsiteScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    fun setUp() {
        val campsiteRepo = CampsiteRepositoryStud()
        composeTestRule.setContent {
            CampsiteScreen(
                repository = campsiteRepo,
                onAddCampsite = { campsiteRepo.addCampsite(it) },
                onRemoveCampsite = { campsiteRepo.removeCampsite(it) },
                onBack = {},
                isAdmin = true,
                navController = null
            )
        }
    }

    @Test
    fun test_add_and_remove() {
        composeTestRule
            .onNodeWithTag("addFieldButton")
            .assertExists()

        composeTestRule
            .onNodeWithTag("addFieldButton")
            .performClick()

        composeTestRule
            .onNodeWithTag("nameInput")
            .assertExists()

        composeTestRule
            .onNodeWithTag("idInput")
            .assertExists()

        composeTestRule
            .onNodeWithTag("nameInput")
            .performTextInput("Camp Melody")

        composeTestRule
            .onNodeWithTag("idInput")
            .performTextInput("1")

        composeTestRule
            .onNodeWithTag("addCampsiteButton")
            .assertExists()

        composeTestRule
            .onNodeWithTag("addCampsiteButton")
            .performClick()

        composeTestRule
            .onNodeWithText("Camp Melody")
            .assertExists()

        composeTestRule
            .onNodeWithText("Camp Melody")
            .performClick()

        composeTestRule
            .onNodeWithText("Camp Melody")
            .assertExists()

        composeTestRule
            .onNodeWithTag("addFieldButton")
            .performClick()

        composeTestRule
            .onNodeWithTag("nameInput")
            .performTextInput("Balls")

        composeTestRule
            .onNodeWithTag("idInput")
            .performTextInput("2")

        composeTestRule
            .onNodeWithTag("addCampsiteButton")
            .performClick()

        composeTestRule
            .onNodeWithText("Balls")
            .assertExists()

        composeTestRule
            .onNodeWithText("Balls")
            .performClick()

        composeTestRule
            .onNodeWithText("Balls")
            .assertExists()

        composeTestRule
            .onNodeWithTag("removeFieldButton")
            .assertExists()

        composeTestRule
            .onNodeWithTag("removeFieldButton")
            .performClick()

        composeTestRule
            .onNodeWithText("Camp Melody")
            .performClick()

        composeTestRule
            .onNodeWithText("Camp Melody")
            .assertDoesNotExist()

        composeTestRule
            .onNodeWithText("Balls")
            .assertExists()

        composeTestRule
            .onNodeWithTag("removeFieldButton")
            .performClick()

        composeTestRule
            .onNodeWithText("Balls")
            .performClick()

        composeTestRule
            .onNodeWithText("Balls")
            .assertDoesNotExist()
    }

    @Test
    fun search_test() {
        composeTestRule
            .onNodeWithTag("addFieldButton")
            .performClick()

        composeTestRule
            .onNodeWithTag("nameInput")
            .performTextInput("Camp Melody")

        composeTestRule
            .onNodeWithTag("idInput")
            .performTextInput("1")

        composeTestRule
            .onNodeWithTag("addCampsiteButton")
            .performClick()

        composeTestRule
            .onNodeWithTag("addFieldButton")
            .performClick()

        composeTestRule
            .onNodeWithTag("nameInput")
            .performTextInput("Balls")

        composeTestRule
            .onNodeWithTag("idInput")
            .performTextInput("2")

        composeTestRule
            .onNodeWithTag("addCampsiteButton")
            .performClick()

        composeTestRule
            .onNodeWithTag("addFieldButton")
            .performClick()

        composeTestRule
            .onNodeWithTag("nameInput")
            .performTextInput("Bogos")

        composeTestRule
            .onNodeWithTag("idInput")
            .performTextInput("3")

        composeTestRule
            .onNodeWithTag("addCampsiteButton")
            .performClick()

        composeTestRule
            .onNodeWithTag("searchBar")
            .performTextInput("B")

        composeTestRule
            .onNodeWithText("Camp Melody")
            .assertDoesNotExist()

        composeTestRule
            .onNodeWithText("Balls")
            .assertExists()

        composeTestRule
            .onNodeWithText("Bogos")
            .assertExists()

        composeTestRule
            .onNodeWithTag("searchBar")
            .performTextClearance()

        composeTestRule
            .onNodeWithText("Camp Melody")
            .assertExists()

        composeTestRule
            .onNodeWithTag("removeFieldButton")
            .performClick()

        composeTestRule
            .onNodeWithText("Camp Melody")
            .performClick()

        composeTestRule
            .onNodeWithTag("removeFieldButton")
            .performClick()

        composeTestRule
            .onNodeWithText("Balls")
            .performClick()

        composeTestRule
            .onNodeWithTag("removeFieldButton")
            .performClick()

        composeTestRule
            .onNodeWithText("Bogos")
            .performClick()
    }
}

class CampsiteRepositoryStud : CampsiteRepo {
    private val _campsites = mutableStateListOf<Campsite>()

    override var searchQuery: String by mutableStateOf("")
    override var minGuests: Int by mutableIntStateOf(0)
    override var selectedLocation: String by mutableStateOf("")
    override var selectedDate: Date by mutableStateOf(Date(0L))

    override val campsites: List<Campsite>
        get() = _campsites.filter { campsite ->
            // Match Search Query (if provided)
            val matchesQuery = searchQuery.isBlank() ||
                    campsite.name.contains(searchQuery, ignoreCase = true)

            // Match Min Guests (if provided / > 0)
            val matchesGuests = minGuests <= 0 ||
                    campsite.numGuests >= minGuests

            // Match Location (if provided)
            val matchesLocation = selectedLocation.isBlank() ||
                    campsite.location.equals(selectedLocation, ignoreCase = true)

//            val matchesDate = selectedDate == NO_DATE_SELECTED || isSameDay(campsite.date, selectedDate)

            matchesQuery && matchesGuests && matchesLocation
        }

    override fun addCampsite(campsite: Campsite) {
        _campsites.add(campsite)
    }

    override fun removeCampsite(campsite: Campsite) {
        _campsites.remove(campsite)
    }

    override fun getCampsitesByOwner(ownerId: String): List<Campsite> {
        return _campsites.filter { it.ownerId == ownerId }
    }
}