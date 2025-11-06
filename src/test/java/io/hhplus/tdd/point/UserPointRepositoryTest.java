package io.hhplus.tdd.point;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class UserPointRepositoryTest {

    @Autowired
    UserPointRepository userPointRepository;

    @DisplayName("포인트 정보를 저장한다.")
    @Test
    void save() {
        // given
        UserPoint userPoint = new UserPoint(1L, 300L, 0);

        // when
        userPointRepository.save(userPoint);

        // then
        UserPoint foundUserPoint = userPointRepository.findById(1L);
        assertThat(foundUserPoint).extracting("id", "point")
                .containsExactly(1L, 300L);
    }

    @DisplayName("유저 아이디로 포인트 정보를 조회한다.")
    @Test
    void findById() {
        // given
        UserPoint userPoint = new UserPoint(2L, 300L, 0);
        userPointRepository.save(userPoint);

        // when
        UserPoint foundUserPoint = userPointRepository.findById(2L);

        // then
        assertThat(foundUserPoint).extracting("id", "point")
                .containsExactly(2L, 300L);
    }
}