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

import com.google.common.base.MoreObjects;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.emptyToNull;

public class Filesystem {
    public enum AccessMode {
        /** The source is accessed with the permissions of the user inside the guest. This is the default. */
        PASSTHROUGH,
        /** The source is accessed with the permissions of the hypervisor (QEMU process). */
        MAPPED,
        /**
         * Similar to {@link AccessMode#PASSTHROUGH}, the exception is that failure of privileged operations like
         * 'chown' are ignored. This makes a passthrough-like mode usable for people who run the hypervisor as non-root.
         */
        SQUASH,
    }

    public String source;
    public String target;
    public AccessMode accessMode;
    public boolean readOnly;

    public Filesystem(String source, String target, AccessMode accessMode, boolean readOnly) {
        checkNotNull(emptyToNull(source));
        checkNotNull(emptyToNull(target));

        this.source = source;
        this.target = target;
        this.accessMode = accessMode;
        this.readOnly = readOnly;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("source", source)
            .add("target", target)
            .add("accessMode", accessMode)
            .add("readOnly", readOnly).toString();
    }
}
