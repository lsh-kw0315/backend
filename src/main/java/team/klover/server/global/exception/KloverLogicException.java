package team.klover.server.global.exception;

public class KloverLogicException extends KloverException{
  public KloverLogicException(ReturnCode returnCode) {
    super(returnCode);
  }
}
