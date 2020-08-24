package me.sjlee.crawling.controller;

import me.sjlee.crawling.constant.ScrappingConstant;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ScrappingControllerTest {

    @Autowired
    MockMvc mockMvc;

    @DisplayName("메인페이지 (index.html) 호출 테스트")
    @Test
    void mainPageTest() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk());
    }

    @DisplayName("사이트의 정보를 스크래핑하는 부분")
    @Test
    void getChartBySitename() throws Exception {
        mockMvc.perform(get("/chart/melon")
                    .characterEncoding("UTF-8")
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(content().string(CoreMatchers.containsString(ScrappingConstant.FAMOUS_TITLE)))
                .andExpect(content().string(CoreMatchers.containsString(ScrappingConstant.FAMOUS_ARTIST)))
                .andExpect(status().isOk())
                .andDo(print());
    }

}