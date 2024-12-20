package io.hhplus.tdd.point;

import io.hhplus.tdd.repository.UserPointRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class PointServiceConcurrencyTest {

    @Autowired
    PointService pointService;

    @Autowired
    UserPointRepository userPointRepository;

    @BeforeEach
    void beforeEach() {
        UserPoint userPoint = userPointRepository.findById(1L);
        if (userPoint.point() != 0) {
            userPointRepository.save(new UserPoint(1L, 0, 0));
        }
    }

    @DisplayName("같은 사용자의 포인트 충전 요청이 동시에 들어오더라도 한 번에 하나의 요청씩 처리된다.")
    @Test
    void chargeCurrently() throws InterruptedException {
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(threadCount);

        long userId = 1L;
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    pointService.charge(userId, 100L);
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();

        UserPoint userPoint = userPointRepository.findById(userId);
        assertThat(userPoint).isNotNull();
        assertThat(userPoint.point()).isEqualTo(10000L);
    }

    @DisplayName("같은 사용자의 포인트 사용 요청이 동시에 들어오더라도 한 번에 하나의 요청씩 처리된다.")
    @Test
    void useCurrently() throws InterruptedException {
        int threadCount = 100;
        long userId = 1L;
        pointService.charge(userId, 1000L);

        ExecutorService executorService = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    pointService.use(userId, 10L);
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();

        UserPoint userPoint = userPointRepository.findById(userId);
        assertThat(userPoint).isNotNull();
        assertThat(userPoint.point()).isEqualTo(0);
    }
}
