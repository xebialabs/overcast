package com.xebialabs.overcast.support.vagrant;

import org.junit.Test;

import static com.xebialabs.overcast.support.vagrant.VagrantState.*;
import static junit.framework.Assert.assertEquals;

public class VagrantStateTest {

	@Test
	public void shouldTranslateStatusStringToTest() {
		assertEquals(
				RUNNING,
				fromStatusString("Current VM states:\n" +
						"\n" +
						"default                  running\n" +
						"\n" +
						"The VM is running. To stop this VM, you can run `vagrant halt` to\n" +
						"shut it down forcefully, or you can run `vagrant suspend` to simply\n" +
						"suspend the virtual machine. In either case, to restart it again,\n" +
						"simply run `vagrant up`.")
		);

		assertEquals(NOT_CREATED, fromStatusString("default                  not created\n"));
		assertEquals(POWEROFF, fromStatusString("default                  poweroff\n"));
		assertEquals(ABORTED, fromStatusString("default                  aborted\n"));
		assertEquals(SAVED, fromStatusString("default                  saved\n"));
	}

	@Test
	public void shouldGiveATransitionCommand() {
		assertEquals("up", getTransitionCommand(RUNNING)[0]);
		assertEquals("halt", getTransitionCommand(POWEROFF)[0]);
		assertEquals("suspend", getTransitionCommand(SAVED)[0]);
		assertEquals("destroy", getTransitionCommand(NOT_CREATED)[0]);
		assertEquals("-f", getTransitionCommand(NOT_CREATED)[1]);
	}
}
