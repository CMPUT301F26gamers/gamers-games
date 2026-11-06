package com.example.campsiteapp

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CampsiteValidatorTest {

    private lateinit var validator: CampsiteValidator

    @Before
    fun setUp() {
        validator = CampsiteValidator()
    }

    @Test
    fun isValidName_normalName_returnsTrue() {
        assertTrue(validator.isValidName("Pine Ridge"))
    }

    @Test
    fun isValidName_emptyOrSpaces_returnsFalse() {
        assertFalse(validator.isValidName(""))
        assertFalse(validator.isValidName("   "))
    }

    @Test
    fun isValidLocation_normalLocation_returnsTrue() {
        assertTrue(validator.isValidLocation("Jasper, AB"))
    }

    @Test
    fun isValidLocation_emptyOrSpaces_returnsFalse() {
        assertFalse(validator.isValidLocation(""))
        assertFalse(validator.isValidLocation("  "))
    }

    @Test
    fun isValidMaxGuests_wholeNumbersOneOrMore_returnsTrue() {
        assertTrue(validator.isValidMaxGuests("1"))
        assertTrue(validator.isValidMaxGuests("8"))
        assertTrue(validator.isValidMaxGuests(" 12 "))
    }

    @Test
    fun isValidMaxGuests_zeroNegativeOrNotANumber_returnsFalse() {
        assertFalse(validator.isValidMaxGuests(""))
        assertFalse(validator.isValidMaxGuests("0"))
        assertFalse(validator.isValidMaxGuests("-3"))
        assertFalse(validator.isValidMaxGuests("2.5"))
        assertFalse(validator.isValidMaxGuests("four"))
    }

    @Test
    fun parseTags_splitsAndTrims() {
        assertEquals(
            listOf("lakeside", "pet friendly", "fire pit"),
            validator.parseTags("lakeside, pet friendly ,fire pit")
        )
    }

    @Test
    fun parseTags_dropsEmptyAndDuplicateTags() {
        assertEquals(listOf("lakeside", "wifi"), validator.parseTags("lakeside,, ,Lakeside, wifi,"))
    }

    @Test
    fun parseTags_emptyInput_returnsNoTags() {
        assertTrue(validator.parseTags("").isEmpty())
        assertTrue(validator.parseTags("   ").isEmpty())
    }
}
