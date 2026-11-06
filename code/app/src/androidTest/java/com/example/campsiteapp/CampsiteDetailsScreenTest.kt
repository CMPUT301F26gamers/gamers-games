package com.example.campsiteapp

import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import junit.framework.TestCase.assertEquals
import org.junit.Before
import androidx.compose.ui.test.performScrollTo
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class CampsiteDetailsScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val campsite = Campsite(id = "1", name = "Balls")
    private val profile = Profile(uid = "u1")

    private fun lottery(onWaitlist: Boolean = false) = Lottery(
        id = "L1",
        ownerId = "owner",
        campsiteIds = mutableListOf("1"),
        waitlist = if (onWaitlist) mutableListOf("u1") else mutableListOf()
    )

    private fun show(
        lotteries: List<Lottery>,
        state: JoinState = JoinState.CAN_JOIN,
        onJoin: () -> Unit = {},
        onLeave: () -> Unit = {}
    ) {
        composeTestRule.setContent {
            CampsiteDetailsScreen(
                campsite = campsite,
                profile = profile,
                lotteries = lotteries,
                joinStateOf = { state },
                onJoin = { _, _ -> onJoin() },
                onLeave = { _, _ -> onLeave() },
                onBack = {}
            )
        }
    }

    @Test
    fun rateButton_exists() {
        show(emptyList())
        composeTestRule.onNodeWithTag("rateButton").assertExists()
    }

    @Test
    fun noLotteries_showsNoRows() {
        show(emptyList())
        composeTestRule.onNodeWithTag("lotteryRow_L1").assertDoesNotExist()
    }

    @Test
    fun canJoin_clickCallsOnJoin() {
        var joined = false
        show(listOf(lottery()), JoinState.CAN_JOIN, onJoin = { joined = true })

        composeTestRule.onNodeWithTag("joinButton_L1").performScrollTo().performClick()
        assertTrue(joined)
    }

    @Test
    fun onWaitlist_showsLeaveAndCallsOnLeave() {
        var left = false
        show(listOf(lottery(onWaitlist = true)), JoinState.ON_WAITLIST, onLeave = { left = true })

        composeTestRule.onNodeWithTag("leaveButton_L1").performScrollTo().performClick()
        assertTrue(left)
    }

    @Test
    fun waitlistFull_joinIsDisabled() {
        show(listOf(lottery()), JoinState.WAITLIST_FULL)
        composeTestRule.onNodeWithTag("joinButton_L1").assertIsNotEnabled()
    }

    @Test
    fun notOpen_joinIsDisabled() {
        show(listOf(lottery()), JoinState.NOT_OPEN)
        composeTestRule.onNodeWithTag("joinButton_L1").assertIsNotEnabled()
    }

    @Test
    fun addRating_computesAverageForTwoUsers() {

        val campsite = Campsite()

        campsite.addRating("user1", 4)
        campsite.addRating("user2", 2)

        assertEquals(3.0, campsite.averageRating)
    }

    @Test
    fun addRating_replacesExistingUserRating() {

        val campsite = Campsite()

        campsite.addRating("user1", 5)
        campsite.addRating("user1", 1)

        assertEquals(1, campsite.ratings.size)
        assertEquals(1.0, campsite.averageRating)
    }
}