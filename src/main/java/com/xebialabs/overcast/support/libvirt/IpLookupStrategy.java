/**
 *    Copyright 2012-2018 XebiaLabs B.V.
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

public interface IpLookupStrategy {
    /**
     * attempt to lookup an IP for a MAC. May throw {@link IpLookupException} when the lookup fails abnormally. May
     * throw {@link IpNotFoundException} when the IP was not found. mac may be <code>null</code> if it is not required
     * for the lookup.
     */
    String lookup(String mac);
}
