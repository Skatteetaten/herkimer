package no.skatteetaten.aurora.herkimer.controller

class DataAccessException(message: String) : RuntimeException(message)
class NoSuchResourceException(
    val resourceIds: List<String>
) : RuntimeException("")
