package be.sgl.backend.config

import be.sgl.backend.service.exception.IncompleteConfigurationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(IncompleteConfigurationException::class)
    fun handleIncompleteConfigurationException(ex: IncompleteConfigurationException): ResponseEntity<String> {
        return ResponseEntity(ex.message, HttpStatus.CONFLICT)
    }

    fun handleCoffeeException(): ResponseEntity<String> {
        return ResponseEntity("Drink some tea", HttpStatus.I_AM_A_TEAPOT)
    }
}
