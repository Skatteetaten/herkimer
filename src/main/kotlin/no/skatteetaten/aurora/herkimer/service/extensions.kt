package no.skatteetaten.aurora.herkimer.service

import org.springframework.dao.DuplicateKeyException

fun <T> Result<T>.getOrElseReturnIfDuplicate(fn: () -> T): T =
    getOrElse {
        when (it.cause) {
            is DuplicateKeyException -> fn()
            else -> throw it
        }
    }
