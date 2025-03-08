package uk.gov.dwp.uc.pairtest.domain;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.TicketService;
import uk.gov.dwp.uc.pairtest.TicketServiceImpl;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class TicketServiceTest {

    private TicketService ticketService;
    private TicketPaymentService ticketPaymentService;
    private SeatReservationService seatReservationService;

    @Before
    public void setUp() {
        ticketPaymentService = Mockito.mock(TicketPaymentService.class);
        seatReservationService = Mockito.mock(SeatReservationService.class);
        ticketService = new TicketServiceImpl(ticketPaymentService, seatReservationService);
    }

    @Test
    public void should_throw_exception_when_negative_account_id(){
        boolean thrown = false;
        List<TicketTypeRequest> ticketTypeRequests = Arrays.asList(
                new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 5),
                new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 20)
        );

        try {
            ticketService.purchaseTickets(-1L, ticketTypeRequests.toArray(new TicketTypeRequest[]{}));
        } catch (InvalidPurchaseException e) {
            thrown = true;
            assertEquals("AccountId should be greater than zero", e.getMessage());
        }
        assertTrue(thrown);
    }

    @Test
    public void should_throw_exception_when_ticket_type_is_null(){
        boolean thrown = false;
        List<TicketTypeRequest> ticketTypeRequests = Arrays.asList(
                new TicketTypeRequest(null, 5),
                new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 20),
                new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 1)
        );

        try {
            ticketService.purchaseTickets(1L, ticketTypeRequests.toArray(new TicketTypeRequest[]{}));
        } catch (InvalidPurchaseException e) {
            thrown = true;
            assertEquals("Ticket Request type should not be null", e.getMessage());
        }
        assertTrue(thrown);
    }

    @Test
    public void should_throw_exception_when_number_of_tickets_is_negative_or_zero(){
        boolean thrown = false;
        List<TicketTypeRequest> ticketTypeRequests = Arrays.asList(
                new TicketTypeRequest(TicketTypeRequest.Type.ADULT, -5),
                new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 20),
                new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 1)
        );

        try {
            ticketService.purchaseTickets(1L, ticketTypeRequests.toArray(new TicketTypeRequest[]{}));
        } catch (InvalidPurchaseException e) {
            thrown = true;
            assertEquals("Ticket Requests should have positive and greater than zero number of tickets", e.getMessage());
        }
        assertTrue(thrown);
    }

    @Test
    public void should_throw_exception_when_number_of_tickets_exceeds_25(){
        boolean thrown = false;
        List<TicketTypeRequest> ticketTypeRequests = Arrays.asList(
                new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 5),
                new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 20),
                new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 1)
        );

        try {
            ticketService.purchaseTickets(1L, ticketTypeRequests.toArray(new TicketTypeRequest[]{}));
        } catch (InvalidPurchaseException e) {
            thrown = true;
            assertEquals("Only 25 tickets are allowed", e.getMessage());
        }
        assertTrue(thrown);
    }

    @Test
    public void should_throw_exception_when_no_adult_tickets_are_bought(){
        boolean thrown = false;

        try {
            ticketService.purchaseTickets(1L,
                    new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 2));
        } catch (InvalidPurchaseException e) {
            thrown = true;
        }
        assertTrue(thrown);
    }

    @Test
    public void should_calculate_correctly_the_amount_to_pay(){

        List<TicketTypeRequest> ticketTypeRequests = Arrays.asList(
                new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 4),
                new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 20),
                new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 1)
        );
        ArgumentCaptor<Integer> amountCaptor = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Long> accountCaptor = ArgumentCaptor.forClass(Long.class);


        ticketService.purchaseTickets(1L, ticketTypeRequests.toArray(new TicketTypeRequest[]{}));
        Mockito.verify(ticketPaymentService).makePayment(accountCaptor.capture(), amountCaptor.capture());

        assertEquals(400, amountCaptor.getValue().intValue());
        assertEquals(1L, accountCaptor.getValue().longValue());

    }

    @Test
    public void should_calculate_correctly_the_number_of_seats(){

        List<TicketTypeRequest> ticketTypeRequests = Arrays.asList(
                new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 4),
                new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 20),
                new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 1)
        );
        ArgumentCaptor<Integer> seatsCaptor = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Long> accountCaptor = ArgumentCaptor.forClass(Long.class);


        ticketService.purchaseTickets(1L, ticketTypeRequests.toArray(new TicketTypeRequest[]{}));
        Mockito.verify(seatReservationService).reserveSeat(accountCaptor.capture(), seatsCaptor.capture());

        assertEquals(24, seatsCaptor.getValue().intValue());
        assertEquals(1L, accountCaptor.getValue().longValue());

    }

}