package spring.tripmate.domain.apiPayload.code.status;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import spring.tripmate.domain.apiPayload.code.BaseCode;
import spring.tripmate.domain.apiPayload.code.ReasonDTO;

@Getter
@RequiredArgsConstructor
public enum SuccessStatus implements BaseCode {
    // enum 상수
    _OK(HttpStatus.OK, "200 Request", "요청입니다.");

    //필드
    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    @Override
    public ReasonDTO getReason() { //httpStatus 없이 성공 코드와 메시지 전달
        return ReasonDTO.builder()
                .isSuccess(true) //성공응답이므로 true고정
                .code(code)
                .message(message)
                .build();
    }
    @Override
    public ReasonDTO getReasonHttpStatus() { //httpStatus도 같이 전달
        return ReasonDTO.builder()
                .isSuccess(true) //성공응답이므로 true고정
                .code(code)
                .message(message)
                .httpStatus(httpStatus) //getReason()에서 이부분만 추가
                .build();
    }
}
