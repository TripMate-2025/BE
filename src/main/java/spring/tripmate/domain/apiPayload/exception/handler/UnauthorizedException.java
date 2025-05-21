package spring.tripmate.domain.apiPayload.exception.handler;

import spring.tripmate.domain.apiPayload.code.BaseErrorCode;
import spring.tripmate.domain.apiPayload.exception.GeneralException;

public class UnauthorizedException extends GeneralException {
    public UnauthorizedException(BaseErrorCode errorCode) {
        super(errorCode);
    }
}