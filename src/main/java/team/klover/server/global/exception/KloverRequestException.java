package team.klover.server.global.exception;

public class KloverRequestException extends KloverException {
  public KloverRequestException(ReturnCode returnCode) {
    super(returnCode);
  }
}
