package io.hhplus.tdd.controller;

import io.hhplus.tdd.point.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PointController.class)
class PointControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    PointService pointService;

    @DisplayName("포인트를 조회한다.")
    @Test
    void point() throws Exception {

        given(pointService.getPoint(any(long.class)))
                .willReturn(new UserPoint(1L, 100L, System.currentTimeMillis()));

        mockMvc.perform(
                        get("/point/1")
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.point").exists())
                .andExpect(jsonPath("$.updateMillis").exists())
        ;
    }

    @DisplayName("포인트 충전/이용 내역을 조회한다.")
    @Test
    void history() throws Exception {
        given(pointService.getPointHistory(any(long.class)))
                .willReturn(List.of(
                        new PointHistory(1L, 1L, 1000, TransactionType.CHARGE, System.currentTimeMillis()),
                        new PointHistory(2L, 1L, 100, TransactionType.USE, System.currentTimeMillis())
                ));

        mockMvc.perform(
                        get("/point/1/histories")
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.*.id").exists())
                .andExpect(jsonPath("$.*.userId").exists())
                .andExpect(jsonPath("$.*.amount").exists())
                .andExpect(jsonPath("$.*.type").exists())
                .andExpect(jsonPath("$.*.updateMillis").exists())
        ;
    }

    @DisplayName("포인트를 충전한다.")
    @Test
    void charge() throws Exception {

        given(pointService.charge(any(long.class), any(long.class)))
                .willReturn(new UserPoint(1L, 100L, System.currentTimeMillis()));

        mockMvc.perform(
                        patch("/point/1/charge")
                                .content("100")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.point").exists())
                .andExpect(jsonPath("$.updateMillis").exists())
        ;
    }

    @DisplayName("포인트를 사용한다.")
    @Test
    void use() throws Exception {
        given(pointService.use(any(long.class), any(long.class)))
                .willReturn(new UserPoint(1L, 100L, System.currentTimeMillis()));

        mockMvc.perform(
                        patch("/point/1/use")
                                .content("100")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.point").exists())
                .andExpect(jsonPath("$.updateMillis").exists())
        ;
    }
}