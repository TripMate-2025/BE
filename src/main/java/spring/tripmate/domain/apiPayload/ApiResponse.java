package spring.tripmate.domain.apiPayload;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import spring.tripmate.domain.apiPayload.code.BaseCode;
import spring.tripmate.domain.apiPayload.code.ErrorReasonDTO;
import spring.tripmate.domain.apiPayload.code.status.SuccessStatus;

@Getter
@AllArgsConstructor
@JsonPropertyOrder({"isSuccess", "code", "message", "result"})
public class ApiResponse<T> {

    @JsonProperty("isSuccess")
    private final Boolean isSuccess;
    private final String code;
    private final String message;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private T result;


    //성공한 경우 응답 생성, 리턴
    //기본 성공응답
    public static <T> ApiResponse<T> onSuccess(T result){
        return new ApiResponse<>(true, SuccessStatus._OK.getCode() , SuccessStatus._OK.getMessage(), result);
    }
    //커스텀 성공응답(세부적인 성공코드와 메시지도 제공)
    public static <T> ApiResponse<T> of(BaseCode code, T result){
        return new ApiResponse<>(true, code.getReasonHttpStatus().getCode() , code.getReasonHttpStatus().getMessage(), result);
    }
    //실패한 경우 응답 생성, 리턴
    public static <T> ApiResponse<T> onFailure(String code, String message, T data){
        return new ApiResponse<>(false, code, message, data);
    }

    // ErrorReasonDTO를 받는 실패 응답 생성 메서드의 이름 변경
    public static <T> ApiResponse<T> fromErrorReason(ErrorReasonDTO errorReason) {
        return new ApiResponse<>(false, errorReason.getCode(), errorReason.getMessage(), null);
    }
}
