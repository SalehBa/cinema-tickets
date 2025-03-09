package uk.gov.dwp.uc.pairtest.domain;

import java.util.Map;


/**
 * Immutable Object
 */

public final class TicketTypeRequest {

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

    public int getTotalPrice(Map<TicketTypeRequest.Type, Integer> currentPrices){
        return noOfTickets * currentPrices.get(type);
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
