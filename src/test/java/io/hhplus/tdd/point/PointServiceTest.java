package io.hhplus.tdd.point;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PointServiceTest {

    @Mock
    PointHistoryRepository pointHistoryRepository;

    @Mock
    UserPointRepository userPointRepository;

    @InjectMocks
    PointService pointService;

    @DisplayName("특정 유저의 포인트를 조회한다.")
    @Test
    void getPoint() {
        // given
        long id = 1L;
        given(userPointRepository.findById(anyLong()))
                .willReturn(new UserPoint(id, 30L, System.currentTimeMillis()));

        // when
        UserPoint userPoint = pointService.getPoint(id);

        // then
        assertThat(userPoint).isNotNull();
        assertThat(userPoint).extracting("id", "point")
                .containsExactly(id, 30L);
    }

    @DisplayName("특정 유저의 포인트 충전/이용 내역을 조회한다.")
    @Test
    void getPointHistory() {
        // given
        given(pointHistoryRepository.findById(anyLong()))
                .willReturn(List.of(
                        new PointHistory(1L, 1L, 300L, TransactionType.CHARGE, System.currentTimeMillis()),
                        new PointHistory(2L, 1L, 200L, TransactionType.USE, System.currentTimeMillis())
                ));
        // when
        List<PointHistory> pointHistoryList = pointService.getPointHistory(1L);

        // then
        assertThat(pointHistoryList).isNotNull();
        assertThat(pointHistoryList).hasSize(2)
                .extracting("id", "userId", "amount", "type")
                .containsExactlyInAnyOrder(
                        tuple(1L, 1L, 300L, TransactionType.CHARGE),
                        tuple(2L, 1L, 200L, TransactionType.USE)
                );
    }

    @DisplayName("특정 유저의 포인트를 충전한다.")
    @Test
    void charge() {
        // given
        given(userPointRepository.findById(anyLong()))
                .willReturn(UserPoint.empty(1L));
        given(userPointRepository.save(any(UserPoint.class)))
                .willReturn(new UserPoint(1L, 300L, System.currentTimeMillis()));

        // when
        UserPoint userPoint = pointService.charge(1L, 300L);

        // then
        assertThat(userPoint).isNotNull();
        assertThat(userPoint).extracting("id", "point")
                .containsExactly(1L, 300L);
        verify(pointHistoryRepository).save(any(PointHistory.class));
    }

    @DisplayName("충전 포인트가 최대 충전 포인트 한계값을 넘은 경우 예외가 발생한다.")
    @Test
    void chargeWithExceedPointLimit() {
        // given
        given(userPointRepository.findById(anyLong()))
                .willReturn(UserPoint.empty(1L));

        // when, then
        assertThatThrownBy(() -> pointService.charge(1L, 3_000_000L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("충전 가능한 포인트 최대값을 초과했습니다.");
    }

    @DisplayName("특정 유저의 포인트를 사용한다.")
    @Test
    void use() {
        // given
        given(userPointRepository.findById(anyLong()))
                .willReturn(new UserPoint(1L, 300L, System.currentTimeMillis()));
        given(userPointRepository.save(any(UserPoint.class)))
                .willReturn(new UserPoint(1L, 200L, System.currentTimeMillis()));

        // when
        UserPoint userPoint = pointService.use(1L, 100L);

        // then
        assertThat(userPoint).isNotNull();
        assertThat(userPoint).extracting("id", "point")
                .containsExactly(1L, 200L);
        verify(pointHistoryRepository).save(any(PointHistory.class));
    }

    @DisplayName("사용할 포인트가 부족한 경우 예외가 발생한다.")
    @Test
    void useWithInsufficientPoint() {
        // given
        given(userPointRepository.findById(anyLong()))
                .willReturn(UserPoint.empty(1L));

        // when, then
        assertThatThrownBy(() -> pointService.use(1L, 3_000_000L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("포인트가 부족합니다.");
    }
}