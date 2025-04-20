package org.jy.jamye.ui

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import org.springframework.mock.env.MockEnvironment
import org.springframework.test.context.junit4.SpringRunner


//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ProfileControllerTest {
//    @LocalServerPort
//    val port: Int? = null
//
//    @Autowired
//    val restTemplate: TestRestTemplate? = null

//    @Test
//    fun noAuthorization() {
//        val expectedProfile = "default"
//
//        val response = restTemplate!!.getForEntity("/profile", String::class.java)
//        assertEquals(expectedProfile, response.body)
//        assertEquals(HttpStatus.OK, response.statusCode)
//    }

    @Test
    fun testGetProfile() {
        val expectedProfile = "real"

        val env = MockEnvironment()
        env.addActiveProfile(expectedProfile)
        env.addActiveProfile("oauth")
        env.addActiveProfile("real-db")

        val controller = ProfileController(env)

        val profile = controller.profile()

        assertEquals(expectedProfile, profile)
    }

    @Test
    fun testGetDefaultProfile() {
        val expectedProfile = "default"

        val env = MockEnvironment()

        val controller = ProfileController(env)

        val profile = controller.profile()

        assertEquals(expectedProfile, profile)
    }

    @Test
    fun testGetFirstProfile() {
        val expectedProfile = "oauth"

        val env = MockEnvironment()
        env.addActiveProfile("oauth")
        env.addActiveProfile("real-db")

        val controller = ProfileController(env)

        val profile = controller.profile()

        assertEquals(expectedProfile, profile)
    }
}