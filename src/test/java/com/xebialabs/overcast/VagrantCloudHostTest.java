package com.xebialabs.overcast;

import com.xebialabs.overcast.vagrant.VagrantHelper;
import com.xebialabs.overcast.vagrant.VagrantResponse;
import org.junit.*;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class VagrantCloudHostTest {

    @Mock
    private VagrantHelper vagrantHelper;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void shouldThrowNoExceptionsWhenAllGoesFine() {
        when(vagrantHelper.doVagrant("status")).thenReturn(new VagrantResponse(0, "", "not created"));
        when(vagrantHelper.doVagrant("up")).thenReturn(new VagrantResponse(0, "", ""));

        VagrantCloudHost vagrantCloudHost = new VagrantCloudHost("127.0.0.1", vagrantHelper);

        vagrantCloudHost.setup();
    }

    @Test(expected = RuntimeException.class)
    public void shouldThrowAnExceptionWhenExitCodeIsNot0() {
        when(vagrantHelper.doVagrant("status")).thenReturn(new VagrantResponse(0, "", "not created"));
        when(vagrantHelper.doVagrant("up")).thenReturn(new VagrantResponse(3, "", ""));

        VagrantCloudHost vagrantCloudHost = new VagrantCloudHost("127.0.0.1", vagrantHelper);
        vagrantCloudHost.setup();
    }


    @Test(expected = RuntimeException.class)
    public void shouldThrowAnExceptionWhenOutputContainsPuppetErrors() {

        String outputWithPuppetErrors = "\u001B[0;36mnotice: /Stage[main]/Deployit-mw::blablabla\n" +
                    "\u001B[1;35merr: /Stage[main]/Was::Config/Exec[call url]/returns: blablabla[0m\n" +
                    "\u001B[0;36mnotice: /Stage[main]/Was::Config/Exec[configure-was]: Dependency blablabla[0m\n" +
                    "\u001B[0;33mwarning: /Stage[main]/Was::Config/Exec[configure-was]: Skipping because of blablabla[0m\n" +
                    "\u001B[0;36mnotice: Finished catalog run in 593.85 seconds[0m";

        when(vagrantHelper.doVagrant("status")).thenReturn(new VagrantResponse(0, "", "not created"));
        when(vagrantHelper.doVagrant("up")).thenReturn(new VagrantResponse(0, "", outputWithPuppetErrors));

        VagrantCloudHost vagrantCloudHost = new VagrantCloudHost("127.0.0.1", vagrantHelper);
        vagrantCloudHost.setup();
    }

}
