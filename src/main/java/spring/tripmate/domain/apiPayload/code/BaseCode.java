package spring.tripmate.domain.apiPayload.code;

public interface BaseCode {  //ReasonErrorDTO 객체 반환하는 두 메서드 정의
    ReasonDTO getReason(); // 상태 코드 및 메시지 반환
    ReasonDTO getReasonHttpStatus(); // HttpStatus 포함 상태 반환
}
