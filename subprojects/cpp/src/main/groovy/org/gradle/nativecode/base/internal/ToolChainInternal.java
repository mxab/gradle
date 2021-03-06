/*
 * Copyright 2013 the original author or authors.
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

package org.gradle.nativecode.base.internal;

import org.gradle.api.internal.tasks.compile.Compiler;
import org.gradle.nativecode.base.ToolChain;

public interface ToolChainInternal extends ToolChain {
    ToolChainAvailability getAvailability();

    <T extends BinaryToolSpec> Compiler<T> createCppCompiler();

    <T extends BinaryToolSpec> Compiler<T> createCCompiler();

    <T extends BinaryToolSpec> Compiler<T> createAssembler();

    <T extends LinkerSpec> Compiler<T> createLinker();

    <T extends StaticLibraryArchiverSpec> Compiler<T> createStaticLibraryArchiver();

    String getExecutableName(String executablePath);

    String getSharedLibraryName(String libraryPath);

    String getSharedLibraryLinkFileName(String libraryPath);

    String getStaticLibraryName(String libraryPath);

    /**
     * Returns a unique identifier for the output produced by this toolchain on the current platform.
     */
    String getOutputType();
}
