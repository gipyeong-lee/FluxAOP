package com.indiemove.fluxaop

import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.router

@Component
class TestRouter(private val testHandler: TestHandler) {
    fun route() = router {
        GET("/test", testHandler::get)
        POST("/test", testHandler::post)
    }
}
