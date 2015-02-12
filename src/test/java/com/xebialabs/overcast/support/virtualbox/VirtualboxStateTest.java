/**
 *    Copyright 2012-2015 XebiaLabs B.V.
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
