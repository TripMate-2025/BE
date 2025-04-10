package spring.tripmate.domain.apiPayload.code;

public interface BaseErrorCode {    //ErrorReasonDTO 객체 반환하는 두 메서드 정의
    ErrorReasonDTO getReason(); // 에러 코드 및 메시지 반환
    ErrorReasonDTO getReasonHttpStatus(); // HttpStatus 포함 에러 상태 반환
}
