package com.xebialabs.overcast.command;

/**
 * Thrown when {@link Command} returns non zero code
 */
public class NonZeroCodeException extends RuntimeException {

    private Command command;

    private CommandResponse response;


    public NonZeroCodeException(final Command command, final CommandResponse response) {
        super("Command " + command.toString() + " returned non-zero code " + response.getReturnCode());
        this.command = command;
        this.response = response;
    }

}
