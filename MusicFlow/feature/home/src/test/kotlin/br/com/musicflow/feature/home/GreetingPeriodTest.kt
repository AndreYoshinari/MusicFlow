package br.com.musicflow.feature.home

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalTime

class GreetingPeriodTest {
    @Test
    fun greetingFollowsTheHour() {
        assertEquals(GreetingPeriod.Night, greetingPeriod(LocalTime.of(4, 59)))
        assertEquals(GreetingPeriod.Morning, greetingPeriod(LocalTime.of(5, 0)))
        assertEquals(GreetingPeriod.Morning, greetingPeriod(LocalTime.of(11, 59)))
        assertEquals(GreetingPeriod.Afternoon, greetingPeriod(LocalTime.of(12, 0)))
        assertEquals(GreetingPeriod.Afternoon, greetingPeriod(LocalTime.of(17, 59)))
        assertEquals(GreetingPeriod.Night, greetingPeriod(LocalTime.of(18, 0)))
    }
}
