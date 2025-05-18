package spring.tripmate.domain.apiPayload.exception.handler;

import spring.tripmate.domain.apiPayload.code.BaseErrorCode;
import spring.tripmate.domain.apiPayload.exception.GeneralException;

public class GeminiCallFailedException extends GeneralException {
    public GeminiCallFailedException(BaseErrorCode errorCode) {
        super(errorCode);
    }
}
