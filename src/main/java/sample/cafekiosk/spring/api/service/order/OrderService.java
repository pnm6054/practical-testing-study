package sample.cafekiosk.spring.api.service.order;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sample.cafekiosk.spring.api.controller.order.request.OrderCreateRequest;
import sample.cafekiosk.spring.api.service.order.response.OrderResponse;
import sample.cafekiosk.spring.domain.order.Order;
import sample.cafekiosk.spring.domain.order.OrderRepository;
import sample.cafekiosk.spring.domain.product.Product;
import sample.cafekiosk.spring.domain.product.ProductRepository;
import sample.cafekiosk.spring.domain.product.ProductType;
import sample.cafekiosk.spring.domain.stock.Stock;
import sample.cafekiosk.spring.domain.stock.StockRepository;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Transactional
@Service
@RequiredArgsConstructor
public class OrderService {
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final StockRepository stockRepository;

    /**
     * 재고 감소 -> 동시성 고민
     * optimistic lock / pessimistic lock
     */
    public OrderResponse createOrder(final OrderCreateRequest request, LocalDateTime registeredDateTime) {
        final List<String> productNumbers = request.getProductNumbers();
        final List<Product> products = findProductsBy(productNumbers);

        deductStockQuantities(products);

        Order order = Order.create(products, registeredDateTime);
        final Order saveOrder = orderRepository.save(order);
        return OrderResponse.of(saveOrder);
    }

    private void deductStockQuantities(final List<Product> products) {
        final List<String> stockProductNumbers = extractStockProductNumbers(products);

        final Map<String, Stock> stockMap = createStockMapBy(stockProductNumbers);
        final Map<String, Long> productCountingMap = createCountingMapBy(stockProductNumbers);

        for (String stockProductNumber : new HashSet<>(stockProductNumbers)) {
            final Stock stock = stockMap.get(stockProductNumber);
            final int quantity = productCountingMap.get(stockProductNumber).intValue();

            if (stock.isQuantityLessThan(quantity)) {
                throw new IllegalArgumentException("재고가 부족한 상품이 있습니다.");
            }

            stock.deductQuantity(quantity);
        }
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

    private static List<String> extractStockProductNumbers(final List<Product> products) {
        return products.stream()
                .filter(product -> ProductType.containsStockType(product.getType()))
                .map(Product::getProductNumber)
                .toList();
    }

    private static Map<String, Long> createCountingMapBy(final List<String> stockProductNumbers) {
        return stockProductNumbers.stream()
                .collect(Collectors.groupingBy(productNumber -> productNumber, Collectors.counting()));
    }

    private Map<String, Stock> createStockMapBy(final List<String> stockProductNumbers) {
        return stockRepository.findAllByProductNumberIn(stockProductNumbers).stream()
                .collect(Collectors.toMap(Stock::getProductNumber, stock -> stock));
    }
}
