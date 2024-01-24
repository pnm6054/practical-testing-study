package sample.cafekiosk.spring.api.service.order;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sample.cafekiosk.spring.api.controller.order.request.OrderCreateRequest;
import sample.cafekiosk.spring.api.service.order.response.OrderResponse;
import sample.cafekiosk.spring.domain.order.Order;
import sample.cafekiosk.spring.domain.order.OrderRepository;
import sample.cafekiosk.spring.domain.product.Product;
import sample.cafekiosk.spring.domain.product.ProductRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    public OrderResponse createOrder(final OrderCreateRequest request, LocalDateTime registeredDateTime) {
        final List<String> productNumbers = request.getProductNumbers();

        final List<Product> duplicateProducts = findProductsBy(productNumbers);

        Order order = Order.create(duplicateProducts, registeredDateTime);
        final Order saveOrder = orderRepository.save(order);
        return OrderResponse.of(saveOrder);
    }

    private List<Product> findProductsBy(final List<String> productNumbers) {
        final List<Product> products = productRepository.findAllByProductNumberIn(productNumbers);
        final Map<String, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getProductNumber, product -> product));

        final List<Product> duplicateProducts = productNumbers.stream()
                .map(productMap::get)
                .toList();
        return duplicateProducts;
    }
}
