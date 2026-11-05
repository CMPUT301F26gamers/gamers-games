package com.example.campsiteapp

import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

class ProfileTest {

    private lateinit var profile: Profile

    @Before
    fun setUp() {
        profile = Profile("testUID")
    }

    @Test
    fun testConstruction(){
        val newProfile = Profile("testUID", "testName", "testEmail", "testPhoneNumber", "testProfilePictureUrl", false)
        assertEquals("testName", newProfile.name)
        assertEquals("testEmail", newProfile.email)
        assertEquals("testPhoneNumber", newProfile.phoneNumber)
        assertEquals("testProfilePictureUrl", newProfile.profilePictureUrl)
        assertFalse(newProfile.enableNotifications)
    }

    @Test
    fun checkInitialProfileInformation() {
        assertEquals("", profile.name)
        assertEquals("", profile.email)
        assertEquals("", profile.phoneNumber)
        assertEquals("", profile.profilePictureUrl)
        assertTrue(profile.enableNotifications)
    }

    @Test
    fun checkUpdateProfileInformationForAllParameters() {
        profile.updateProfileInformation("testName", "testEmail", "testPhoneNumber", "testProfilePictureUrl", false)
        assertEquals("testName", profile.name)
        assertEquals("testEmail", profile.email)
        assertEquals("testPhoneNumber", profile.phoneNumber)
        assertEquals("testProfilePictureUrl", profile.profilePictureUrl)
        assertFalse(profile.enableNotifications)

    }

    @Test
    fun checkUpdateProfileInformationForName() {
        profile.updateProfileInformation(name = "testName")
        assertEquals("testName", profile.name)
        assertEquals("", profile.email)
        assertEquals("", profile.phoneNumber)
        assertEquals("", profile.profilePictureUrl)
        assertTrue(profile.enableNotifications)
    }

    @Test
    fun checkUpdateProfileInformationForEmail() {
        profile.updateProfileInformation(email = "testEmail")
        assertEquals("", profile.name)
        assertEquals("testEmail", profile.email)
        assertEquals("", profile.phoneNumber)
        assertEquals("", profile.profilePictureUrl)
        assertTrue(profile.enableNotifications)
    }

    @Test
    fun checkUpdateProfileInformationForPhoneNumber() {
        profile.updateProfileInformation(phoneNumber = "testPhoneNumber")
        assertEquals("", profile.name)
        assertEquals("", profile.email)
        assertEquals("testPhoneNumber", profile.phoneNumber)
        assertEquals("", profile.profilePictureUrl)
        assertTrue(profile.enableNotifications)
    }

    @Test
    fun checkUpdateProfileInformationForProfilePictureUrl() {
        profile.updateProfileInformation(profilePictureUrl = "testProfilePictureUrl")
        assertEquals("", profile.name)
        assertEquals("", profile.email)
        assertEquals("", profile.phoneNumber)
        assertEquals("testProfilePictureUrl", profile.profilePictureUrl)
        assertTrue(profile.enableNotifications)
    }

    @Test
    fun checkUpdateProfileInformationForEnableNotifications() {
        profile.updateProfileInformation(enableNotifications = false)
        assertEquals("", profile.name)
        assertEquals("", profile.email)
        assertEquals("", profile.phoneNumber)
        assertEquals("", profile.profilePictureUrl)
        assertFalse(profile.enableNotifications)
    }

    @Test
    fun checkUpdateProfileInformationForNull() {
        profile.updateProfileInformation(name = "TestName", email = "TestEmail", phoneNumber = null, profilePictureUrl = null, enableNotifications = null)
        assertEquals("TestName", profile.name)
        assertEquals("TestEmail", profile.email)
        assertEquals("", profile.phoneNumber)
        assertEquals("", profile.profilePictureUrl)
        assertTrue(profile.enableNotifications)
    }

    @Test
    fun checkDefaultProfileRoles() {
        assertTrue(profile.hasRole(Roles.ENTRANT))
        assertFalse(profile.hasRole(Roles.OWNER))
        // Commented out for now to leave default for testing
        // assertFalse(profile.hasRole(Roles.ADMIN))
    }

    @Test
    fun checkAddingCampsiteOwnerRoles(){
        assertTrue(profile.hasRole(Roles.ENTRANT))
        assertFalse(profile.hasRole(Roles.OWNER))
        // assertFalse(profile.hasRole(Roles.ADMIN))
        profile.addRole(Roles.OWNER)
        assertTrue(profile.hasRole(Roles.ENTRANT))
        assertTrue(profile.hasRole(Roles.OWNER))
        // assertFalse(profile.hasRole(Roles.ADMIN))
    }
}