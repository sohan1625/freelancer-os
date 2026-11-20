package com.freelancer.freelanceros;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.freelancer.freelanceros.model.Client;
import com.freelancer.freelanceros.model.Frequency;
import com.freelancer.freelanceros.model.Invoice;
import com.freelancer.freelanceros.model.RecurringInvoice;
import com.freelancer.freelanceros.model.RecurringInvoiceRequest;
import com.freelancer.freelanceros.model.User;
import com.freelancer.freelanceros.model.Workspace;
import com.freelancer.freelanceros.repository.ClientRepository;
import com.freelancer.freelanceros.repository.InvoiceRepository;
import com.freelancer.freelanceros.repository.RecurringInvoiceRepository;
import com.freelancer.freelanceros.repository.UserRepository;
import com.freelancer.freelanceros.repository.WorkspaceRepository;
import com.freelancer.freelanceros.security.JwtService;
import com.freelancer.freelanceros.service.InvoiceScheduler;
import com.freelancer.freelanceros.service.RecurringInvoiceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TenantIsolationIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WorkspaceRepository workspaceRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private RecurringInvoiceRepository recurringInvoiceRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private InvoiceScheduler invoiceScheduler;

    @Autowired
    private RecurringInvoiceService recurringInvoiceService;

    @Autowired
    private ObjectMapper objectMapper;

    private User userA;
    private User userB;
    private Client clientA;
    private Client clientB;
    private Invoice invoiceA;
    private Invoice invoiceB;

    @BeforeEach
    void setUp() {
        String runId = UUID.randomUUID().toString();

        Workspace workspaceA = new Workspace();
        workspaceA.setName("Workspace A " + runId);
        workspaceA = workspaceRepository.saveAndFlush(workspaceA);

        Workspace workspaceB = new Workspace();
        workspaceB.setName("Workspace B " + runId);
        workspaceB = workspaceRepository.saveAndFlush(workspaceB);

        userA = createUser("user-a-" + runId, workspaceA);
        userB = createUser("user-b-" + runId, workspaceB);

        clientA = createClient("Client A", workspaceA);
        clientB = createClient("Client B", workspaceB);

        invoiceA = createInvoice(clientA, "123.45", "PAID");
        invoiceB = createInvoice(clientB, "987.65", "PAID");
    }

    @Test
    void usersCanListOnlyTheirOwnClientInvoices() throws Exception {
        mockMvc.perform(get("/api/clients/{clientId}/invoices", clientA.getId())
                        .with(authenticatedAs(userA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(invoiceA.getId()));

        mockMvc.perform(get("/api/clients/{clientId}/invoices", clientA.getId())
                        .with(authenticatedAs(userB)))
                .andExpect(status().isNotFound())
                .andExpect(content().string(not(containsString("123.45"))));

        mockMvc.perform(get("/api/clients/invoices")
                        .with(authenticatedAs(userA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(invoiceA.getId()));

        mockMvc.perform(get("/api/clients/invoices")
                        .with(authenticatedAs(userB)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(invoiceB.getId()))
                .andExpect(content().string(not(containsString("Client A"))));
    }

    @Test
    void invoicePaymentDeletionAndPdfAreWorkspaceScoped() throws Exception {
        Invoice pendingInvoiceA = createInvoice(clientA, "45.00", "PENDING");

        mockMvc.perform(patch("/api/clients/invoices/{invoiceId}/paid", pendingInvoiceA.getId())
                        .with(authenticatedAs(userB)))
                .andExpect(status().isNotFound());
        assertTrue(invoiceRepository.findById(pendingInvoiceA.getId()).orElseThrow().getStatus().equals("PENDING"));

        mockMvc.perform(delete("/api/clients/invoices/{invoiceId}", pendingInvoiceA.getId())
                        .with(authenticatedAs(userB)))
                .andExpect(status().isNotFound());
        assertTrue(invoiceRepository.existsById(pendingInvoiceA.getId()));

        mockMvc.perform(get("/api/clients/invoices/{invoiceId}/pdf", invoiceA.getId())
                        .with(authenticatedAs(userB)))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/clients/invoices/{invoiceId}/pdf", invoiceA.getId())
                        .with(authenticatedAs(userA)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(result -> {
                    byte[] pdf = result.getResponse().getContentAsByteArray();
                    assertTrue(pdf.length > 4);
                    assertTrue(pdf[0] == '%' && pdf[1] == 'P' && pdf[2] == 'D' && pdf[3] == 'F');
                });

        mockMvc.perform(patch("/api/clients/invoices/{invoiceId}/paid", pendingInvoiceA.getId())
                        .with(authenticatedAs(userA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAID"));

        mockMvc.perform(delete("/api/clients/invoices/{invoiceId}", pendingInvoiceA.getId())
                        .with(authenticatedAs(userA)))
                .andExpect(status().isNoContent());
        assertFalse(invoiceRepository.existsById(pendingInvoiceA.getId()));
    }

    @Test
    void invoiceCreationRejectsOtherWorkspaceClientAndAcceptsOwn() throws Exception {
        mockMvc.perform(post("/api/clients/{clientId}/invoices", clientB.getId())
                        .with(authenticatedAs(userA))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":25.00,\"status\":\"PENDING\"}"))
                .andExpect(status().isNotFound());

        mockMvc.perform(post("/api/clients/{clientId}/invoices", clientA.getId())
                        .with(authenticatedAs(userA))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":25.00,\"status\":\"PENDING\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(25.00));
    }

    @Test
    void dashboardRevenueAndStatisticsAreWorkspaceScoped() throws Exception {
        mockMvc.perform(get("/api/analytics").with(authenticatedAs(userA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalInvoices").value(1))
                .andExpect(jsonPath("$.revenueCollected").value(123.45))
                .andExpect(jsonPath("$.topClients[0].name").value("Client A"));

        mockMvc.perform(get("/api/analytics").with(authenticatedAs(userB)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalInvoices").value(1))
                .andExpect(jsonPath("$.revenueCollected").value(987.65))
                .andExpect(jsonPath("$.topClients[0].name").value("Client B"));

        mockMvc.perform(get("/api/analytics/client-risk/{clientId}", clientA.getId())
                        .with(authenticatedAs(userB)))
                .andExpect(status().isNotFound());
    }

    @Test
    void clientRiskUsesOnlyTheAuthenticatedWorkspace() throws Exception {
        createInvoice(clientA, "10.00", "OVERDUE");
        createInvoice(clientA, "20.00", "OVERDUE");
        createInvoice(clientB, "30.00", "OVERDUE");

        mockMvc.perform(get("/api/analytics/client-risk/{clientId}", clientA.getId())
                        .with(authenticatedAs(userA)))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        mockMvc.perform(get("/api/analytics/client-risk/{clientId}", clientA.getId())
                        .with(authenticatedAs(userB)))
                .andExpect(status().isNotFound());
    }

    @Test
    void recurringInvoiceCreationAndMutationsAreWorkspaceScoped() throws Exception {
        String request = objectMapper.writeValueAsString(recurringRequest(clientB.getId()));
        mockMvc.perform(post("/api/recurring-invoices")
                        .with(authenticatedAs(userA))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isNotFound());

        String ownRequest = objectMapper.writeValueAsString(recurringRequest(clientA.getId()));
        String response = mockMvc.perform(post("/api/recurring-invoices")
                        .with(authenticatedAs(userA))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ownRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.scheduledTime").value("10:30:00"))
                .andReturn().getResponse().getContentAsString();
        long recurringInvoiceId = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(patch("/api/recurring-invoices/{id}/time", recurringInvoiceId)
                        .with(authenticatedAs(userB))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"scheduledTime\":\"11:15:00\"}"))
                .andExpect(status().isNotFound());

        mockMvc.perform(patch("/api/recurring-invoices/{id}/time", recurringInvoiceId)
                        .with(authenticatedAs(userA))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"scheduledTime\":\"11:15:00\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.scheduledTime").value("11:15:00"));

        mockMvc.perform(patch("/api/recurring-invoices/{id}/deactivate", recurringInvoiceId)
                        .with(authenticatedAs(userB)))
                .andExpect(status().isNotFound());
        assertTrue(recurringInvoiceRepository.findById(recurringInvoiceId).orElseThrow().isActive());

        mockMvc.perform(delete("/api/recurring-invoices/{id}", recurringInvoiceId)
                        .with(authenticatedAs(userB)))
                .andExpect(status().isNotFound());
        assertTrue(recurringInvoiceRepository.existsById(recurringInvoiceId));

        mockMvc.perform(patch("/api/recurring-invoices/{id}/deactivate", recurringInvoiceId)
                        .with(authenticatedAs(userA)))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/recurring-invoices/{id}", recurringInvoiceId)
                        .with(authenticatedAs(userA)))
                .andExpect(status().isOk());
        assertFalse(recurringInvoiceRepository.existsById(recurringInvoiceId));
    }

    @Test
    void dueRecurringInvoicesGenerateOnceAdvanceAndSkipInactiveOrMismatchedSchedules() {
        LocalDateTime beforeSchedule = LocalDateTime.of(2026, 1, 15, 11, 29);
        LocalDate today = beforeSchedule.toLocalDate();
        RecurringInvoice valid = recurringInvoice(clientA, clientA.getWorkspace());
        valid.setDescription("Scheduled monthly work");
        valid.setAmount(new BigDecimal("75.50"));
        valid.setFrequency(Frequency.MONTHLY);
        valid.setNextRunDate(today);
        valid.setScheduledTime(LocalTime.of(11, 30));

        RecurringInvoice inactive = recurringInvoice(clientA, clientA.getWorkspace());
        inactive.setActive(false);
        inactive.setNextRunDate(today.minusDays(2));

        RecurringInvoice mismatched = recurringInvoice(clientB, clientA.getWorkspace());
        mismatched.setNextRunDate(today.minusDays(1));
        mismatched.setScheduledTime(LocalTime.of(10, 0));

        recurringInvoiceRepository.saveAndFlush(valid);
        recurringInvoiceRepository.saveAndFlush(inactive);
        recurringInvoiceRepository.saveAndFlush(mismatched);
        long invoicesBefore = invoiceRepository.count();

        invoiceScheduler.processRecurringInvoicesAt(beforeSchedule);
        assertEquals(invoicesBefore, invoiceRepository.count());

        LocalDateTime scheduledMoment = beforeSchedule.plusMinutes(1);
        invoiceScheduler.processRecurringInvoicesAt(scheduledMoment);

        assertTrue(invoiceRepository.count() == invoicesBefore + 1);
        Invoice generated = invoiceRepository
                .findByClientIdAndClientWorkspaceId(clientA.getId(), clientA.getWorkspace().getId())
                .stream()
                .filter(invoice -> "Scheduled monthly work".equals(invoice.getDescription()))
                .findFirst()
                .orElseThrow();
        assertEquals(new BigDecimal("75.50"), generated.getAmount());
        assertEquals(clientA.getId(), generated.getClient().getId());
        assertEquals("PENDING", generated.getStatus());
        assertEquals(scheduledMoment.toLocalDate(), generated.getIssueDate());
        assertEquals(scheduledMoment.toLocalDate().plusDays(14), generated.getDueDate());
        assertEquals(today.plusMonths(1), recurringInvoiceRepository.findById(valid.getId())
                .orElseThrow().getNextRunDate());
        assertEquals(today.minusDays(2), recurringInvoiceRepository.findById(inactive.getId())
                .orElseThrow().getNextRunDate());
        assertEquals(today.minusDays(1), recurringInvoiceRepository.findById(mismatched.getId())
                .orElseThrow().getNextRunDate());

        invoiceScheduler.processRecurringInvoicesAt(scheduledMoment);
        assertEquals(invoicesBefore + 1, invoiceRepository.count());

        assertTrue(invoiceRepository.findByClientIdAndClientWorkspaceId(
                clientB.getId(), clientB.getWorkspace().getId()).size() == 1);
    }

    @Test
    void overdueRecurringScheduleSendsOneInvoiceAndJumpsToNextFutureDate() {
        LocalDateTime now = LocalDateTime.of(2026, 4, 20, 12, 0);
        RecurringInvoice behind = recurringInvoice(clientA, clientA.getWorkspace());
        behind.setDescription("Catch-up work");
        behind.setFrequency(Frequency.MONTHLY);
        behind.setNextRunDate(LocalDate.of(2026, 1, 10));
        behind.setScheduledTime(LocalTime.of(9, 0));
        recurringInvoiceRepository.saveAndFlush(behind);
        long invoicesBefore = invoiceRepository.count();

        invoiceScheduler.processRecurringInvoicesAt(now);
        invoiceScheduler.processRecurringInvoicesAt(now.plusMinutes(1));

        assertEquals(invoicesBefore + 1, invoiceRepository.count());
        assertEquals(LocalDate.of(2026, 5, 10), recurringInvoiceRepository.findById(behind.getId())
                .orElseThrow().getNextRunDate());
    }

    @Test
    void recurringNextDateAdvancesAccordingToFrequency() {
        LocalDate date = LocalDate.of(2024, 1, 31);
        assertEquals(LocalDate.of(2024, 2, 7),
                recurringInvoiceService.nextDate(date, Frequency.WEEKLY));
        assertEquals(LocalDate.of(2024, 2, 29),
                recurringInvoiceService.nextDate(date, Frequency.MONTHLY));
        assertEquals(LocalDate.of(2025, 1, 31),
                recurringInvoiceService.nextDate(date, Frequency.YEARLY));
    }

    @Test
    void missingTenantAssociationsAndUnauthenticatedRequestsAreDenied() throws Exception {
        Client noWorkspaceClient = new Client();
        noWorkspaceClient.setName("No workspace");
        noWorkspaceClient = clientRepository.saveAndFlush(noWorkspaceClient);
        Invoice noClientInvoice = new Invoice();
        noClientInvoice.setAmount(new BigDecimal("1.00"));
        noClientInvoice.setStatus("PENDING");
        noClientInvoice = invoiceRepository.saveAndFlush(noClientInvoice);
        Invoice noWorkspaceInvoice = createInvoice(noWorkspaceClient, "2.00", "PENDING");

        mockMvc.perform(post("/api/clients/{clientId}/invoices", noWorkspaceClient.getId())
                        .with(authenticatedAs(userA))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":25.00}"))
                .andExpect(status().isNotFound());

        mockMvc.perform(patch("/api/clients/invoices/{invoiceId}/paid", noClientInvoice.getId())
                        .with(authenticatedAs(userA)))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/api/clients/invoices/{invoiceId}/pdf", noWorkspaceInvoice.getId())
                        .with(authenticatedAs(userA)))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/clients/{clientId}/invoices", clientA.getId()))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/clients/invoices/{invoiceId}/pdf", invoiceA.getId()))
                .andExpect(status().isForbidden());
    }

    private User createUser(String email, Workspace workspace) {
        User user = new User();
        user.setName(email);
        user.setEmail(email + "@example.test");
        user.setPassword("not-used");
        user.setWorkspace(workspace);
        return userRepository.saveAndFlush(user);
    }

    private Client createClient(String name, Workspace workspace) {
        Client client = new Client();
        client.setName(name);
        client.setWorkspace(workspace);
        return clientRepository.saveAndFlush(client);
    }

    private Invoice createInvoice(Client client, String amount, String status) {
        Invoice invoice = new Invoice();
        invoice.setClient(client);
        invoice.setAmount(new BigDecimal(amount));
        invoice.setStatus(status);
        invoice.setIssueDate(LocalDate.now());
        invoice.setDueDate(LocalDate.now().plusDays(14));
        return invoiceRepository.saveAndFlush(invoice);
    }

    private RecurringInvoiceRequest recurringRequest(Long clientId) {
        RecurringInvoiceRequest request = new RecurringInvoiceRequest();
        request.setClientId(clientId);
        request.setDescription("Monthly service");
        request.setAmount(new BigDecimal("50.00"));
        request.setFrequency(Frequency.MONTHLY);
        request.setStartDate(LocalDate.now().plusDays(1));
        request.setScheduledTime("10:30");
        return request;
    }

    private RecurringInvoice recurringInvoice(Client client, Workspace workspace) {
        RecurringInvoice recurringInvoice = new RecurringInvoice();
        recurringInvoice.setClient(client);
        recurringInvoice.setWorkspace(workspace);
        recurringInvoice.setDescription("Monthly service");
        recurringInvoice.setAmount(new BigDecimal("50.00"));
        recurringInvoice.setFrequency(Frequency.MONTHLY);
        recurringInvoice.setNextRunDate(LocalDate.now().minusDays(1));
        recurringInvoice.setActive(true);
        return recurringInvoice;
    }

    private RequestPostProcessor authenticatedAs(User user) {
        String token = jwtService.generateAccessToken(user);
        return request -> {
            request.addHeader("Authorization", "Bearer " + token);
            return request;
        };
    }
}
