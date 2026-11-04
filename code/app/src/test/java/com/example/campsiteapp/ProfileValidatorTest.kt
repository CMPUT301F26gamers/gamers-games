package com.example.campsiteapp

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ProfileValidatorTest {

    private lateinit var validator: ProfileValidator

    @Before
    fun setUp() {
        validator = ProfileValidator()
    }

    @Test
    fun isValidName_normalName_returnsTrue() {
        assertTrue(validator.isValidName("Alex"))
    }

    @Test
    fun isValidName_emptyOrSpaces_returnsFalse() {
        assertFalse(validator.isValidName(""))
        assertFalse(validator.isValidName("   "))
    }

    @Test
    fun isValidEmail_validEmails_returnsTrue() {
        assertTrue(validator.isValidEmail("alex@example.com"))
        assertTrue(validator.isValidEmail("alex.p@mail.ualberta.ca"))
        assertTrue(validator.isValidEmail("  alex@example.com  "))
    }

    @Test
    fun isValidEmail_missingParts_returnsFalse() {
        assertFalse(validator.isValidEmail(""))
        assertFalse(validator.isValidEmail("alex"))
        assertFalse(validator.isValidEmail("alex@"))
        assertFalse(validator.isValidEmail("@example.com"))
        assertFalse(validator.isValidEmail("alex@example"))
    }

    @Test
    fun isValidEmail_badFormat_returnsFalse() {
        assertFalse(validator.isValidEmail("alex@@example.com"))
        assertFalse(validator.isValidEmail("al ex@example.com"))
        assertFalse(validator.isValidEmail("alex@.com"))
        assertFalse(validator.isValidEmail("alex@example.c"))
        assertFalse(validator.isValidEmail("alex@example."))
    }

    @Test
    fun isValidPhone_empty_returnsTrue() {
        assertTrue(validator.isValidPhone(""))
        assertTrue(validator.isValidPhone("   "))
    }

    @Test
    fun isValidPhone_validNumbers_returnsTrue() {
        assertTrue(validator.isValidPhone("7805550123"))
        assertTrue(validator.isValidPhone("780-555-0123"))
        assertTrue(validator.isValidPhone("+1 (780) 555 0123"))
    }

    @Test
    fun isValidPhone_wrongLength_returnsFalse() {
        assertFalse(validator.isValidPhone("555-0123"))
        assertFalse(validator.isValidPhone("1234567890123456"))
    }

    @Test
    fun isValidPhone_badCharacters_returnsFalse() {
        assertFalse(validator.isValidPhone("780-555-abcd"))
        assertFalse(validator.isValidPhone("780+555+0123"))
    }
}
