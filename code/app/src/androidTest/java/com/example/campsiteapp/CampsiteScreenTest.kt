package com.example.campsiteapp

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class CampsiteScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    fun setUp() {
        val campsiteRepo = CampsiteRepositoryStud()
        composeTestRule.setContent {
            CampsiteScreen(
                campsites = campsiteRepo.campsites,
                onAddCampsite = { campsiteRepo.addCampsite(it) },
                onRemoveCampsite = { campsiteRepo.removeCampsite(it) },
                onBack = {}
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
}

class CampsiteRepositoryStud {
    private val _campsites = mutableStateListOf<Campsite>()
    val campsites: List<Campsite>
        get() = _campsites

    fun addCampsite(campsite: Campsite) {
        _campsites.add(campsite)
    }

    fun removeCampsite(campsite: Campsite) {
        _campsites.remove(campsite)
    }
}