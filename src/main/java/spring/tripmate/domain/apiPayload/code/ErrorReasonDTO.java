package spring.tripmate.domain.apiPayload.code;

import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@Builder
public class ErrorReasonDTO { //에러 응답데이터를 담는 DTO

    private HttpStatus httpStatus;  //Spring에서 제공하는 HTTP 상태 코드 (Enum)
    private final boolean isSuccess;//성공여부
    private final String code;      //에러코드
    private final String message;   //에러 설명(에러코드보다 자세히 설명)
}
