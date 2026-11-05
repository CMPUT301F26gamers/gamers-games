package com.example.campsiteapp

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Calendar
import java.util.Date
import java.util.TimeZone

class EditLotteryViewModelTest {

    private val repo = mockk<LotteryRepository>(relaxed = true)
    private lateinit var vm: EditLotteryViewModel
    private val day = 24 * 60 * 60 * 1000L
    private val base = (System.currentTimeMillis() / day + 1) * day

    /** A real lottery so the ViewModel has something to prefill from. */
    private fun existingLottery() = Lottery(
        id = "l1",
        ownerId = "owner1",
        campsiteIds = mutableListOf("c1", "c2"),
        registrationStart = Date(base),
        registrationEnd = Date(base + day),
        bookingStart = Date(base + 3 * day),
        bookingEnd = Date(base + 4 * day),
        lotteryStart = Date(base + 6 * day),
        lotteryEnd = Date(base + 7 * day),
        numWinners = 3,
        waitlistCapacity = 10
    )

    @Before
    fun setUp() {
        every { repo.findLottery("l1") } returns existingLottery()
        vm = EditLotteryViewModel(repo, "l1")
    }

    /** UTC midnight of the given date, like the Material date picker returns. */
    private fun pickerDate(year: Int, month: Int, dayOfMonth: Int): Date {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        cal.clear()
        cal.set(year, month, dayOfMonth, 0, 0, 0)
        return cal.time
    }

    @Test
    fun init_prefillsFormFromLottery() {
        assertEquals(listOf("c1", "c2"), vm.getSelectedCampsiteIds())
        assertEquals("3", vm.getWinnersText())
        assertEquals("10", vm.getCapacityText())
        assertEquals(Date(base), vm.getRegistrationStart())
        assertEquals(Date(base + 7 * day), vm.getStayEnd())
        assertNull(vm.getError())
    }

    @Test
    fun setRegistrationPeriod_clearsBookingAndStay() {
        vm.setRegistrationPeriod(Date(base), Date(base + day))
        assertNull(vm.getBookingStart())
        assertNull(vm.getBookingEnd())
        assertNull(vm.getStayStart())
        assertNull(vm.getStayEnd())
    }

    @Test
    fun setBookingPeriod_clearsStayButKeepsRegistration() {
        vm.setBookingPeriod(Date(base + 3 * day), Date(base + 4 * day))
        assertNull(vm.getStayStart())
        assertEquals(Date(base), vm.getRegistrationStart())
    }

    @Test
    fun setStayPeriod_convertsPickerDateToLocalDayBounds() {
        vm.setStayPeriod(pickerDate(2030, Calendar.JUNE, 10), pickerDate(2030, Calendar.JUNE, 12))

        val start = Calendar.getInstance().apply { time = vm.getStayStart()!! }
        assertEquals(10, start.get(Calendar.DAY_OF_MONTH))
        assertEquals(0, start.get(Calendar.HOUR_OF_DAY))

        val end = Calendar.getInstance().apply { time = vm.getStayEnd()!! }
        assertEquals(12, end.get(Calendar.DAY_OF_MONTH))
        assertEquals(23, end.get(Calendar.HOUR_OF_DAY))
    }

    @Test
    fun save_nonNumericWinners_showsErrorAndSkipsRepo() {
        vm.onWinnersChange("abc")
        vm.save()
        assertEquals("Number of winners must be a whole number of 1 or more", vm.getError())
        assertFalse(vm.isSaving())
        verify(exactly = 0) { repo.updateLottery(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun save_clearedPeriod_showsSelectPeriodsError() {
        vm.setRegistrationPeriod(Date(base), Date(base + day))   // clears booking + stay
        vm.save()
        assertEquals("Select registration, booking and stay periods", vm.getError())
    }

    @Test
    fun save_success_setsSavedFlag() {
        val done = slot<(Boolean) -> Unit>()
        every {
            repo.updateLottery(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), capture(done))
        } returns Unit

        vm.save()
        assertTrue(vm.isSaving())
        done.captured(true)

        assertFalse(vm.isSaving())
        assertTrue(vm.isSaved())
        vm.onSavedHandled()
        assertFalse(vm.isSaved())
    }

    @Test
    fun save_failure_showsCouldNotSave() {
        val done = slot<(Boolean) -> Unit>()
        every {
            repo.updateLottery(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), capture(done))
        } returns Unit

        vm.save()
        done.captured(false)

        assertEquals("Could not save lottery", vm.getError())
        assertFalse(vm.isSaved())
    }
}