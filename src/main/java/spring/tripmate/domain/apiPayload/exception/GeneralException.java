package spring.tripmate.domain.apiPayload.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import spring.tripmate.domain.apiPayload.code.BaseErrorCode;
import spring.tripmate.domain.apiPayload.code.ErrorReasonDTO;

@Getter
@AllArgsConstructor
public class GeneralException extends RuntimeException {

  private BaseErrorCode code; //BaseErrorCode를 구현한 Enum 타입의 객체 (에러 코드 및 상태 정보)

  //예외 발생 시, 에러 상태 및 메시지만 담은 ErrorReasonDTO를 반환
  public ErrorReasonDTO getErrorReason() {
    return this.code.getReason();
  }

  //예외 발생 시, HTTP 상태 코드까지 포함한 ErrorReasonDTO를 반환
  public ErrorReasonDTO getErrorReasonHttpStatus(){
    return this.code.getReasonHttpStatus();
  }
}
