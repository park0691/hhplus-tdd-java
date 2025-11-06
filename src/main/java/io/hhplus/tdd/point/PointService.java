package io.hhplus.tdd.point;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PointService {

    private final PointHistoryRepository pointHistoryRepository;
    private final UserPointRepository userPointRepository;
    private static final long MAX_POINT_LIMIT = 1_000_000L;

    public UserPoint getPoint(long id) {
        return userPointRepository.findById(id);
    }

    public List<PointHistory> getPointHistory(long id) {
        return pointHistoryRepository.findById(id);
    }

    public synchronized UserPoint charge(long id, long amount) {
        UserPoint userPointCurrent = userPointRepository.findById(id);
        long point = userPointCurrent.point() + amount;
        if (point > MAX_POINT_LIMIT) {
            throw new IllegalArgumentException("충전 가능한 포인트 최대값을 초과했습니다.");
        }
        UserPoint userPointNew = userPointRepository.save(new UserPoint(userPointCurrent.id(), point, System.currentTimeMillis()));

        pointHistoryRepository.save(new PointHistory(0, id, amount, TransactionType.CHARGE, 0L));
        return userPointNew;
    }

    public synchronized UserPoint use(long id, long amount) {
        UserPoint userPointCurrent = userPointRepository.findById(id);
        long point = userPointCurrent.point() - amount;
        if (point < 0) {
            throw new IllegalArgumentException("포인트가 부족합니다.");
        }
        UserPoint userPointNew = userPointRepository.save(new UserPoint(userPointCurrent.id(), point, System.currentTimeMillis()));

        pointHistoryRepository.save(new PointHistory(0, id, amount, TransactionType.USE, 0L));
        return userPointNew;
    }
}
