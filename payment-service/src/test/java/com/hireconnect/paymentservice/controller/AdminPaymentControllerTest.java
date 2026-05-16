package com.hireconnect.paymentservice.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.core.MethodParameter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import com.hireconnect.paymentservice.entity.Payment;
import com.hireconnect.paymentservice.enums.Role;
import com.hireconnect.paymentservice.repository.PaymentRepository;
import com.hireconnect.paymentservice.security.AuthenticatedUser;

class AdminPaymentControllerTest {

    private MockMvc mockMvc;

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private AdminPaymentController adminPaymentController;

    private AuthenticatedUser adminUser;
    private AuthenticatedUser candidateUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        adminUser = new AuthenticatedUser(1L, "admin@test.com", Role.ADMIN);
        candidateUser = new AuthenticatedUser(2L, "candidate@test.com", Role.CANDIDATE);

        mockMvc = createMockMvcWithUser(adminUser);
    }

    @Test
    void getAllTransactions_Success() throws Exception {
        when(paymentRepository.findAll()).thenReturn(List.of(new Payment()));

        mockMvc.perform(get("/api/payments/admin/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getAllTransactions_Forbidden() throws Exception {
        MockMvc candidateMvc = createMockMvcWithUser(candidateUser);
        candidateMvc.perform(get("/api/payments/admin/transactions"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getFinancialStats_Success() throws Exception {
        when(paymentRepository.sumTotalRevenue()).thenReturn(new BigDecimal("1000.00"));
        when(paymentRepository.countSuccessfulPayments()).thenReturn(10L);

        mockMvc.perform(get("/api/payments/admin/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRevenue").value(1000.00))
                .andExpect(jsonPath("$.successCount").value(10));
    }

    @Test
    void getFinancialStats_NullRevenue_ShouldReturnZero() throws Exception {
        when(paymentRepository.sumTotalRevenue()).thenReturn(null);
        when(paymentRepository.countSuccessfulPayments()).thenReturn(0L);

        mockMvc.perform(get("/api/payments/admin/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRevenue").value(0))
                .andExpect(jsonPath("$.successCount").value(0));
    }

    private MockMvc createMockMvcWithUser(AuthenticatedUser user) {
        return MockMvcBuilders.standaloneSetup(adminPaymentController)
                .setCustomArgumentResolvers(new HandlerMethodArgumentResolver() {
                    @Override
                    public boolean supportsParameter(MethodParameter p) { return true; }
                    @Override
                    public Object resolveArgument(MethodParameter p, ModelAndViewContainer m, NativeWebRequest w, WebDataBinderFactory f) {
                        return user;
                    }
                }).build();
    }
}
