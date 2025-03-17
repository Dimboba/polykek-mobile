package laz.dimboba.sounddetection.mobileserver.security

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(AuthException::class)
    fun handleAuthException(ex: AuthException): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(ex.message ?: "Authentication error")
        return ResponseEntity(errorResponse, HttpStatus.UNAUTHORIZED)
    }
}

open class AuthException(message: String): RuntimeException(message)

data class ErrorResponse(val message: String)