package team.klover.server.global.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import team.klover.server.global.exception.ReturnCode;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private String returnCode;
    private String returnMessage;
    private T data;
    private KloverPage<T> kloverPage;

    public static <T> ApiResponse of(T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.returnCode = ReturnCode.SUCCESS.getReturnCode();
        response.returnMessage = ReturnCode.SUCCESS.getReturnMessage();
        response.data = data;

        return response;
    }

    public static <T> ApiResponse<T> of(KloverPage<T> kloverPage) {
        ApiResponse<T> response = new ApiResponse<>();
        response.returnCode = ReturnCode.SUCCESS.getReturnCode();
        response.returnMessage = ReturnCode.SUCCESS.getReturnMessage();
        response.kloverPage = kloverPage;

        return response;
    }

    public static <T> ApiResponse of(ReturnCode returnCode) {
        ApiResponse<T> response = new ApiResponse<>();
        response.returnCode = returnCode.getReturnCode();
        response.returnMessage = returnCode.getReturnMessage();

        return response;
    }
}
