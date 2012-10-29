package com.xebialabs.overcast;

public enum VagrantState {
    NOT_CREATED, POWEROFF, SAVED, RUNNING;

    public static VagrantState fromStatusString(String statusString) {
        if (statusString.contains("not created")) return NOT_CREATED;
        if (statusString.contains("poweroff")) return POWEROFF;
        if (statusString.contains("saved")) return SAVED;
        if (statusString.contains("running")) return RUNNING;

        throw new RuntimeException("Unknown status: " + statusString);
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
                return new String[]{"up"};
        }

        throw new RuntimeException("This never gonna happen.");

    }
}
