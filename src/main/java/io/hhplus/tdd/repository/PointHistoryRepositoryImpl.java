package io.hhplus.tdd.repository;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.point.PointHistory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class PointHistoryRepositoryImpl implements PointHistoryRepository {

    private final PointHistoryTable pointHistoryTable;

    @Override
    public PointHistory save(PointHistory pointHistory) {
        return pointHistoryTable.insert(pointHistory.userId(), pointHistory.amount(), pointHistory.type(), pointHistory.updateMillis());
    }

    @Override
    public List<PointHistory> findById(long userId) {
        return pointHistoryTable.selectAllByUserId(userId);
    }
}
