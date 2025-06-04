package org.jy.jamye.common.exception

import com.fasterxml.jackson.core.JsonParseException
import io.sentry.Sentry
import jakarta.persistence.EntityNotFoundException
import org.hibernate.exception.ConstraintViolationException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.TypeMismatchException
import org.springframework.core.env.Environment
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.web.HttpMediaTypeNotSupportedException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.UnsatisfiedServletRequestParameterException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.multipart.MultipartException
import org.springframework.web.multipart.support.MissingServletRequestPartException

@RestControllerAdvice
class ExceptionHandler(private val env: Environment) {
    val log: Logger = LoggerFactory.getLogger(ExceptionHandler::class.java)

    @ExceptionHandler(IllegalArgumentException::class,
        EntityNotFoundException::class,
        JsonParseException::class,
        HttpMediaTypeNotSupportedException::class,
        HttpMessageNotReadableException::class,
        MissingServletRequestPartException::class,
        MissingServletRequestParameterException::class,
        ConstraintViolationException::class,
        TypeMismatchException::class,
        MethodArgumentTypeMismatchException::class,
        MultipartException::class,
        UnsatisfiedServletRequestParameterException::class,
        HttpRequestMethodNotSupportedException::class
    )
    fun handleException(e: Exception): ResponseEntity<ErrorResponseDto> {
        log.info(e.printStackTrace().toString())
        return ResponseEntity(
            ErrorResponseDto(
                status = HttpStatus.BAD_REQUEST.value(),
                message = e.message
            ), HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValidException(e: MethodArgumentNotValidException): ResponseEntity<ErrorResponseDto> {
        return ResponseEntity(
            ErrorResponseDto(
                status = HttpStatus.BAD_REQUEST.value(),
                message = e.bindingResult.fieldError!!.defaultMessage
            ), HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(BasicException::class)
    fun basicException(e: BasicException): ResponseEntity<ErrorResponseDto> {
        return ResponseEntity(ErrorResponseDto(
                status = e.status.value(),
                error = e.status.reasonPhrase,
                message = e.errorCode.message,
                code = e.errorCode
            ), e.status)
    }

    @ExceptionHandler(BadCredentialsException::class
    )
    fun authExceptionHandler(e: Exception): ResponseEntity<ErrorResponseDto> {
        return ResponseEntity(ErrorResponseDto(
                status = HttpStatus.FORBIDDEN.value(),
                message = e.message
            ), HttpStatus.FORBIDDEN)
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception::class)
    fun exception(e: Exception): ResponseEntity<ErrorResponseDto> {
        log.info(e.printStackTrace().toString())
        reportErrorToSentry(e)
        return ResponseEntity(ErrorResponseDto(
            status = HttpStatus.BAD_REQUEST.value(),
            message = e.message,
            code = ErrorCode.SERVER_ERROR
        ), HttpStatus.BAD_REQUEST)
    }

    	private fun reportErrorToSentry(e: Exception) {
            if (!env.activeProfiles.contains("local")) return

            Sentry.captureException(e); //sentry 에러 전송
	}
}