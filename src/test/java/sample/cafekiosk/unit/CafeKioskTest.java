package sample.cafekiosk.unit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import sample.cafekiosk.unit.beverage.Americano;
import sample.cafekiosk.unit.beverage.Latte;
import sample.cafekiosk.unit.order.Order;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CafeKioskTest {

    @Test
    void testtt() {
        final CafeKiosk cafeKiosk = new CafeKiosk();
        cafeKiosk.add(new Americano());

        // 의미없는 테스트
        System.out.println(">>> 담긴 음료 수 : " + cafeKiosk.getBeverages().size());
        System.out.println(">>> 담긴 음료 : " + cafeKiosk.getBeverages().get(0).getName());
    }

    @DisplayName("음료 1개를 추가하면 주문 목록에 담긴다.")
    @Test
    void add() {
        final CafeKiosk cafeKiosk = new CafeKiosk();
        cafeKiosk.add(new Americano());

        assertThat(cafeKiosk.getBeverages()).hasSize(1);
        assertThat(cafeKiosk.getBeverages().get(0).getName()).isEqualTo("아메리카노");
    }

    @Test
    void remove() {
        final CafeKiosk cafeKiosk = new CafeKiosk();
        final Americano americano = new Americano();

        cafeKiosk.add(americano);
        assertThat(cafeKiosk.getBeverages()).hasSize(1);

        cafeKiosk.remove(americano);
        assertThat(cafeKiosk.getBeverages()).isEmpty();

        assertThat(cafeKiosk.getBeverages()).hasSize(0);
    }

    @Test
    void clear() {
        final CafeKiosk cafeKiosk = new CafeKiosk();
        final Americano americano = new Americano();
        final Latte latte = new Latte();

        cafeKiosk.add(americano);
        cafeKiosk.add(latte);
        assertThat(cafeKiosk.getBeverages()).hasSize(2);

        cafeKiosk.clear();
        assertThat(cafeKiosk.getBeverages()).isEmpty();
    }

    @DisplayName("주문 목록에 담긴 상품들의 총 금액을 계산할 수 있다.")
    @Test
    void calculateTotalPrice() {
        // given
        final CafeKiosk cafeKiosk = new CafeKiosk();
        final Americano americano = new Americano();
        final Latte latte = new Latte();

        cafeKiosk.add(americano);
        cafeKiosk.add(latte);

        // when
        final int totalPrice = cafeKiosk.calculateTotalPrice();

        // then
        assertThat(totalPrice).isEqualTo(8500);

    }

    @Test
    void addSeveralBeverages() {
        final CafeKiosk cafeKiosk = new CafeKiosk();
        final Americano americano = new Americano();

        cafeKiosk.add(americano, 2);

        assertThat(cafeKiosk.getBeverages().get(0)).isEqualTo(americano);
        assertThat(cafeKiosk.getBeverages().get(1)).isEqualTo(americano);
    }

    @Test
    void addZeroBeverages() {
        final CafeKiosk cafeKiosk = new CafeKiosk();
        final Americano americano = new Americano();

        assertThatThrownBy(() -> cafeKiosk.add(americano, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("음료는 1잔 이상 주문하실 수 있습니다.");
    }

    @Test
    void createOrderWithCurrentTime() {
        final CafeKiosk cafeKiosk = new CafeKiosk();
        final Americano americano = new Americano();

        cafeKiosk.add(americano);

        final Order order = cafeKiosk.createOrder(LocalDateTime.of(2023, 1, 16, 10, 0));

        assertThat(order.getBeverages()).hasSize(1);
        assertThat(order.getBeverages().get(0).getName()).isEqualTo("아메리카노");
    }

    @Test
    void createOrderOutsideOpenTime() {
        final CafeKiosk cafeKiosk = new CafeKiosk();
        final Americano americano = new Americano();

        cafeKiosk.add(americano);

        assertThatThrownBy(() -> cafeKiosk.createOrder(LocalDateTime.of(2023, 1, 16, 9, 59)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("현재 주문 가능한 시간이 아닙니다.");
    }
}