package com.sih.civic.web;
import org.springframework.http.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(NoSuchElementException.class)
    ResponseEntity<?> notFound(Exception e){ return ResponseEntity.status(404).body(Map.of("error",e.getMessage())); }
    @ExceptionHandler(SecurityException.class)
    ResponseEntity<?> forbidden(Exception e){ return ResponseEntity.status(403).body(Map.of("error",e.getMessage())); }
    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<?> denied(Exception e){ return ResponseEntity.status(403).body(Map.of("error","Admin access required")); }
    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<?> badRequest(Exception e){ return ResponseEntity.badRequest().body(Map.of("error",e.getMessage())); }
    @ExceptionHandler(org.springframework.security.authentication.BadCredentialsException.class)
    ResponseEntity<?> badCredentials(Exception e){ return ResponseEntity.status(401).body(Map.of("error","Invalid email or password")); }
    @ExceptionHandler(Exception.class)
    ResponseEntity<?> generic(Exception e){
        return ResponseEntity.status(500).body(Map.of("error","Unexpected server error"));
    }
}
