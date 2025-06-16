package spring.tripmate.domain.apiPayload.exception.handler;

import spring.tripmate.domain.apiPayload.code.status.ErrorStatus;

public class RoomHandler extends RuntimeException {
    public RoomHandler(ErrorStatus message) {
        super(String.valueOf(message));
    }
}
