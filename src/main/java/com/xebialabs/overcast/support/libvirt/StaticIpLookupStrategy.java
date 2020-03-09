/**
 *    Copyright 2012-2020 XebiaLabs B.V.
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
package com.xebialabs.overcast.support.libvirt;

import static com.xebialabs.overcast.OvercastProperties.getRequiredOvercastProperty;

/**
 * Simple {@link IpLookupStrategy} that returns a pre-configured static IP.
 */
public class StaticIpLookupStrategy implements IpLookupStrategy {
    private static final String STATIC_IP_SUFFIX = ".static.ip";

    private String ip;

    public StaticIpLookupStrategy(String ip) {
        this.ip = ip;
    }

    public static StaticIpLookupStrategy create(String prefix) {
        String ip = getRequiredOvercastProperty(prefix + STATIC_IP_SUFFIX);
        return new StaticIpLookupStrategy(ip);
    }

    @Override
    public String lookup(String mac) {
        return ip;
    }
}
