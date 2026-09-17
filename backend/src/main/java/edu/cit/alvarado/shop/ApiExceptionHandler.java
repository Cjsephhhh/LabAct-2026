package edu.cit.alvarado.shop;
import org.springframework.http.HttpStatus; import org.springframework.web.bind.MethodArgumentNotValidException; import org.springframework.web.bind.annotation.*; import java.util.*;
@RestControllerAdvice public class ApiExceptionHandler {
 @ExceptionHandler(OrderServiceImpl.OrderNotFoundException.class) @ResponseStatus(HttpStatus.NOT_FOUND) public Map<String,Object> notFound(RuntimeException e){return error("NOT_FOUND",e.getMessage());}
 @ExceptionHandler(OrderServiceImpl.OrderAlreadyCancelledException.class) @ResponseStatus(HttpStatus.CONFLICT) public Map<String,Object> conflict(RuntimeException e){return error("CONFLICT",e.getMessage());}
 @ExceptionHandler(MethodArgumentNotValidException.class) @ResponseStatus(HttpStatus.BAD_REQUEST) public Map<String,Object> validation(MethodArgumentNotValidException e){return error("VALIDATION_ERROR","Please provide at least one valid item with a positive quantity.");}
 @ExceptionHandler(IllegalStateException.class) @ResponseStatus(HttpStatus.CONFLICT) public Map<String,Object> illegalState(RuntimeException e){return error("CONFLICT",e.getMessage());}
 @ExceptionHandler(IllegalArgumentException.class) @ResponseStatus(HttpStatus.BAD_REQUEST) public Map<String,Object> badRequest(RuntimeException e){return error("BAD_REQUEST",e.getMessage());}
 private Map<String,Object> error(String s,String m){var b=new LinkedHashMap<String,Object>();b.put("status",s);b.put("reason",m);return b;}
}
