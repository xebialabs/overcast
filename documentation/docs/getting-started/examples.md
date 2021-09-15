---
sidebar_position: 5
---

# Examples

## Set up and Tear down

    @BeforeClass
    public static void doInitHost() {
       CloudHostFactory.getCloudHost("{my-host-label}").setup();
    }

    @AfterClass
    public static void doTeardownHost() {
       CloudHostFactory.getCloudHost("{my-host-label}").teardown();
    }

Also Overcast is used for integration tests of [Overthere](https://github.com/xebialabs/overthere).

