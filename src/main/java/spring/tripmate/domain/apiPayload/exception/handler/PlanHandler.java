package spring.tripmate.domain.apiPayload.exception.handler;

import spring.tripmate.domain.apiPayload.code.BaseErrorCode;
import spring.tripmate.domain.apiPayload.exception.GeneralException;

public class PlanHandler extends GeneralException {
    public PlanHandler(BaseErrorCode errorCode) {
        super(errorCode);
    }
}
