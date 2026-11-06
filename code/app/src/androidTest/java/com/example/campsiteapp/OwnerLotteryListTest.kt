package com.example.campsiteapp

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OwnerLotteryListTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun test_empty_waitlist_shows_message() {
        composeTestRule.setContent {
            OwnerLotteryListScreen(
                entrants = emptyList(),
                capacity = 10,
                isDrawComplete = false,
                onBack = {}
            )
        }

        composeTestRule.onNodeWithTag("emptyMessage").assertExists()
        composeTestRule.onNodeWithText("Total Entrants: 0 / 10").assertExists()
    }

    @Test
    fun test_list_shows_names_dates_and_capacity() {
        val dummyEntrants = listOf(
            WaitlistEntry("Alice", "01-23-2026"),
            WaitlistEntry("Bob", "12-01-2020")
        )

        composeTestRule.setContent {
            OwnerLotteryListScreen(
                entrants = dummyEntrants,
                capacity = null,
                isDrawComplete = false,
                onBack = {}
            )
        }

        composeTestRule.onNodeWithText("Total Entrants: 2").assertExists()

        // Verify Alice
        composeTestRule.onNodeWithText("Alice").assertExists()
        composeTestRule.onNodeWithText("Joined: 01-23-2026").assertExists()

        // Verify Bob
        composeTestRule.onNodeWithText("Bob").assertExists()
        composeTestRule.onNodeWithText("Joined: 12-01-2020").assertExists()
    }

    @Test
    fun test_draw_complete_shows_won_and_not_selected() {
        val drawnEntrants = listOf(
            WaitlistEntry("Alice", "01-23-2026", status = "Won"),
            WaitlistEntry("Bob", "12-01-2020", status = "Not selected")
        )

        composeTestRule.setContent {
            OwnerLotteryListScreen(
                entrants = drawnEntrants,
                capacity = 5,
                isDrawComplete = true,
                onBack = {}
            )
        }

        composeTestRule.onNodeWithText("Won").assertExists()
        composeTestRule.onNodeWithText("Not selected").assertExists()
    }

    @Test
    fun test_back_button() {
        var backClicked = false
        composeTestRule.setContent {
            OwnerLotteryListScreen(
                entrants = emptyList(),
                capacity = null,
                isDrawComplete = false,
                onBack = { backClicked = true }
            )
        }

        composeTestRule.onNodeWithTag("backButton").performClick()
        assertTrue(backClicked)
    }

    @Test
    fun test_auto_drawn_lottery_shows_won_and_not_selected() {
        val lottery = Lottery(id = "l1", registrationEnd = java.util.Date(0L))
        lottery.waitlist.add("Alice")
        lottery.waitlist.add("Bob")
        lottery.drawAutomatically(java.util.Date())
        val entrants = lottery.waitlist.map { WaitlistEntry(it, "-", lottery.entrantStatus(it)) }

        composeTestRule.setContent {
            OwnerLotteryListScreen(
                entrants = entrants,
                capacity = null,
                isDrawComplete = lottery.drawn,
                onBack = {}
            )
        }

        composeTestRule.onNodeWithText("Won").assertExists()
        composeTestRule.onNodeWithText("Not selected").assertExists()
    }

    @Test
    fun test_lottery_not_drawn_yet_shows_no_results() {
        val lottery = Lottery(id = "l1", registrationEnd = java.util.Date(Long.MAX_VALUE))
        lottery.waitlist.add("Alice")
        lottery.drawAutomatically(java.util.Date())
        val entrants = lottery.waitlist.map { WaitlistEntry(it, "-", lottery.entrantStatus(it)) }

        composeTestRule.setContent {
            OwnerLotteryListScreen(
                entrants = entrants,
                capacity = null,
                isDrawComplete = lottery.drawn,
                onBack = {}
            )
        }

        composeTestRule.onNodeWithText("Alice").assertExists()
        composeTestRule.onNodeWithText("Won").assertDoesNotExist()
        composeTestRule.onNodeWithText("Not selected").assertDoesNotExist()
    }
}
