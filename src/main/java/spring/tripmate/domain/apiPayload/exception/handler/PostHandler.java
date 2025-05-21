package spring.tripmate.domain.apiPayload.exception.handler;

import spring.tripmate.domain.apiPayload.code.BaseErrorCode;
import spring.tripmate.domain.apiPayload.exception.GeneralException;

public class PostHandler extends GeneralException {
    public PostHandler(BaseErrorCode errorCode) {
        super(errorCode);
    }
}
