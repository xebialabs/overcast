package com.xebialabs.overcast.support.vmware;

public class VMWareVM {

    private final Integer cpuCount;
    private final String id;
    private final Integer memorySizeMb;
    private final String name;
    private final String powerState;

    public VMWareVM(Integer memorySizeMb, String id, String name, String powerState, Integer cpuCount) {
        this.memorySizeMb = memorySizeMb;
        this.id = id;
        this.name = name;
        this.powerState = powerState;
        this.cpuCount = cpuCount;
    }

    public Integer getMemorySizeMb() {
        return memorySizeMb;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPowerState() {
        return powerState;
    }

    public Integer getCpuCount() {
        return cpuCount;
    }
}
