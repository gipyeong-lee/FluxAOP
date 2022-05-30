package com.indiemove.fluxaop.aop

import org.apache.logging.log4j.util.Strings
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.reflect.MethodSignature
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.server.ServerRequest
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.lang.reflect.Method
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties

val log = LoggerFactory.getLogger("Aspect")

@Aspect
@Configuration
class ReactiveLogAspect {
    companion object {
        const val KEY_RESPONSE = "RESPONSE"
    }

    @Around("@annotation(com.indiemove.fluxaop.aop.ReactiveLog)")
    fun around(point: ProceedingJoinPoint): Any {
        val args = point.args

        val result = point.proceed()
        val signature: MethodSignature = point.signature as MethodSignature
        val method: Method = signature.method
        val reactiveLog: ReactiveLog = method.getAnnotation(ReactiveLog::class.java)

        // 0. skip if not on Webflux Handler
        if (args[0] !is ServerRequest) {
            return result
        }
        log.info("Before Send Log")
        (result as Mono<*>).doFinally {
            log.info("send Log")
            val body = mutableMapOf<String, String?>()
            val serverRequest = args[0] as ServerRequest
            body.buildDimension(serverRequest, reactiveLog)
            log.info(body.toString())
        }.subscribeOn(Schedulers.boundedElastic()).subscribe()
        return result
    }
}

fun <T> T.sendLog(
    args: Array<Any>,
    reactiveLog: ReactiveLog
): Any {
    log.info("Inside Send Log")
    return when (this) {
        is Mono<*> -> {
            return this.doOnNext {
                val serverRequest = args[0] as ServerRequest
                log.info("MONO")
            }
        }
        is Flux<*> -> {
            val serverRequest = args[0] as ServerRequest
            return this.doFinally {
                log.info("FLUX")
            }
        }
        else -> {
            Mono.just(Unit)
        }
    }
}

inline fun <reified T : ServerRequest, K : Any> T.bodyToMonoWithLog(
    elementClass: Class<out K>,
    dimensions: Map<String, String>? = mapOf()
): Mono<K> {
    return this.bodyToMono(elementClass).map {
        val bodyMap = (it as Any).asMap().toMutableMap()
        dimensions?.map { dimension -> bodyMap[dimension.key] = dimension.value }
        this.attributes()[elementClass.simpleName] = bodyMap
        it
    }
}

fun MutableMap<String, String?>.buildDimension(
    serverRequest: ServerRequest,
    reactiveLog: ReactiveLog
) {
    try {
        reactiveLog.pathVariables.map { key ->
            this.put(
                key,
                serverRequest.pathVariable(key)
            )
        }
        reactiveLog.headerNames.map { key ->
            this.put(key, serverRequest.headers().firstHeader(key) ?: Strings.EMPTY)
        }
        reactiveLog.queryParams.map { key ->
            this.put(key, serverRequest.queryParam(key).orElse(Strings.EMPTY))
        }
        this[ReactiveLogAspect.KEY_RESPONSE] =
            serverRequest.attributes().getOrDefault(ReactiveLogAspect.KEY_RESPONSE, Strings.EMPTY) as String

    } catch (e: Exception) { //ignore wrong path argument
    }
}


inline fun <reified T : Any> T.asMap(): Map<String, String?> {
    val props = (this::class as KClass<T>).memberProperties.associateBy { it.name }
    val result = props.keys.associateWith { key ->
        val property = this::class.members.find { it.name == key } as KProperty1<Any, *>
        property.get(this).toString()
    }
    return result.toMap()
}
