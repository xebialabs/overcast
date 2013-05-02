package com.xebialabs.overcast.command;

public class CommandResponse {

    private int returnCode;

    private String errors;

    private String output;

    public CommandResponse(int returnCode, String errors, String output) {
        this.returnCode = returnCode;
        this.errors = errors;
        this.output = output;
    }

    public int getReturnCode() {
        return returnCode;
    }

    public String getErrors() {
        return errors;
    }

    public String getOutput() {
        return output;
    }

    public boolean isSuccessful() {
        return getReturnCode() == 0;
    }
}
