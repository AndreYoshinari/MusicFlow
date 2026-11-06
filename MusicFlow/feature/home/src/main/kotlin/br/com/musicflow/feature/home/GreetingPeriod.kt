package br.com.musicflow.feature.home

import java.time.LocalTime

enum class GreetingPeriod {
    Morning,
    Afternoon,
    Night,
}

fun greetingPeriod(time: LocalTime): GreetingPeriod = when (time.hour) {
    in 5..11 -> GreetingPeriod.Morning
    in 12..17 -> GreetingPeriod.Afternoon
    else -> GreetingPeriod.Night
}
