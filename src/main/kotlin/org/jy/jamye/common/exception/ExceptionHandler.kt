package org.jy.jamye.common.exception

import com.fasterxml.jackson.core.JsonParseException
import jakarta.persistence.EntityNotFoundException
import org.hibernate.exception.ConstraintViolationException
import org.jy.jamye.common.io.ResponseDto
import org.springframework.beans.TypeMismatchException
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
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.multipart.MultipartException
import org.springframework.web.multipart.support.MissingServletRequestPartException

@RestControllerAdvice
class ExceptionHandler {

    @ExceptionHandler(IllegalArgumentException::class,
        MethodArgumentNotValidException::class,
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
    fun handleException(e: Exception): ResponseEntity<ResponseDto<Nothing>> {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ResponseDto(message = e.message, status = HttpStatus.BAD_REQUEST))
    }

    @ExceptionHandler(BadCredentialsException::class
    )
    fun authExceptionHandler(e: Exception): ResponseEntity<ResponseDto<Nothing>> {
        return ResponseEntity
            .status(HttpStatus.FORBIDDEN)
            .body(ResponseDto(message = e.message, status = HttpStatus.FORBIDDEN))
    }
}