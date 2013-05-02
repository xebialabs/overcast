package com.xebialabs.overcast.support.virtualbox;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.*;

public class VirtualboxStateTest {

    @Test
    public void shouldRecognizePowerOffState() {
        String s = "Nested Paging:   on\n" +
                "State:           powered off (since 2013-03-26T17:33:45.000000000)\n" +
                "Monitor count:   1\n" +
                "3D Acceleration: off";

        assertThat(VirtualboxState.fromStatusString(s), is(VirtualboxState.POWEROFF));
    }

    @Test
    public void shouldRecognizeAbortedState() {
        String s = "Nested Paging:   on\n" +
                "State:           aborted (since 2013-03-26T17:33:45.000000000)\n" +
                "Monitor count:   1\n" +
                "3D Acceleration: off";

        assertThat(VirtualboxState.fromStatusString(s), is(VirtualboxState.ABORTED));
    }

    @Test
    public void shouldRecognizeRunningState() {
        String s = "Nested Paging:   on\n" +
                "State:           running (since 2013-03-26T17:33:45.000000000)\n" +
                "Monitor count:   1\n" +
                "3D Acceleration: off";

        assertThat(VirtualboxState.fromStatusString(s), is(VirtualboxState.RUNNING));
    }
}
