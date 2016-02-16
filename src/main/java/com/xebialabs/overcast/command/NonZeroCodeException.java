/**
 *    Copyright 2012-2016 XebiaLabs B.V.
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

/**
 * Thrown when {@link Command} returns non zero code
 */
@SuppressWarnings("serial")
public class NonZeroCodeException extends RuntimeException {

    private Command command;

    private CommandResponse response;

    public NonZeroCodeException(final Command command, final CommandResponse response) {
        super("Command " + command.toString() + " returned non-zero code " + response.getReturnCode());
        this.command = command;
        this.response = response;
    }

    public Command getCommand() {
        return command;
    }

    public CommandResponse getResponse() {
        return response;
    }
}
