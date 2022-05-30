package com.indiemove.fluxaop

import com.indiemove.fluxaop.aop.ReactiveLog
import com.indiemove.fluxaop.aop.bodyToMonoWithLog
import com.indiemove.fluxaop.domain.TestBody
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono

@Component
class TestHandler {

    @ReactiveLog(
        logType = "GET"
    )
    fun get(serverRequest: ServerRequest): Mono<ServerResponse> {
        return serverRequest.bodyToMonoWithLog(Void::class.java).flatMap {
            ServerResponse.ok().build()
        }
    }

    @ReactiveLog(
        logType = "POST",
        bodyName = "TestBody"
    )
    fun post(serverRequest: ServerRequest): Mono<ServerResponse> {
        return serverRequest.bodyToMonoWithLog(TestBody::class.java).flatMap {
            ServerResponse.ok().build()
        }
    }
}
