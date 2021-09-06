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

import org.junit.jupiter.api.Test;

import static com.xebialabs.overcast.command.Command.aCommand;
import static com.xebialabs.overcast.command.CommandProcessor.atLocation;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class CommandProcessorTest {

    @Test
    public void shouldThrowExceptionWhenCommandFailed() {
        //Test only for UNIX
        assumeTrue(System.getenv().containsKey("PATH"));

        assertThrows(NonZeroCodeException.class, () -> {
            atLocation("/tmp").run(aCommand("ls").withArguments("-wrong-argument"));
        });
    }

    @Test
    public void shouldStoreOutput() {
        //Test only for UNIX
        assumeTrue(System.getenv().containsKey("PATH"));
        CommandResponse ls = atLocation("/tmp").run(aCommand("ls"));
        assertThat(ls.getReturnCode(), is(0));
        assertThat(ls.getOutput().length() > 0, is(true));
    }


}
