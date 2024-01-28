package sample.cafekiosk.spring.api.service.order;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import sample.cafekiosk.spring.api.controller.order.request.OrderCreateRequest;
import sample.cafekiosk.spring.api.service.order.response.OrderResponse;
import sample.cafekiosk.spring.domain.order.OrderRepository;
import sample.cafekiosk.spring.domain.orderproduct.OrderProductRepository;
import sample.cafekiosk.spring.domain.product.Product;
import sample.cafekiosk.spring.domain.product.ProductRepository;
import sample.cafekiosk.spring.domain.product.ProductType;
import sample.cafekiosk.spring.domain.stock.Stock;
import sample.cafekiosk.spring.domain.stock.StockRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static sample.cafekiosk.spring.domain.product.ProductSellingStatus.*;
import static sample.cafekiosk.spring.domain.product.ProductType.*;

@ActiveProfiles("test")
@SpringBootTest
class OrderServiceTest {
    @Autowired
    private OrderService orderService;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private OrderProductRepository orderProductRepository;
    @Autowired
    private StockRepository stockRepository;
    @Autowired
    private ProductRepository productRepository;

    @AfterEach
    void tearDown() {
        orderProductRepository.deleteAllInBatch();
        productRepository.deleteAllInBatch();
        stockRepository.deleteAllInBatch();
        orderRepository.deleteAllInBatch();
    }

    @DisplayName("주문번호 리스트를 받아 주문을 생성한다.")
    @Test
    void createOrder() {
        // given
        final LocalDateTime registeredDateTime = LocalDateTime.now();

        final Product product1 = createProduct("001", HANDMADE, 1000);
        final Product product2 = createProduct("002", HANDMADE, 3000);
        final Product product3 = createProduct("003", HANDMADE, 5000);

        productRepository.saveAll(List.of(product1, product2, product3));

        final OrderCreateRequest request = OrderCreateRequest.builder()
                .productNumbers(List.of("001", "002"))
                .build();

        // when
        final OrderResponse orderResponse = orderService.createOrder(request, registeredDateTime);

        // then
        assertThat(orderResponse.getId()).isNotNull();
        assertThat(orderResponse)
                .extracting("registeredDateTime", "totalPrice")
                .contains(registeredDateTime, 4000);
        assertThat(orderResponse.getProducts())
                .hasSize(2)
                .extracting("productNumber", "price")
                .containsExactlyInAnyOrder(
                        tuple("001", 1000),
                        tuple("002", 3000)
                );
    }

    @Test
    @DisplayName("중복되는 상품번호 리스트로 주문을 생성할 수 있다.")
    void createOrderWithDuplicateProductNumbers() {
        // given
        final LocalDateTime registeredDateTime = LocalDateTime.now();

        final Product product1 = createProduct("001", HANDMADE, 1000);
        final Product product2 = createProduct("002", HANDMADE, 3000);
        final Product product3 = createProduct("003", HANDMADE, 5000);

        productRepository.saveAll(List.of(product1, product2, product3));

        final OrderCreateRequest request = OrderCreateRequest.builder()
                .productNumbers(List.of("001", "001"))
                .build();

        // when
        final OrderResponse orderResponse = orderService.createOrder(request, registeredDateTime);

        // then
        assertThat(orderResponse.getId()).isNotNull();
        assertThat(orderResponse)
                .extracting("registeredDateTime", "totalPrice")
                .contains(registeredDateTime, 2000);
        assertThat(orderResponse.getProducts())
                .hasSize(2)
                .extracting("productNumber", "price")
                .containsExactlyInAnyOrder(
                        tuple("001", 1000),
                        tuple("001", 1000)
                );
    }

    @DisplayName("재고와 관련된 상품이 포함되어 있는 주문번호 리스트를 받아 주문을 생성한다.")
    @Test
    void createOrderWithStock() {
        // given
        final LocalDateTime registeredDateTime = LocalDateTime.now();

        final Product product1 = createProduct("001", BOTTLE, 1000);
        final Product product2 = createProduct("002", BAKERY, 3000);
        final Product product3 = createProduct("003", HANDMADE, 5000);
        productRepository.saveAll(List.of(product1, product2, product3));

        final Stock stock1 = Stock.create("001", 2);
        final Stock stock2 = Stock.create("002", 2);
        stockRepository.saveAll(List.of(stock1, stock2));

        final OrderCreateRequest request = OrderCreateRequest.builder()
                .productNumbers(List.of("001", "001", "002", "003"))
                .build();

        // when
        final OrderResponse orderResponse = orderService.createOrder(request, registeredDateTime);

        // then
        assertThat(orderResponse.getId()).isNotNull();
        assertThat(orderResponse)
                .extracting("registeredDateTime", "totalPrice")
                .contains(registeredDateTime, 10000);
        assertThat(orderResponse.getProducts())
                .hasSize(4)
                .extracting("productNumber", "price")
                .containsExactlyInAnyOrder(
                        tuple("001", 1000),
                        tuple("001", 1000),
                        tuple("002", 3000),
                        tuple("003", 5000)
                );

        final List<Stock> stocks = stockRepository.findAll();
        assertThat(stocks)
                .hasSize(2)
                .extracting("productNumber", "quantity")
                .containsExactlyInAnyOrder(
                        tuple("001", 0),
                        tuple("002", 1)
                );
    }

    @DisplayName("재고가 부족한 상품으로 주문을 생성하려는 경우 예외가 발생한다.")
    @Test
    void createOrderWithNoStock() {
        // given
        final LocalDateTime registeredDateTime = LocalDateTime.now();

        final Product product1 = createProduct("001", BOTTLE, 1000);
        final Product product2 = createProduct("002", BAKERY, 3000);
        final Product product3 = createProduct("003", HANDMADE, 5000);
        productRepository.saveAll(List.of(product1, product2, product3));

        final Stock stock1 = Stock.create("001", 2);
        final Stock stock2 = Stock.create("002", 2);
        stock1.deductQuantity(1); // todo
        stockRepository.saveAll(List.of(stock1, stock2));

        final OrderCreateRequest request = OrderCreateRequest.builder()
                .productNumbers(List.of("001", "001", "002", "003"))
                .build();

        // when // then
        assertThatThrownBy(() -> orderService.createOrder(request, registeredDateTime))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("재고가 부족한 상품이 있습니다.");
    }

    private Product createProduct(final String productNumber, final ProductType type, final int price) {
        return Product.builder()
                .productNumber(productNumber)
                .type(type)
                .price(price)
                .sellingStatus(SELLING)
                .name("메뉴 이름")
                .build();
    }

}