package com.example.campsiteapp

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextReplacement
import com.example.campsiteapp.ui.theme.CampsiteAppTheme
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.Date

/**
 * Compose UI tests for [EditLotteryScreen].
 * Goes in src/androidTest. The repository is mocked, so no Firebase is touched.
 */
class EditLotteryScreenTest {

    @get:Rule
    val rule = createComposeRule()

    private val repo = mockk<LotteryRepository>(relaxed = true)
    private lateinit var vm: EditLotteryViewModel
    private val campsites = listOf(Campsite(id = "c1", name = "Lakeside", ownerId = "owner1"))
    private val day = 24 * 60 * 60 * 1000L
    private val base = (System.currentTimeMillis() / day + 1) * day

    @Before
    fun setUp() {
        every { repo.findLottery("l1") } returns Lottery(
            id = "l1",
            ownerId = "owner1",
            campsiteIds = mutableListOf("c1"),
            registrationStart = Date(base),
            registrationEnd = Date(base + day),
            bookingStart = Date(base + 3 * day),
            bookingEnd = Date(base + 4 * day),
            lotteryStart = Date(base + 6 * day),
            lotteryEnd = Date(base + 7 * day),
            numWinners = 3,
            waitlistCapacity = 10
        )
        vm = EditLotteryViewModel(repo, "l1")
    }

    private fun showScreen(onBack: () -> Unit = {}) {
        rule.setContent {
            CampsiteAppTheme {
                EditLotteryScreen(
                    onBack = onBack,
                    onSaved = {},
                    campsites = campsites,
                    vm = vm
                )
            }
        }
    }

    @Test
    fun screen_showsPrefilledValues() {
        showScreen()
        rule.onNodeWithTag("winnersInput").assertTextContains("3")
        rule.onNodeWithTag("capacityInput").assertTextContains("10")
        rule.onNodeWithText("Lakeside").assertIsDisplayed()
    }

    @Test
    fun invalidWinners_showsHint() {
        showScreen()
        rule.onNodeWithTag("winnersInput").performTextReplacement("0")
        rule.onNodeWithText("Enter a whole number of 1 or more").assertExists()
    }

    @Test
    fun removingACampsite_updatesTheList() {
        showScreen()
        rule.onNodeWithText("No campsites added yet").assertDoesNotExist()

        rule.onNodeWithText("Remove").performClick()
        rule.onNodeWithText("No campsites added yet").assertExists()
    }

    @Test
    fun saveButton_sendsPrefilledValuesToRepo() {
        showScreen()
        rule.onNodeWithTag("saveButton").performScrollTo().performClick()
        verify {
            repo.updateLottery(
                lotteryId = "l1",
                campsiteIds = listOf("c1"),
                regStart = any(), regEnd = any(),
                bookStart = any(), bookEnd = any(),
                start = any(), end = any(),
                numWinners = 3,
                waitlistCapacity = 10,
                onDone = any()
            )
        }
    }

    @Test
    fun backButton_callsOnBack() {
        var backCalls = 0
        showScreen(onBack = { backCalls++ })
        rule.onNodeWithTag("backButton").performClick()
        assertEquals(1, backCalls)
    }
}