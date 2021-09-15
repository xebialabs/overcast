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
