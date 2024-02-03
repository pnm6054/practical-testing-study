package sample.cafekiosk.spring.api;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import sample.cafekiosk.spring.api.service.product.response.ProductResponse;

@Getter
public class ApiResponse<T> {
    private int code;
    private HttpStatus status;
    private String message;
    private T data;

    public ApiResponse(final HttpStatus status, final String message, final T data) {
        this.code = status.value();
        this.status = status;
        this.message = message;
        this.data = data;
    }

    public static <T> ApiResponse<T> of(final HttpStatus httpStatus, final String message, final T data) {
        return new ApiResponse<>(httpStatus, message, data);
    }

    public static <T> ApiResponse<T> of(final HttpStatus httpStatus, final T data) {
        return of(httpStatus, httpStatus.name(), data);
    }

    public static <T> ApiResponse<T> ok(final T data) {
        return of(HttpStatus.OK, data);
    }
}
