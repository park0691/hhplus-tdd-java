package io.hhplus.tdd.repository;

import io.hhplus.tdd.point.PointHistory;

import java.util.List;

public interface PointHistoryRepository {
    PointHistory save(PointHistory pointHistory);

    List<PointHistory> findById(long userId);
}
