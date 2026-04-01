package io.hhplus.tdd.point;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.IntStream;

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
        int threadCount = 20;
        long chargePoint = 100L;
        long userId = 1L;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    pointService.charge(userId, chargePoint);
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();

        UserPoint userPoint = userPointRepository.findById(userId);
        assertThat(userPoint).isNotNull();
        assertThat(userPoint.point()).isEqualTo(threadCount * chargePoint);
    }

    @DisplayName("같은 사용자의 포인트 사용 요청이 동시에 들어오더라도 한 번에 하나의 요청씩 처리된다.")
    @Test
    void useCurrently() throws InterruptedException {
        int threadCount = 20;
        long initialPoint = 2000L;
        long usePoint = 10L;
        long userId = 1L;

        pointService.charge(userId, initialPoint);

        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    pointService.use(userId, usePoint);
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();

        UserPoint userPoint = userPointRepository.findById(userId);
        assertThat(userPoint).isNotNull();
        assertThat(userPoint.point()).isEqualTo(initialPoint - threadCount * usePoint);
    }

    @DisplayName("여러 사용자의 포인트 사용/충전 요청이 동시에 들어오더라도 동시성 문제 없이 처리된다.")
    @RepeatedTest(2)
    void useAndChargeCurrently() throws InterruptedException, ExecutionException {
        // given
        int userCount = 10;
        long initialPoint = 20_000L;
        int reqCount = 25;
        long chargePoint = 50;
        long usePoint = 20;

        IntStream.rangeClosed(1, userCount).forEach(i -> {
            userPointRepository.save(new UserPoint(i, 0, 0));
        });

        // 초기 포인틀를 가진 10명의 사용자 생성
        for (long userId = 1; userId <= userCount; userId++) {
            pointService.charge(userId, initialPoint);
        }

        // 무작위의 사용/충전 요청 스레드 생성
        ExecutorService executorService = Executors.newFixedThreadPool(userCount * (2 * reqCount));
        List<Callable<Void>> tasks = new ArrayList<>();
        for (long userId = 1; userId <= userCount; userId++) {
            tasks.addAll(
                    generateShuffledUseAndChargeThread(userId, usePoint, chargePoint, reqCount)
            );
        }
        Collections.shuffle(tasks);

        // when
        List<Future<Void>> futures = executorService.invokeAll(tasks);
        for (Future<Void> future : futures) {
            future.get();
        }

        // then
        long expectedPoint = initialPoint + (reqCount * chargePoint) - (reqCount * usePoint);
        for (long userId = 1; userId <= userCount; userId++) {
            long currentPoint = pointService.getPoint(userId).point();
            assertThat(currentPoint).isEqualTo(expectedPoint);
        }
    }

    private List<Callable<Void>> generateShuffledUseAndChargeThread(
            long userId,
            long usePoint,
            long chargePoint,
            int reqCount
    ) {
        List<Callable<Void>> jobs = new ArrayList<>();
        for (int i = 0; i < reqCount; i++) {
            jobs.add(
                    () -> {
                        pointService.use(userId, usePoint);
                        return null;
                    }
            );
        }
        for (int i = 0; i < reqCount; i++) {
            jobs.add(
                    () -> {
                        pointService.charge(userId, chargePoint);
                        return null;
                    }
            );
        }
        Collections.shuffle(jobs);
        return jobs;
    }
}
