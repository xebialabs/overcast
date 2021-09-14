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
