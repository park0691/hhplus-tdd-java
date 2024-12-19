package io.hhplus.tdd.repository;

import io.hhplus.tdd.point.UserPoint;

public interface UserPointRepository {

    UserPoint save(UserPoint userPoint);

    UserPoint findById(long id);
}
