package no.skatteetaten.aurora.herkimer.controller

import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import java.lang.IllegalArgumentException

@ControllerAdvice
class ErrorHandler : ResponseEntityExceptionHandler() {

    @ExceptionHandler(NoSuchResourceException::class)
    fun handleNotFoundException(ex: NoSuchResourceException, req: WebRequest) = handleException(ex, req, HttpStatus.NOT_FOUND)

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(ex: IllegalArgumentException, req: WebRequest) = handleException(ex, req, HttpStatus.BAD_REQUEST)

    private fun handleException(e: Exception, request: WebRequest, httpStatus: HttpStatus): ResponseEntity<*> {

        logger.debug("error", e)
        val headers = HttpHeaders().apply { contentType = MediaType.APPLICATION_JSON }

        val error = when (e) {
            is NoSuchResourceException -> ErrorResponse(e.errorMessage)
            else -> ErrorResponse(e.message ?: "")
        }

        if (httpStatus.is5xxServerError) {
            logger.error("Unexpected error while handling request", e)
        }

        val response = AuroraResponse<ResourceBase>(success = false, message = e.message ?: "", errors = listOf(error))

        return handleExceptionInternal(e, response, headers, httpStatus, request)
    }
}
