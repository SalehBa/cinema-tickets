package uk.gov.dwp.uc.pairtest.domain;

import java.util.Map;

/**
 * Immutable Object
 */

public final class TicketTypeRequest {

    private static final Map<Type, Integer> TICKETS_PRICES =
            Map.of(
                    TicketTypeRequest.Type.ADULT, 25,
                    TicketTypeRequest.Type.CHILD, 15,
                    TicketTypeRequest.Type.INFANT, 0
            );
    private final int noOfTickets;
    private final Type type;

    public TicketTypeRequest(Type type, int noOfTickets) {
        this.type = type;
        this.noOfTickets = noOfTickets;
    }

    public int getNoOfTickets() {
        return noOfTickets;
    }

    public Type getTicketType() {
        return type;
    }

    public int getTotalPrice(){
        return noOfTickets * TICKETS_PRICES.get(type);
    }
    public int getSeatsToAllocate(){
        switch (type){
            case ADULT:
            case CHILD:
                return noOfTickets;
            case INFANT:
            default:
                return 0;
        }
    }

    public enum Type {
        ADULT, CHILD , INFANT
    }

}
