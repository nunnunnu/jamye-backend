package org.jy.jamye

import jakarta.servlet.http.HttpServletRequest
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@SpringBootApplication
@EnableScheduling
@EnableAsync
class JamyeApplication

fun main(args: Array<String>) {
    runApplication<JamyeApplication>(*args)
}

@RestController
class TestController {
    val log: Logger = LoggerFactory.getLogger(ExceptionHandler::class.java)
    @GetMapping
    fun test(request: HttpServletRequest): String {
        log.info("무중단배포 테스트")
        val referer = request.getHeader("Referer")
        val origin = request.getHeader("Origin")

        println("Referer: $referer")
        println("Origin: $origin")
        return "build test3"
    }

}