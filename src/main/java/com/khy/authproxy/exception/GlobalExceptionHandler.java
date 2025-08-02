package com.khy.authproxy.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    @ExceptionHandler(NotFoundUserInfoException.class)
    public ResponseEntity<?> handleMyCustomException(NotFoundUserInfoException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body("공급자로부터 사용자 정보를 읽어오지 못했습니다.\n" + ex.getMessage());
    }

    // 인증/권한 예외 처리 (예시)
    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<?> handleAccessDenied(Exception ex) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body("접근 권한이 없습니다.");
    }

    // 모든 예외 잡기 (순서상 마지막에 권장)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleAllException(Exception ex) {
        // 예: 로그 남기기, 에러 메시지 숨기기 등
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("시스템 오류가 발생했습니다.");
    }
}
