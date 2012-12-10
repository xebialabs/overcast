package com.xebialabs.overcast.vagrant;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import com.google.common.io.CharStreams;

public class VagrantResponse {

    private int returnCode;

    private String errors;

    private String output;

    /**
     * Creates vagrant response object and converts streams to strings. Be aware that it expects marked stream and will reset it in order to read again.
     * @param returnCode return code of the vagrant command
     * @param errors marked error stream
     * @param output marked output stream
     */
    public VagrantResponse(int returnCode, final BufferedInputStream errors, BufferedInputStream output) {

        try {
            output.reset();
            errors.reset();
            this.output = CharStreams.toString(new InputStreamReader(output, "UTF-8"));
            this.errors = CharStreams.toString(new InputStreamReader(errors, "UTF-8"));
        } catch (IOException e) {
            throw new RuntimeException("Can not read vagrant output as UTF-8");
        }

        this.returnCode = returnCode;
    }

    public VagrantResponse(int returnCode, String errors, String output) {
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
}
