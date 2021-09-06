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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class Command {

    private final List<String> command = new ArrayList<String>();

    private Command() {}

    public static Command aCommand(String executable) {
        if (executable == null) {
            throw new IllegalArgumentException("Executable can not be null");
        }
        Command c = new Command();
        c.withPart(executable);
        return c;
    }

    public Command withPart(String... part) {
        if (part == null) {
            return this;
        }

        for (String p : part) {
            if (p != null) {
                command.add(p);
            }
        }
        return this;
    }

    public Command withArguments(String... argument) {
        return withPart(argument);
    }

    public Command withOptions(String... option) {
        return withPart(option);
    }

    public Command withPrefix(String prefix) {
        return withPart(prefix);
    }

    public List<String> getCommand() {
        return command;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        Iterator<String> iterator = command.iterator();
        if (iterator.hasNext()) {
            builder.append(iterator.next());
        }
        while (iterator.hasNext()) {
            builder.append(' ').append(iterator.next());
        }
        return builder.toString();
    }

    public static Command fromString(String s) {
        Command c = new Command();

        for (String part : s.split("\\s")) {
            c.getCommand().add(part);
        }

        return c;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null || !(obj instanceof Command)) {
            return false;
        }

        return this.toString().equals(obj.toString());
    }

    @Override
    public int hashCode() {
        int c = 1000;

        for (char ch : this.toString().toCharArray()) {
            c += ch;
        }

        return c;
    }

    public List<String> asList() {
        return Arrays.asList(getCommand().toArray(new String[getCommand().size()]));
    }
}
