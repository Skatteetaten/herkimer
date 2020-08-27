package no.skatteetaten.aurora.herkimer.controller

import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

@ControllerAdvice
class ErrorHandler : ResponseEntityExceptionHandler() {

    @ExceptionHandler(NoSuchResourceException::class)
    fun handleNotFoundException(ex: NoSuchResourceException, req: WebRequest) = handleException(ex, req, HttpStatus.NOT_FOUND)

    private fun handleException(e: Exception, request: WebRequest, httpStatus: HttpStatus): ResponseEntity<*> {

        logger.debug("error", e)
        val headers = HttpHeaders().apply { contentType = MediaType.APPLICATION_JSON }

        val errors = when (e) {
            is NoSuchResourceException -> e.resourceIds.map { ErrorResponse(it) }
            else -> listOf(ErrorResponse(e.message ?: ""))
        }

        if (httpStatus.is5xxServerError) {
            logger.error("Unexpected error while handling request", e)
        }

        val response = AuroraResponse<Resource>(success = false, message = e.message ?: "", errors = errors)

        return handleExceptionInternal(e, response, headers, httpStatus, request)
    }
}
