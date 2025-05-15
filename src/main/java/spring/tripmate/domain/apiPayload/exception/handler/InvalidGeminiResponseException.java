package spring.tripmate.domain.apiPayload.exception.handler;

import spring.tripmate.domain.apiPayload.code.BaseErrorCode;
import spring.tripmate.domain.apiPayload.exception.GeneralException;

public class InvalidGeminiResponseException extends GeneralException {
    public InvalidGeminiResponseException(BaseErrorCode errorCode) {
        super(errorCode);
    }
}
