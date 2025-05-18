package spring.tripmate.domain.apiPayload.code.status;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import spring.tripmate.domain.apiPayload.code.BaseErrorCode;
import spring.tripmate.domain.apiPayload.code.ErrorReasonDTO;

@Getter
@AllArgsConstructor
public enum ErrorStatus implements BaseErrorCode {

    // 가장 일반적인 응답 상수들
    _INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON500", "서버 에러, 관리자에게 문의 바랍니다."),
    _BAD_REQUEST(HttpStatus.BAD_REQUEST,"COMMON400","잘못된 요청입니다."),
    _UNAUTHORIZED(HttpStatus.UNAUTHORIZED,"COMMON401","인증이 필요합니다."),
    _FORBIDDEN(HttpStatus.FORBIDDEN, "COMMON403", "금지된 요청입니다."),
    _NOT_FOUND(HttpStatus.NOT_FOUND, "COMMON404", "요청한 리소스를 찾을 수 없습니다."),
    //TO DO : 개발하면서 필요한 에러 상수 추가하기
    GEMINI_API_CALL_FAILED(HttpStatus.BAD_GATEWAY, "GEMINI5001", "Gemini API 호출 실패"),
    INVALID_GEMINI_RESPONSE(HttpStatus.INTERNAL_SERVER_ERROR, "GEMINI5002", "Gemini 응답 파싱 실패"),

    INVALID_GOOGLE_PLACE(HttpStatus.BAD_REQUEST, "GOOGLE4001", "해당되는 장소를 찾을 수 없습니다."),
    INVALID_GOOGLE_RESPONSE(HttpStatus.INTERNAL_SERVER_ERROR, "GOOGLE5001", "Google Place 응답 파싱 실패"),

    PLAN_NOT_FOUND(HttpStatus.NOT_FOUND, "PLAN4001", "해당되는 계획이 없습니다.");

    //테스트 상수
    //TEMP_EXCEPTION(HttpStatus.BAD_REQUEST, "TEMP4001", "이거는 테스트");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    @Override
    public ErrorReasonDTO getReason() { //httpStatus 없이 에러 메시지와 코드만 반환하는 DTO 생성, 반환
        return ErrorReasonDTO.builder()
                .message(message)
                .code(code)
                .isSuccess(false) //에러 응답이므로 false 고정
                .build();
    }

    @Override
    public ErrorReasonDTO getReasonHttpStatus() { //HttpStatus까지 포함된 에러 DTO를 생성, 반환
        return ErrorReasonDTO.builder()
                .message(message)
                .code(code)
                .isSuccess(false) //에러 응답이므로 false 고정
                .httpStatus(httpStatus) //getReason()에 이부분만 추가
                .build();
    }
}
