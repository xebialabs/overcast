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
package com.xebialabs.overcast.host;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

public class VMWareCloudHostItest {

    @Test
    public void shouldCloneAndDisposeVm() {
        VMWareHost h = (VMWareHost) CloudHostFactory.getCloudHost("overcastVMWareHost");
        try {
            h.setup();

            assertThat(h.getSessionId().length(), is(32));
            assertThat(h.getHostName(), notNullValue());
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            h.teardown();
        }
    }
}
