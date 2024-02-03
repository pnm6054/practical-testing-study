package sample.cafekiosk.spring.api.service.order.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class OrderCreateServiceRequest {
    private List<String> productNumbers;

    @Builder
    public OrderCreateServiceRequest(final List<String> productNumbers) {
        this.productNumbers = productNumbers;
    }
}
