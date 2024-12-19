package io.hhplus.tdd.repository;

import io.hhplus.tdd.point.PointHistory;
import io.hhplus.tdd.point.TransactionType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@SpringBootTest
class PointHistoryRepositoryTest {

    @Autowired
    PointHistoryRepository pointHistoryRepository;

    @DisplayName("포인트 히스토리를 저장하고 조회한다.")
    @Test
    void basicTest() {
        // given
        PointHistory pointHistory = new PointHistory(0, 1L, 300L, TransactionType.CHARGE, 0);
        PointHistory pointHistory2 = new PointHistory(0, 1L, 200L, TransactionType.USE, 0);

        // when
        pointHistoryRepository.save(pointHistory);
        pointHistoryRepository.save(pointHistory2);

        // then
        List<PointHistory> pointHistoryList = pointHistoryRepository.findById(1L);
        assertThat(pointHistoryList).isNotNull();
        assertThat(pointHistoryList).hasSize(2)
                .extracting("userId", "amount", "type")
                .containsExactlyInAnyOrder(
                        tuple(1L, 300L, TransactionType.CHARGE),
                        tuple(1L, 200L, TransactionType.USE)
                );
    }
}