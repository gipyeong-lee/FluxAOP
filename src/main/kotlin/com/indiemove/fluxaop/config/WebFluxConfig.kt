package com.indiemove.fluxaop

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.config.EnableWebFlux
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.ServerResponse

@Configuration
@EnableWebFlux
class WebFluxConfig {

    @Bean
    fun monoRouterFunction(
        testRouter: TestRouter
    ): RouterFunction<ServerResponse> {
        return testRouter.route()
    }
}
