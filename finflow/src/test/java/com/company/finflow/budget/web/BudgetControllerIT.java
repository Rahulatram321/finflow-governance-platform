package com.company.finflow.budget.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@Disabled("Template integration test. Configure test profile and database before enabling.")
@SpringBootTest
@AutoConfigureMockMvc
class BudgetControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void listBudgetsTemplate() throws Exception {
        mockMvc.perform(get("/api/budgets").header("X-Tenant-Id", 1L))
            .andExpect(status().isOk());
    }
}
