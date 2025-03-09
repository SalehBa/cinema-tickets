package uk.gov.dwp.uc.pairtest;

import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

import java.util.Arrays;
import java.util.Map;

public class TicketServiceImpl implements TicketService {
    public static final int MAX_TICKETS_PER_PURCHACE = 25;
    public static final Map<TicketTypeRequest.Type, Integer> TICKETS_PRICES =
            Map.of(
                    TicketTypeRequest.Type.ADULT, 25,
                    TicketTypeRequest.Type.CHILD, 15,
                    TicketTypeRequest.Type.INFANT, 0
            );

    /**
     * Should only have private methods other than the one below.
     */

    private TicketPaymentService ticketPaymentService;
    private SeatReservationService seatReservationService;

    public TicketServiceImpl(TicketPaymentService ticketPaymentService,
                             SeatReservationService seatReservationService) {
        this.ticketPaymentService = ticketPaymentService;
        this.seatReservationService = seatReservationService;
    }

    @Override
    public void purchaseTickets(Long accountId, TicketTypeRequest... ticketTypeRequests) throws InvalidPurchaseException {
        validateAccount(accountId);
        validatePurchase(ticketTypeRequests);
        processPurchace(accountId, ticketTypeRequests);

    }

    private void validateAccount(Long accountId) {
        if (accountId <= 0) {
            throw new InvalidPurchaseException("AccountId should be greater than zero");
        }
    }

    private void validatePurchase(TicketTypeRequest... ticketTypeRequests) throws InvalidPurchaseException {
        // All ticket requests should have a valid and not null type
        if (Arrays.stream(ticketTypeRequests).anyMatch(ticketTypeRequest -> ticketTypeRequest.getTicketType() == null)) {
            throw new InvalidPurchaseException("Ticket Request type should not be null");
        }

        // All ticket requests should have non negative and greater than zero number of tickets
        if (Arrays.stream(ticketTypeRequests).anyMatch(ticketTypeRequest -> ticketTypeRequest.getNoOfTickets() <= 0)) {
            throw new InvalidPurchaseException("Ticket Requests should have positive and greater than zero number of tickets");
        }

        // 25 ticket Max
        if (Arrays.stream(ticketTypeRequests).mapToInt(TicketTypeRequest::getNoOfTickets).sum() > MAX_TICKETS_PER_PURCHACE) {
            throw new InvalidPurchaseException(String.format("Only %s tickets are allowed", MAX_TICKETS_PER_PURCHACE));
        }

        // Child and Infant tickets cannot be purchased without purchasing an Adult ticket.
        if (Arrays.stream(ticketTypeRequests).noneMatch(ticketTypeRequest ->
                TicketTypeRequest.Type.ADULT.equals(ticketTypeRequest.getTicketType()))) {
            throw new InvalidPurchaseException("Child and Infant tickets cannot be purchased without purchasing an Adult ticket");
        }

        // Infant should sit on an Adult Lap so number of infant should not exceed the number of adults
        int adultsTickets = getNoOfTicketsByType(ticketTypeRequests, TicketTypeRequest.Type.ADULT);
        int infantTickets = getNoOfTicketsByType(ticketTypeRequests, TicketTypeRequest.Type.INFANT);

        if (infantTickets > adultsTickets) {
            throw new InvalidPurchaseException("The number of Infants should not exceed the number of adults");
        }
    }


    private void processPurchace(Long accountId, TicketTypeRequest[] ticketTypeRequests) {
        int totalAmountToPay = Arrays.stream(ticketTypeRequests)
                .mapToInt(ticketTypeRequest -> ticketTypeRequest.getTotalPrice(TICKETS_PRICES))
                .sum();
        ticketPaymentService.makePayment(accountId, totalAmountToPay);

        int totalSeatsToAllocate = Arrays.stream(ticketTypeRequests).mapToInt(TicketTypeRequest::getSeatsToAllocate).sum();
        seatReservationService.reserveSeat(accountId, totalSeatsToAllocate);
    }


    private int getNoOfTicketsByType(TicketTypeRequest[] ticketTypeRequests, TicketTypeRequest.Type type) {
        return Arrays.stream(ticketTypeRequests)
                .filter(ticket -> type.equals(ticket.getTicketType()))
                .mapToInt(TicketTypeRequest::getNoOfTickets)
                .sum();
    }


}
