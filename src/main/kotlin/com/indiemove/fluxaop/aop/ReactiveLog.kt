package com.indiemove.fluxaop.aop

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class ReactiveLog(
    val logType: String = "",
    val dimensionMap: Array<String> = [],
    val pathVariables: Array<String> = [],
    val headerNames: Array<String> = [],
    val queryParams: Array<String> = [],
    val bodyName: String = "", // include package
)
