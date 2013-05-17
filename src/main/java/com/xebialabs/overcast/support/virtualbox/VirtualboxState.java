package com.xebialabs.overcast.support.virtualbox;

import java.util.Arrays;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import static com.google.common.collect.Collections2.*;

public enum VirtualboxState {
    POWEROFF, ABORTED, SAVED, RUNNING;


    public static VirtualboxState fromStatusString(String s) {

        String stateString = Iterables.getOnlyElement(filter(Arrays.asList(s.split("\n")), new Predicate<String>() {
            @Override
            public boolean apply(final String input) {
                return input.startsWith("State:");
            }
        }));


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
}
