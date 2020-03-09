/**
 *    Copyright 2012-2020 XebiaLabs B.V.
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
package com.xebialabs.overcast.support.virtualbox;

public enum VirtualboxState {
    POWEROFF, ABORTED, SAVED, RUNNING;


    public static VirtualboxState fromStatusString(String s) {

        String stateString = findState(s);


        if (stateString.contains("powered off")) {
            return POWEROFF;
        }

        if (stateString.contains("saved")) {
            return POWEROFF;
        }

        if (stateString.contains("aborted")) {
            return ABORTED;
        }

        if (stateString.contains("running")) {
            return RUNNING;
        }

        throw new IllegalStateException("Can not detect state for state string: " + stateString);
    }

    private static String findState(String s) {
        String[] lines = s.split("\n");
        for (String line : lines) {
            if (line.startsWith("State:")) {
                return line;
            }
        }
        throw new IllegalArgumentException("Expected 'State:...' but was: '" + s + "'.");
    }
}
