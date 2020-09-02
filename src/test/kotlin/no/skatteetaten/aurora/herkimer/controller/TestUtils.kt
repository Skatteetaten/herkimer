package no.skatteetaten.aurora.herkimer.controller

import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import no.skatteetaten.aurora.mockmvc.extensions.responseJsonPath
import org.springframework.test.web.servlet.ResultActions
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

inline fun <reified T : Any> mockLocalDateTimeToNow(fn: (lockedDateTime: LocalDateTime) -> T): Pair<LocalDateTime, T> {
    mockkStatic(LocalDateTime::class)

    val expectedTime = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)

    every {
        LocalDateTime.now()
    } returns expectedTime

    val result = fn(expectedTime)

    unmockkStatic(LocalDateTime::class)

    return expectedTime to result
}

fun ResultActions.validateAuditing(createdTime: LocalDateTime, modifiedTime: LocalDateTime = createdTime) =
    this.responseJsonPath("$.items[0].createdDate").equalsValue(createdTime.toString())
        .responseJsonPath("$.items[0].modifiedDate").equalsValue(modifiedTime.toString())
        .responseJsonPath("$.items[0].modifiedBy").equalsValue("aurora")
        .responseJsonPath("$.items[0].createdBy").equalsValue("aurora")
