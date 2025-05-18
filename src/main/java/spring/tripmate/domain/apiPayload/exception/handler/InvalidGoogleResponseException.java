package spring.tripmate.domain.apiPayload.exception.handler;

import spring.tripmate.domain.apiPayload.code.BaseErrorCode;
import spring.tripmate.domain.apiPayload.exception.GeneralException;

public class InvalidGoogleResponseException extends GeneralException {
    public InvalidGoogleResponseException(BaseErrorCode errorCode) {
        super(errorCode);
    }
}
