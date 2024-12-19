package io.hhplus.tdd.repository;

import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.UserPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserPointRepositoryImpl implements UserPointRepository {

    private final UserPointTable userPointTable;

    @Override
    public UserPoint save(UserPoint userPoint) {
        return userPointTable.insertOrUpdate(userPoint.id(), userPoint.point());
    }

    @Override
    public UserPoint findById(long id) {
        return userPointTable.selectById(id);
    }
}
