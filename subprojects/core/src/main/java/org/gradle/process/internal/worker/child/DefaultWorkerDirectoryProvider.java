/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.process.internal.worker.child;

import org.gradle.initialization.GradleUserHomeDirProvider;

import java.io.File;

public class DefaultWorkerDirectoryProvider implements WorkerDirectoryProvider {
    private final File gradleUserHomeDir;

    public DefaultWorkerDirectoryProvider(GradleUserHomeDirProvider gradleUserHomeDirProvider) {
        this.gradleUserHomeDir = gradleUserHomeDirProvider.getGradleUserHomeDirectory();
    }

    @Override
    public File getIdleWorkingDirectory() {
        File defaultWorkerDirectory = new File(gradleUserHomeDir, "workers");
        if (!defaultWorkerDirectory.exists() && !defaultWorkerDirectory.mkdirs()) {
            throw new IllegalStateException("Unable to create default worker directory at " + defaultWorkerDirectory.getAbsolutePath());
        }
        return defaultWorkerDirectory;
    }
}