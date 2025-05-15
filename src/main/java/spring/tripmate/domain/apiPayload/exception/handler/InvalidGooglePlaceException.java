package spring.tripmate.domain.apiPayload.exception.handler;

import spring.tripmate.domain.apiPayload.code.BaseErrorCode;
import spring.tripmate.domain.apiPayload.exception.GeneralException;

public class InvalidGooglePlaceException extends GeneralException {
    public InvalidGooglePlaceException(BaseErrorCode errorCode) {
        super(errorCode);
    }
}
