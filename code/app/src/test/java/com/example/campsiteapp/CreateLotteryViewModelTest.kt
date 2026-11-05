package com.example.campsiteapp

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Calendar
import java.util.Date
import java.util.TimeZone

class CreateLotteryViewModelTest {

    private val repo = mockk<LotteryRepository>(relaxed = true)
    private lateinit var vm: CreateLotteryViewModel
    private val day = 24 * 60 * 60 * 1000L

    @Before
    fun setUp() {
        vm = CreateLotteryViewModel(repo)
    }

    /** UTC midnight of the given date, like the Material date picker returns. */
    private fun pickerDate(year: Int, month: Int, dayOfMonth: Int): Date {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        cal.clear()
        cal.set(year, month, dayOfMonth, 0, 0, 0)
        return cal.time
    }

    /** Sets all three periods in order (earlier setters clear the later ones). */
    private fun fillValidForm() {
        val base = (System.currentTimeMillis() / day + 1) * day
        vm.setRegistrationPeriod(Date(base), Date(base + day))
        vm.setBookingPeriod(Date(base + 3 * day), Date(base + 4 * day))
        vm.setStayPeriod(Date(base + 6 * day), Date(base + 7 * day))
    }


    @Test
    fun oneDayRange_isMidnightToEndOfDay_andStartIsBeforeEnd() {
        val picked = pickerDate(2099, Calendar.JUNE, 5)
        vm.setRegistrationPeriod(picked, picked)

        val start = Calendar.getInstance().apply { time = vm.getRegistrationStart()!! }
        val end = Calendar.getInstance().apply { time = vm.getRegistrationEnd()!! }

        assertEquals(0, start.get(Calendar.HOUR_OF_DAY))
        assertEquals(0, start.get(Calendar.MINUTE))
        assertEquals(23, end.get(Calendar.HOUR_OF_DAY))
        assertEquals(59, end.get(Calendar.MINUTE))
        assertEquals(59, end.get(Calendar.SECOND))
        assertEquals(999, end.get(Calendar.MILLISECOND))
        assertTrue(start.time.before(end.time))
    }

    @Test
    fun changingAnEarlierPeriod_clearsTheLaterOnes() {
        fillValidForm()
        val d = Date()

        vm.setBookingPeriod(d, d)
        assertNotNull(vm.getRegistrationStart())   // earlier period kept
        assertNotNull(vm.getBookingStart())
        assertNull(vm.getStayStart())              // stay cleared

        fillValidForm()
        vm.setRegistrationPeriod(d, d)
        assertNull(vm.getBookingStart())           // booking and stay cleared
        assertNull(vm.getStayStart())
    }

    @Test
    fun missingDates_showsError_andDoesNotCallRepo() {
        vm.createLottery()
        assertEquals("Select registration, booking and stay periods", vm.getError())
        assertFalse(vm.isSaving())
        verify(exactly = 0) {
            repo.createLottery(any(), any(), any(), any(), any(), any(), any(), any(), any(), any())
        }
    }

    @Test
    fun invalidWinnersText_showsError_andDoesNotCallRepo() {
        fillValidForm()
        vm.onWinnersChange("abc")
        vm.createLottery()
        assertEquals("Number of winners must be a whole number of 1 or more", vm.getError())
        assertFalse(vm.isSaving())
        verify(exactly = 0) {
            repo.createLottery(any(), any(), any(), any(), any(), any(), any(), any(), any(), any())
        }
    }

    @Test
    fun modelMessageIsShownWhenRepoThrows() {
        fillValidForm()
        every {
            repo.createLottery(any(), any(), any(), any(), any(), any(), any(), any(), any(), any())
        } throws IllegalArgumentException("Stay must start after booking ends")
        vm.createLottery()
        assertEquals("Stay must start after booking ends", vm.getError())
        assertFalse(vm.isSaving())
        assertFalse(vm.isCreated())
    }

    @Test
    fun successSetsCreated_andHandledResetsIt() {
        fillValidForm()
        every {
            repo.createLottery(any(), any(), any(), any(), any(), any(), any(), any(), any(), any())
        } answers {
            lastArg<(Boolean) -> Unit>().invoke(true)
        }
        vm.createLottery()
        assertTrue(vm.isCreated())
        assertFalse(vm.isSaving())
        assertNull(vm.getError())

        vm.onCreatedHandled()
        assertFalse(vm.isCreated())
    }

    @Test
    fun saveFailure_showsError_andIsNotCreated() {
        fillValidForm()
        every {
            repo.createLottery(any(), any(), any(), any(), any(), any(), any(), any(), any(), any())
        } answers {
            lastArg<(Boolean) -> Unit>().invoke(false)
        }
        vm.createLottery()
        assertEquals("Could not save lottery", vm.getError())
        assertFalse(vm.isCreated())
        assertFalse(vm.isSaving())
    }
}