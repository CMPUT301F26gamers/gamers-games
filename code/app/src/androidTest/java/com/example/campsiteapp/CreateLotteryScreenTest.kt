package com.example.campsiteapp

import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextReplacement
import com.example.campsiteapp.ui.theme.CampsiteAppTheme
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.Date

/**
 * Compose UI tests for [CreateLotteryScreen].
 * Goes in src/androidTest. The repository is mocked, so no Firebase is touched.
 *
 * The Material date picker dialog itself is not tested here.
 */
class CreateLotteryScreenTest {

    @get:Rule
    val rule = createComposeRule()

    private val repo = mockk<LotteryRepository>(relaxed = true)
    private lateinit var vm: CreateLotteryViewModel
    private val campsites = listOf(Campsite("1", "Camp Melody"), Campsite("2", "Bingus"))
    private val day = 24 * 60 * 60 * 1000L

    @Before
    fun setUp() {
        vm = CreateLotteryViewModel(repo)
    }

    // ---------- helpers ----------

    private fun fillValidForm() {
        val base = (System.currentTimeMillis() / day + 1) * day
        vm.setRegistrationPeriod(Date(base), Date(base + day))
        vm.setBookingPeriod(Date(base + 3 * day), Date(base + 4 * day))
        vm.setStayPeriod(Date(base + 6 * day), Date(base + 7 * day))
    }

    private fun showScreen(
        onBack: () -> Unit = {},
        onCreated: () -> Unit = {}
    ) {
        rule.setContent {
            CampsiteAppTheme {
                CreateLotteryScreen(
                    onBack = onBack,
                    onCreated = onCreated,
                    ownerId = "owner1",
                    campsites = campsites,
                    vm = vm
                )
            }
        }
    }

    // ---------- tests ----------

    @Test
    fun invalidWinners_showsHint() {
        showScreen()
        rule.onNodeWithTag("winnersInput").performTextReplacement("0")
        rule.onNodeWithText("Enter a whole number of 1 or more").assertExists()
    }

    @Test
    fun capacityHint_changesFromNeutralToError() {
        showScreen()
        rule.onNodeWithText("Leave empty for no limit").assertExists()

        rule.onNodeWithTag("capacityInput").performTextReplacement("0")
        rule.onNodeWithText("Enter a whole number of 1 or more, or leave empty").assertExists()
    }

    @Test
    fun addingAndRemovingACampsite_updatesTheList() {
        showScreen()
        rule.onNodeWithText("No campsites added yet").assertExists()

        rule.onNodeWithTag("addCampsiteButton").performClick()
        rule.onNodeWithText("Camp Melody").performClick()
        rule.onNodeWithText("Remove").assertExists()
        rule.onNodeWithText("No campsites added yet").assertDoesNotExist()

        rule.onNodeWithText("Remove").performClick()
        rule.onNodeWithText("No campsites added yet").assertExists()
    }

    @Test
    fun createWithNoDates_showsError() {
        showScreen()
        rule.onNodeWithTag("createButton").performScrollTo().performClick()
        rule.onNodeWithText("Select registration, booking and stay periods").assertExists()
    }

    @Test
    fun backButton_callsOnBack() {
        var backCalls = 0
        showScreen(onBack = { backCalls++ })
        rule.onNodeWithTag("backButton").performClick()
        assertEquals(1, backCalls)
    }

    @Test
    fun savedLottery_callsOnCreatedOnce_andResetsTheFlag() {
        fillValidForm()
        every {
            repo.createLottery(any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any())
        } answers {
            lastArg<(Boolean) -> Unit>().invoke(true)
        }
        vm.createLottery("owner1")  // isCreated is now true before the screen appears

        var createdCalls = 0
        showScreen(onCreated = { createdCalls++ })
        rule.waitForIdle()

        assertEquals(1, createdCalls)
        assertFalse(vm.isCreated())
    }

    @Test
    fun whileSaving_buttonIsDisabled_andShowsCreating() {
        fillValidForm()
        // relaxed mock never calls onDone, so the save stays pending
        vm.createLottery("owner1")

        showScreen()
        rule.onNodeWithText("Creating...").assertExists()
        rule.onNodeWithTag("createButton").assertIsNotEnabled()
    }
}