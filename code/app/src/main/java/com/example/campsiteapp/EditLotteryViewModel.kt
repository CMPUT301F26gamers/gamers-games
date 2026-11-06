package com.example.campsiteapp

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import java.util.Calendar
import java.util.Date
import java.util.TimeZone

/**
 * state for editing an existing lottery
 * prefilled from the lottery and saves through [LotteryRepository.updateLottery]
 */
class EditLotteryViewModel(
    private val repo: LotteryRepository,
    private val lotteryId: String
) : ViewModel() {

    private val lottery = repo.findLottery(lotteryId)

    private var _selectedCampsiteIds by mutableStateOf(lottery?.campsiteIds?.toList() ?: emptyList())
    private var _winnersText by mutableStateOf(lottery?.numWinners?.toString() ?: "1")
    private var _capacityText by mutableStateOf(lottery?.waitlistCapacity?.toString() ?: "")
    private var _registrationStart by mutableStateOf(lottery?.registrationStart)
    private var _registrationEnd by mutableStateOf(lottery?.registrationEnd)
    private var _bookingStart by mutableStateOf(lottery?.bookingStart)
    private var _bookingEnd by mutableStateOf(lottery?.bookingEnd)
    private var _stayStart by mutableStateOf(lottery?.lotteryStart)
    private var _stayEnd by mutableStateOf(lottery?.lotteryEnd)
    private var _error by mutableStateOf(if (lottery == null) "Lottery not found" else null)
    private var _isSaving by mutableStateOf(false)
    private var _saved by mutableStateOf(false)

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
    fun isSaved(): Boolean = _saved

    fun addCampsite(id: String) { _selectedCampsiteIds = _selectedCampsiteIds + id; _error = null }
    fun removeCampsite(id: String) { _selectedCampsiteIds = _selectedCampsiteIds - id; _error = null }
    fun onWinnersChange(text: String) { _winnersText = text; _error = null }
    fun onCapacityChange(text: String) { _capacityText = text; _error = null }

    fun setRegistrationPeriod(start: Date, end: Date) {
        _registrationStart = startOfPickedDay(start)
        _registrationEnd = endOfPickedDay(end)
        _bookingStart = null; _bookingEnd = null
        _stayStart = null; _stayEnd = null
        _error = null
    }

    fun setBookingPeriod(start: Date, end: Date) {
        _bookingStart = startOfPickedDay(start)
        _bookingEnd = endOfPickedDay(end)
        _stayStart = null; _stayEnd = null
        _error = null
    }

    fun setStayPeriod(start: Date, end: Date) {
        _stayStart = startOfPickedDay(start)
        _stayEnd = endOfPickedDay(end)
        _error = null
    }

    fun onSavedHandled() { _saved = false }

    fun save() {
        val winners = _winnersText.toIntOrNull()
        if (winners == null) {
            fail("Number of winners must be a whole number of 1 or more")
            return
        }
        val capacity: Int? = if (_capacityText.isBlank()) null else {
            _capacityText.toIntOrNull() ?: run {
                fail("Capacity must be a whole number, or empty for no limit")
                return
            }
        }
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
            repo.updateLottery(
                lotteryId = lotteryId,
                campsiteIds = _selectedCampsiteIds,
                regStart = regStart, regEnd = regEnd,
                bookStart = bookStart, bookEnd = bookEnd,
                start = stayStart, end = stayEnd,
                numWinners = winners,
                waitlistCapacity = capacity
            ) { ok ->
                _isSaving = false
                if (ok) _saved = true else _error = "Could not save lottery"
            }
        } catch (e: IllegalArgumentException) {
            fail(e.message ?: "Invalid input")
        }
    }

    private fun fail(message: String) {
        _error = message
        _isSaving = false
    }
}