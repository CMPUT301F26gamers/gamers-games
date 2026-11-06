package com.example.campsiteapp

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import java.util.Calendar
import java.util.Date
import java.util.TimeZone

/**
 * Connects the create lottery screen to the lottery model.
 *
 * Connect with LotteryRepository through [createLottery]
 * Catches exceptions and display them through [getError]
 *
 * The three periods must be set in order (registration, booking, stay).
 * Changing an earlier period clears the later ones, because they may no
 * longer be valid.
 *
 * @property repo the shared repository the new lottery is saved through
 */
class CreateLotteryViewModel(
    private val repo: LotteryRepository
) : ViewModel() {

    // mutableStateOf notifies Compose to update the UI

    private var _selectedCampsiteIds by mutableStateOf<List<String>>(emptyList())
    private var _winnersText by mutableStateOf("1")
    private var _capacityText by mutableStateOf("")
    private var _registrationStart by mutableStateOf<Date?>(null)
    private var _registrationEnd by mutableStateOf<Date?>(null)
    private var _bookingStart by mutableStateOf<Date?>(null)
    private var _bookingEnd by mutableStateOf<Date?>(null)
    private var _stayStart by mutableStateOf<Date?>(null)
    private var _stayEnd by mutableStateOf<Date?>(null)
    private var _error by mutableStateOf<String?>(null)
    private var _isSaving by mutableStateOf(false)
    private var _created by mutableStateOf(false)

    // getters

    fun getSelectedCampsiteIds(): List<String> = _selectedCampsiteIds
    fun getWinnersText(): String = _winnersText
    fun getCapacityText(): String = _capacityText
    fun getRegistrationStart(): Date? = _registrationStart
    fun getRegistrationEnd(): Date? = _registrationEnd
    fun getBookingStart(): Date? = _bookingStart
    fun getBookingEnd(): Date? = _bookingEnd
    fun getStayStart(): Date? = _stayStart
    fun getStayEnd(): Date? = _stayEnd
    fun getError(): String? = _error
    fun isSaving(): Boolean = _isSaving
    fun isCreated(): Boolean = _created

    /**
     * Adds a campsite to the lottery being created.
     * @param id the campsite's ID
     */
    fun addCampsite(id: String) {
        _selectedCampsiteIds = _selectedCampsiteIds + id
        _error = null
    }

    /**
     * Removes a campsite from the lottery being created.
     * @param id the campsite's ID
     */
    fun removeCampsite(id: String) {
        _selectedCampsiteIds = _selectedCampsiteIds - id
        _error = null
    }

    /** Called on every keystroke in the winners field (US 02.04.05). */
    fun onWinnersChange(text: String) {
        _winnersText = text
        _error = null
    }

    /** Called on every keystroke in the capacity field (US 02.04.07). */
    fun onCapacityChange(text: String) {
        _capacityText = text
        _error = null
    }

    /**
     * Sets the registration period (US 02.04.03).
     * Clears booking and stay, since they must come after registration.
     */
    fun setRegistrationPeriod(start: Date, end: Date) {
        _registrationStart = startOfPickedDay(start)
        _registrationEnd = endOfPickedDay(end)
        _bookingStart = null
        _bookingEnd = null
        _stayStart = null
        _stayEnd = null
        _error = null
    }

    /**
     * Sets when winners can book or decline.
     * Clears the stay, since it must come after booking.
     */
    fun setBookingPeriod(start: Date, end: Date) {
        _bookingStart = startOfPickedDay(start)
        _bookingEnd = endOfPickedDay(end)
        _stayStart = null
        _stayEnd = null
        _error = null
    }

    /** Sets the stay period the campsites are listed for (US 02.04.01). */
    fun setStayPeriod(start: Date, end: Date) {
        _stayStart = startOfPickedDay(start)
        _stayEnd = endOfPickedDay(end)
        _error = null
    }

    /**
     * Resets the [isCreated] flag. The screen calls this right after it
     * navigates away, so the navigation only happens once.
     */
    fun onCreatedHandled() {
        _created = false
    }

    /**
     * Validates the form and saves a new lottery
     */
    fun createLottery(ownerId: String) {
        // Winners: must at least be a number, the model checks the range
        val winners = _winnersText.toIntOrNull()
        if (winners == null) {
            fail("Number of winners must be a whole number of 1 or more")
            return
        }

        // Capacity: empty means no limit (null), otherwise it must be a number
        val capacity: Int? = if (_capacityText.isBlank()) {
            null
        } else {
            val parsed = _capacityText.toIntOrNull()
            if (parsed == null) {
                fail("Capacity must be a whole number, or empty for no limit")
                return
            }
            parsed
        }

        // Copy to local vals so the null checks smart cast below
        val regStart = _registrationStart
        val regEnd = _registrationEnd
        val bookStart = _bookingStart
        val bookEnd = _bookingEnd
        val stayStart = _stayStart
        val stayEnd = _stayEnd
        if (regStart == null || regEnd == null || bookStart == null ||
            bookEnd == null || stayStart == null || stayEnd == null
        ) {
            fail("Select registration, booking and stay periods")
            return
        }

        _isSaving = true
        _error = null
        try {
            repo.createLottery(
                ownerId = ownerId,
                campsiteIds = _selectedCampsiteIds,
                regStart = regStart,
                regEnd = regEnd,
                bookStart = bookStart, bookEnd = bookEnd,
                start = stayStart, end = stayEnd,
                numWinners = winners,
                waitlistCapacity = capacity
            ) { ok ->
                _isSaving = false
                if (ok) _created = true else _error = "Could not save lottery"
            }
        } catch (e: IllegalArgumentException) {
            fail(e.message ?: "Invalid input")
        }
    }

    /** Shows [message] and stops the saving state. */
    private fun fail(message: String) {
        _error = message
        _isSaving = false
    }
}