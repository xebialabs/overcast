/**
 *    Copyright 2012-2021 Digital.ai
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
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
