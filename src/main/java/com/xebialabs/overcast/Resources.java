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
package com.xebialabs.overcast;

import java.net.URL;

import static com.xebialabs.overcast.Preconditions.checkArgument;

public final class Resources {
    private Resources() {}

    public static URL getResource(String resourceName) {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        if (loader == null) {
            loader = Resources.class.getClassLoader();
        }
        URL url = loader.getResource(resourceName);
        checkArgument(url != null, "resource %s not found.", resourceName);
        return url;
    }

}
