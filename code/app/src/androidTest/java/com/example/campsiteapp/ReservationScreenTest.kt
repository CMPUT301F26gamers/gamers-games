package com.example.campsiteapp

import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.campsiteapp.ui.theme.CampsiteAppTheme
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.util.Date

private const val DAY = 24 * 60 * 60 * 1000L

/** Booking open now, stay 4-5 days from now */
private fun openLottery(winners: List<String>, campsiteIds: List<String> = listOf("c1")) = Lottery(
    id = "L1", ownerId = "owner", campsiteIds = campsiteIds.toMutableList(),
    bookingStart = Date(System.currentTimeMillis() - DAY),
    bookingEnd = Date(System.currentTimeMillis() + 2 * DAY),
    lotteryStart = Date(System.currentTimeMillis() + 4 * DAY),
    lotteryEnd = Date(System.currentTimeMillis() + 5 * DAY),
    winners = winners.toMutableList()
)

/** Compose UI tests for [PendingReservationsScreen]. Goes in src/androidTest. */
class PendingReservationsScreenTest {

    @get:Rule
    val rule = createComposeRule()

    private val lotteryRepo = LotteryRepository(mockk<CollectionReference>(relaxed = true))
    private val campsiteRepo = mockk<CampsiteRepository>()

    private fun showScreen(onPick: (String, String) -> Unit = { _, _ -> }) {
        every { campsiteRepo.get("c1") } returns Campsite(id = "c1", name = "Lakeside")
        every { campsiteRepo.get("c2") } returns Campsite(id = "c2", name = "Hilltop")
        rule.setContent {
            CampsiteAppTheme {
                PendingReservationsScreen("u1", lotteryRepo, campsiteRepo, onPick, onBack = {})
            }
        }
    }

    @Test
    fun noWins_showsEmptyMessage() {
        lotteryRepo.lotteries.add(openLottery(winners = listOf("someoneElse")))
        showScreen()
        rule.onNodeWithTag("emptyPending").assertExists()
        rule.onNodeWithTag("pendingRow_c1").assertDoesNotExist()
    }

    @Test
    fun wonLottery_listsEveryCampsite_andTappingOpensTheOneTapped() {
        lotteryRepo.lotteries.add(openLottery(winners = listOf("u1"), campsiteIds = listOf("c1", "c2")))
        var picked: Pair<String, String>? = null
        showScreen(onPick = { lottery, campsite -> picked = lottery to campsite })

        rule.onNodeWithText("Lakeside").assertExists()
        rule.onNodeWithText("Hilltop").assertExists()
        rule.onNodeWithTag("pendingRow_c2").performClick()

        assertEquals("L1" to "c2", picked)
    }
}

/**
 * Compose UI tests for [ReservationScreen]. Goes in src/androidTest.
 * The date picker itself isn't tapped, so picking a range and confirming
 * is covered by the logic tests and by manual testing.
 */
class ReservationScreenTest {

    @get:Rule
    val rule = createComposeRule()

    private val reservationRepo = ReservationRepository(mockk<FirebaseFirestore>(relaxed = true))
    private val lotteryRepo = mockk<LotteryRepository>(relaxed = true)

    private fun showScreen(lottery: Lottery, onFinished: () -> Unit = {}) {
        rule.setContent {
            CampsiteAppTheme {
                ReservationScreen(
                    lottery, Campsite(id = "c1", name = "Lakeside"), "u1",
                    reservationRepo, lotteryRepo, onFinished, onBack = {}
                )
            }
        }
    }

    @Test
    fun noDatesAvailable_showsMessageInsteadOfCalendar() {
        val l = openLottery(listOf("u1"))
        reservationRepo.reservations.add(
            Reservation("r1", "L1", "c1", "other", l.lotteryStart.toDay(), l.lotteryEnd.toDay())
        )
        showScreen(l)
        rule.onNodeWithTag("noDates").assertExists()
    }

    @Test
    fun confirmIsDisabledUntilARangeIsSelected() {
        showScreen(openLottery(listOf("u1")))
        rule.onNodeWithTag("confirmButton").assertIsNotEnabled()
    }

    @Test
    fun cancellingTheDeclineWarning_doesNotDecline() {
        showScreen(openLottery(listOf("u1")))
        rule.onNodeWithTag("declineButton").performClick()
        rule.onNodeWithText("This cannot be undone", substring = true).assertExists()
        rule.onNodeWithTag("cancelDecline").performClick()
        verify(exactly = 0) { lotteryRepo.decline(any(), any(), any(), any()) }
    }

    @Test
    fun confirmingDecline_callsRepoAndFinishes() {
        every { lotteryRepo.decline("L1", "u1", any(), any()) } answers {
            lastArg<(Boolean) -> Unit>().invoke(true)
        }
        var finished = 0
        showScreen(openLottery(listOf("u1")), onFinished = { finished++ })

        rule.onNodeWithTag("declineButton").performClick()
        rule.onNodeWithTag("confirmDecline").performClick()

        rule.waitForIdle()
        assertEquals(1, finished)
    }
}