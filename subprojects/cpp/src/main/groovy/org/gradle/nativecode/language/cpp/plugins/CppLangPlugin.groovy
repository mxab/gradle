/*
 * Copyright 2011 the original author or authors.
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
package org.gradle.nativecode.language.cpp.plugins
import org.gradle.api.Action
import org.gradle.api.Incubating
import org.gradle.api.Plugin
import org.gradle.api.internal.project.ProjectInternal
import org.gradle.internal.reflect.Instantiator
import org.gradle.language.base.FunctionalSourceSet
import org.gradle.language.base.ProjectSourceSet
import org.gradle.language.base.plugins.LanguageBasePlugin
import org.gradle.nativecode.base.NativeBinary
import org.gradle.nativecode.base.NativeDependencySet
import org.gradle.nativecode.base.SharedLibraryBinary
import org.gradle.nativecode.base.ToolChainTool
import org.gradle.nativecode.base.internal.NativeBinaryInternal
import org.gradle.nativecode.language.cpp.CppSourceSet
import org.gradle.nativecode.language.cpp.internal.DefaultCppSourceSet
import org.gradle.nativecode.language.cpp.tasks.CppCompile

import javax.inject.Inject

@Incubating
class CppLangPlugin implements Plugin<ProjectInternal> {
    private final Instantiator instantiator;

    @Inject
    public CppLangPlugin(Instantiator instantiator) {
        this.instantiator = instantiator;
    }

    void apply(ProjectInternal project) {
        project.getPlugins().apply(LanguageBasePlugin.class);

        ProjectSourceSet projectSourceSet = project.getExtensions().getByType(ProjectSourceSet.class);
        projectSourceSet.all(new Action<FunctionalSourceSet>() {
            public void execute(final FunctionalSourceSet functionalSourceSet) {
                applyConventions(project, functionalSourceSet)
            }
        });

        // TODO:DAZ It's ugly that we can't do this as project.binaries.all, but this is the way I could
        // add the cppCompiler in time to allow it to be configured within the component.binaries.all block.
        project.executables.all {
            it.binaries.all { binary ->
                binary.ext.cppCompiler = new ToolChainTool()
            }
        }
        project.libraries.all {
            it.binaries.all { binary ->
                binary.ext.cppCompiler = new ToolChainTool()
            }
        }

        project.binaries.withType(NativeBinary) { NativeBinaryInternal binary ->
            binary.source.withType(CppSourceSet).all { CppSourceSet sourceSet ->
                def compileTask = createCompileTask(project, binary, sourceSet)
                binary.builderTask.source compileTask.outputs.files.asFileTree.matching { include '**/*.obj', '**/*.o' }
            }
        }
    }

    private void applyConventions(ProjectInternal project, FunctionalSourceSet functionalSourceSet) {
        // Establish defaults for all cpp source sets
        functionalSourceSet.withType(CppSourceSet).all { sourceSet ->
            sourceSet.exportedHeaders.srcDir "src/${functionalSourceSet.name}/headers"
            sourceSet.source.srcDir "src/${functionalSourceSet.name}/cpp"
        }

        // Add a single C++ source set
        functionalSourceSet.add(instantiator.newInstance(DefaultCppSourceSet.class, "cpp", functionalSourceSet, project));
    }

    private def createCompileTask(ProjectInternal project, NativeBinaryInternal binary, def sourceSet) {
        def compileTask = project.task(binary.namingScheme.getTaskName("compile", sourceSet.fullName), type: CppCompile) {
            description = "Compiles the $sourceSet sources of $binary"
        }

        compileTask.toolChain = binary.toolChain
        compileTask.positionIndependentCode = binary instanceof SharedLibraryBinary

        compileTask.includes sourceSet.exportedHeaders
        compileTask.source sourceSet.source
        binary.libs.each { NativeDependencySet deps ->
            compileTask.includes deps.includeRoots
        }

        compileTask.conventionMapping.objectFileDir = { project.file("${project.buildDir}/objectFiles/${binary.namingScheme.outputDirectoryBase}/${sourceSet.fullName}") }
        compileTask.macros = binary.macros
        compileTask.compilerArgs = binary.cppCompiler.args

        compileTask
    }
}