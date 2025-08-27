package com.invoice.system.invoice_system.controller;

import com.invoice.system.invoice_system.TestContainerInitializer;
import com.invoice.system.invoice_system.dto.InvoiceRequest;
import com.invoice.system.invoice_system.dto.PaymentRequest;
import com.invoice.system.invoice_system.dto.ProcessOverdueRequest;
import com.invoice.system.invoice_system.util.ObjectMapperUtil;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

// Test cases are written based on considering today's date as 2025-08-27
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class InvoiceControllerTest extends TestContainerInitializer {

    @Autowired
    private MockMvc mockMvc;

    private static final LocalDate TODAY_DATE = LocalDate.of(2025, 8, 27);

    @Test
    @Order(1)
        // Past due_date
    void saveInvoice1() throws Exception {
        InvoiceRequest invoiceRequest = new InvoiceRequest(BigDecimal.valueOf(200), LocalDate.of(2025, 8, 10));
        mockMvc.perform(MockMvcRequestBuilders.post("/invoices")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(ObjectMapperUtil.writeValueAsString(invoiceRequest))
                ).andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(jsonPath("$.id").value(1));  // Ensure 'id' is returned

        // Validation
        final String expectedOutput = new String(Files.readAllBytes(Path.of(Objects.requireNonNull(getClass().getClassLoader().getResource("expectedOutput/getAllInvoices-1.json")).toURI())));

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/invoices")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                ).andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        JSONAssert.assertEquals(mvcResult.getResponse().getContentAsString(), expectedOutput, false);
    }

    @Test
    @Order(2)
        // Future due_date
    void saveInvoice2() throws Exception {
        InvoiceRequest invoiceRequest = new InvoiceRequest(BigDecimal.valueOf(250), LocalDate.of(2025, 9, 10));
        mockMvc.perform(MockMvcRequestBuilders.post("/invoices")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(ObjectMapperUtil.writeValueAsString(invoiceRequest))
                ).andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(jsonPath("$.id").value(2));  // Ensure 'id' is returned

        // Validation
        final String expectedOutput = new String(Files.readAllBytes(Path.of(Objects.requireNonNull(getClass().getClassLoader().getResource("expectedOutput/getAllInvoices-2.json")).toURI())));

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/invoices")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                ).andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        JSONAssert.assertEquals(mvcResult.getResponse().getContentAsString(), expectedOutput, false);
    }

    @Test
    @Order(3)
        // Past due_date
    void saveInvoice3() throws Exception {
        InvoiceRequest invoiceRequest = new InvoiceRequest(BigDecimal.valueOf(300), LocalDate.of(2025, 8, 15));
        mockMvc.perform(MockMvcRequestBuilders.post("/invoices")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(ObjectMapperUtil.writeValueAsString(invoiceRequest))
                ).andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(jsonPath("$.id").value(3));  // Ensure 'id' is returned

        // Validation
        final String expectedOutput = new String(Files.readAllBytes(Path.of(Objects.requireNonNull(getClass().getClassLoader().getResource("expectedOutput/getAllInvoices-3.json")).toURI())));

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/invoices")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                ).andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        JSONAssert.assertEquals(mvcResult.getResponse().getContentAsString(), expectedOutput, false);
    }

    @Test
    @Order(4)
        // make partial payment for past dated due date for ID 1
    void makePayments1() throws Exception {
        final PaymentRequest paymentRequest = new PaymentRequest(BigDecimal.valueOf(30));
        mockMvc.perform(MockMvcRequestBuilders.post("/invoices/1/payment")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(ObjectMapperUtil.writeValueAsString(paymentRequest))
                ).andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        // Validation
        final String expectedOutput = new String(Files.readAllBytes(Path.of(Objects.requireNonNull(getClass().getClassLoader().getResource("expectedOutput/getAllInvoices-4.json")).toURI())));

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/invoices")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                ).andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        JSONAssert.assertEquals(mvcResult.getResponse().getContentAsString(), expectedOutput, false);
    }

    @Test
    @Order(5)
        // Process overdue
    void processOverdue1() throws Exception {
        ProcessOverdueRequest overdueRequest = new ProcessOverdueRequest(BigDecimal.valueOf(25), 10);
        mockMvc.perform(MockMvcRequestBuilders.post("/invoices/process-overdue?overDueDate=%s".formatted(TODAY_DATE))
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(ObjectMapperUtil.writeValueAsString(overdueRequest))
                ).andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        // Validation
        final String expectedOutput = new String(Files.readAllBytes(Path.of(Objects.requireNonNull(getClass().getClassLoader().getResource("expectedOutput/getAllInvoices-5.json")).toURI())));

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/invoices")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                ).andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        JSONAssert.assertEquals(mvcResult.getResponse().getContentAsString(), expectedOutput, false);
    }

    @Test
    @Order(6)
        // making payment on old invoices which are in partial paid and Marked void // Make more payment allowed on active
    void makePayments2() throws Exception {
        final PaymentRequest paymentRequest = new PaymentRequest(BigDecimal.valueOf(30));
        // Invoice Id 1
        mockMvc.perform(MockMvcRequestBuilders.post("/invoices/1/payment")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(ObjectMapperUtil.writeValueAsString(paymentRequest))
                ).andExpect(MockMvcResultMatchers.status().isConflict())
                .andExpect(jsonPath("$.error").value("Invoice Inactive"))
                .andExpect(jsonPath("$.message").value("Attempted to process payment on inactive invoice: 1"));

        // Invoice ID 3
        mockMvc.perform(MockMvcRequestBuilders.post("/invoices/3/payment")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(ObjectMapperUtil.writeValueAsString(paymentRequest))
                ).andExpect(MockMvcResultMatchers.status().isConflict())
                .andExpect(jsonPath("$.error").value("Invoice Inactive"))
                .andExpect(jsonPath("$.message").value("Attempted to process payment on inactive invoice: 3"));

        // Validation
        final String expectedOutput = new String(Files.readAllBytes(Path.of(Objects.requireNonNull(getClass().getClassLoader().getResource("expectedOutput/getAllInvoices-5.json")).toURI())));

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/invoices")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                ).andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        JSONAssert.assertEquals(mvcResult.getResponse().getContentAsString(), expectedOutput, false);
    }

    @Test
    @Order(7)
        // get only the active invoices
    void getAllInvoices7() throws Exception {
        final String expectedOutput = new String(Files.readAllBytes(Path.of(Objects.requireNonNull(getClass().getClassLoader().getResource("expectedOutput/getAllInvoices-6-isActive-true.json")).toURI())));

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/invoices?isActive=true")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                ).andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        JSONAssert.assertEquals(mvcResult.getResponse().getContentAsString(), expectedOutput, false);
    }

    @Test
    @Order(8)
        // get only the InActive invoices
    void getAllInvoices8() throws Exception {
        final String expectedOutput = new String(Files.readAllBytes(Path.of(Objects.requireNonNull(getClass().getClassLoader().getResource("expectedOutput/getAllInvoices-7-isActive-false.json")).toURI())));

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/invoices?isActive=false")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                ).andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        JSONAssert.assertEquals(mvcResult.getResponse().getContentAsString(), expectedOutput, false);
    }

    @Test
    @Order(9)
        // Process overdue once again to check if new invoices will be created again for already created one
    void processOverdue2() throws Exception {
        ProcessOverdueRequest overdueRequest = new ProcessOverdueRequest(BigDecimal.valueOf(25), 10);
        mockMvc.perform(MockMvcRequestBuilders.post("/invoices/process-overdue")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(ObjectMapperUtil.writeValueAsString(overdueRequest))
                ).andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        // Validation
        final String expectedOutput = new String(Files.readAllBytes(Path.of(Objects.requireNonNull(getClass().getClassLoader().getResource("expectedOutput/getAllInvoices-5.json")).toURI())));

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/invoices")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                ).andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        JSONAssert.assertEquals(mvcResult.getResponse().getContentAsString(), expectedOutput, false);
    }

    @Test
    @Order(10)
        // making multiple payment on the same id 4
    void makeMultiplePayments() throws Exception {
        final PaymentRequest paymentRequest = new PaymentRequest(BigDecimal.valueOf(30));
        // Invoice Id 4
        mockMvc.perform(MockMvcRequestBuilders.post("/invoices/4/payment")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(ObjectMapperUtil.writeValueAsString(paymentRequest))
                ).andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        // Invoice ID 4
        mockMvc.perform(MockMvcRequestBuilders.post("/invoices/4/payment")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(ObjectMapperUtil.writeValueAsString(paymentRequest))
                ).andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        // Invoice ID 4
        mockMvc.perform(MockMvcRequestBuilders.post("/invoices/4/payment")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(ObjectMapperUtil.writeValueAsString(paymentRequest))
                ).andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        // Validation
        final String expectedOutput = new String(Files.readAllBytes(Path.of(Objects.requireNonNull(getClass().getClassLoader().getResource("expectedOutput/getAllInvoices-8.json")).toURI())));

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/invoices")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                ).andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        JSONAssert.assertEquals(mvcResult.getResponse().getContentAsString(), expectedOutput, false);
    }

    @Test
    @Order(11)
        // make over payment on ID 5
    void makeOverPayment() throws Exception {
        final PaymentRequest paymentRequest = new PaymentRequest(BigDecimal.valueOf(60.05));
        // Invoice Id 5
        mockMvc.perform(MockMvcRequestBuilders.post("/invoices/5/payment")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(ObjectMapperUtil.writeValueAsString(paymentRequest))
                ).andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        // Invoice ID 5
        final PaymentRequest paymentRequest1 = new PaymentRequest(BigDecimal.valueOf(70.05));
        mockMvc.perform(MockMvcRequestBuilders.post("/invoices/5/payment")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(ObjectMapperUtil.writeValueAsString(paymentRequest1))
                ).andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        // Invoice ID 5
        final PaymentRequest paymentRequest2 = new PaymentRequest(BigDecimal.valueOf(80.005));
        mockMvc.perform(MockMvcRequestBuilders.post("/invoices/5/payment")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(ObjectMapperUtil.writeValueAsString(paymentRequest2))
                ).andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        // Invoice ID 5
        final PaymentRequest paymentRequest3 = new PaymentRequest(BigDecimal.valueOf(80.005));
        mockMvc.perform(MockMvcRequestBuilders.post("/invoices/5/payment")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(ObjectMapperUtil.writeValueAsString(paymentRequest3))
                ).andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        // Validation
        final String expectedOutput = new String(Files.readAllBytes(Path.of(Objects.requireNonNull(getClass().getClassLoader().getResource("expectedOutput/getAllInvoices-9.json")).toURI())));

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/invoices")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                ).andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        JSONAssert.assertEquals(mvcResult.getResponse().getContentAsString(), expectedOutput, false);
    }

    @Test
    @Order(12)
        // get all payments made
    void getAllPayments() throws Exception {
        final String expectedOutput = new String(Files.readAllBytes(Path.of(Objects.requireNonNull(getClass().getClassLoader().getResource("expectedOutput/getAllInvoices-8.json")).toURI())));

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/invoices/payments")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(List.of().toString())
                ).andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        final List<Map> paymentDataList = ObjectMapperUtil.readValue(contentAsString, Map.class, List.class);
        Assertions.assertThat(paymentDataList).hasSize(3);

    }
}