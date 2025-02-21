package team.klover.server.global.exception;

import lombok.Getter;

@Getter
public class KloverException extends RuntimeException {

  private ReturnCode returnCode;
  private String returnMessage;

  public KloverException(ReturnCode returnCode) {
    this.returnCode = returnCode;
    this.returnMessage = returnCode.getReturnMessage();
  }
}
