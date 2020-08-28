package no.skatteetaten.aurora.herkimer.controller

class DataAccessException(message: String) : RuntimeException(message)

class NoSuchResourceException(
    val errorMessage: String
) : RuntimeException(errorMessage)
