/**
 *    Copyright 2012-2018 XebiaLabs B.V.
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
package com.xebialabs.overcast.support.vagrant;

public enum VagrantState {
    NOT_CREATED, POWEROFF, ABORTED, SAVED, RUNNING;

    public static VagrantState fromStatusString(String s) {
        if (s.contains("not created")) return NOT_CREATED;
        if (s.contains("poweroff")) return POWEROFF;
        if (s.contains("aborted")) return ABORTED;
        if (s.contains("saved")) return SAVED;
        if (s.contains("running")) return RUNNING;

        throw new RuntimeException("Unknown status: " + s);
    }

    public static String[] getTransitionCommand(VagrantState newState) {

        switch (newState) {
            case NOT_CREATED:
                return new String[]{"destroy", "-f"};
            case POWEROFF:
                return new String[]{"halt"};
            case SAVED:
                return new String[]{"suspend"};
            case RUNNING:
                return new String[]{"up", "--provision"};
            case ABORTED:
                break; // ignore
            default:
                throw new IllegalArgumentException(String.format("The state %s is not known", newState));
        }
        throw new RuntimeException("Unexpected state in getTransitionCommand "+newState.name());
    }
}
