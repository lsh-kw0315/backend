package team.klover.server.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ReturnCode {
    SUCCESS("0000", "Success"),

    WRONG_PARAMETER("4000", "Wrong parameter"),
    NOT_FOUND_ENTITY("4001", "Not found entity"),
    ALREADY_EXIST("4002", "Already exist"),
    WRONG_PASSWORD("4003", "Wrong password"),
    NOT_AUTHORIZED("4004", "Not authorized"),
    EXPIRED_TOKEN("4005", "Expired token"),
    INVALID_REQUEST("4006", "Invalid Request"),

    INTERNAL_ERROR("5000", "Unexpected internal error");

    private String returnCode;
    private String returnMessage;
}