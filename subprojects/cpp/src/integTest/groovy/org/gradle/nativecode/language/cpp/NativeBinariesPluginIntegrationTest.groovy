/*
 * Copyright 2012 the original author or authors.
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
package org.gradle.nativecode.language.cpp
import org.gradle.nativecode.language.cpp.fixtures.AbstractBinariesIntegrationSpec
import org.gradle.util.Requires
import org.gradle.util.TestPrecondition
import spock.lang.Ignore

class NativeBinariesPluginIntegrationTest extends AbstractBinariesIntegrationSpec {
    static final HELLO_WORLD = "Hello, World!"

    def "setup"() {
        settingsFile << "rootProject.name = 'test'"
    }

    // TODO:DAZ Fix this
    @Ignore("Not sure what the behaviour should be")
    def "assemble executable from component with no source"() {
        given:
        buildFile << """
            apply plugin: "cpp"
            executables {
                main {}
            }
        """

        when:
        succeeds "mainExecutable"

        then:
        executable("build/binaries/mainExecutable/main").assertExists()
    }

    def "assemble executable from component with multiple language source sets"() {
        given:
        useMixedSources()

        when:
        buildFile << """
            apply plugin: "cpp"
            sources {
                main {}
            }
            executables {
                main {
                    source sources.main.cpp
                    source sources.main.c
                }
            }
        """

        then:
        succeeds "mainExecutable"

        and:
        executable("build/binaries/mainExecutable/main").exec().out == HELLO_WORLD
    }

    def "assemble executable binary directly from language source sets"() {
        given:
        useMixedSources()

        when:
        buildFile << """
            apply plugin: "cpp"
            sources {
                main {}
            }
            executables {
                main {}
            }
            binaries.all {
                source sources.main.cpp
                source sources.main.c
            }
        """

        then:
        succeeds "mainExecutable"

        and:
        executable("build/binaries/mainExecutable/main").exec().out == HELLO_WORLD
    }

    def "assemble executable binary directly from functional source set"() {
        given:
        useMixedSources()

        when:
        buildFile << """
            apply plugin: "cpp"
            sources {
                main {}
            }
            executables {
                main {}
            }
            binaries.all {
                source sources.main
            }
        """
        
        then:
        succeeds "mainExecutable"

        and:
        executable("build/binaries/mainExecutable/main").exec().out == HELLO_WORLD
    }

    private def useMixedSources() {
        file("src/main/headers/hello.h") << """
            void hello();
        """

        file("src/main/c/hello.c") << """
            #include <stdio.h>
            #include "hello.h"

            void hello () {
                printf("${HELLO_WORLD}");
            }
        """

        file("src/main/cpp/main.cpp") << """
            extern "C" {
                #include "hello.h"
            }

            int main () {
                hello();
                return 0;
            }
        """
    }

    def "build fails when link executable fails"() {
        given:
        buildFile << """
            apply plugin: "cpp-exe"
        """

        and:
        file("src", "main", "cpp", "helloworld.cpp") << """
            int thing() { return 0; }
        """

        expect:
        fails "mainExecutable"
        failure.assertHasDescription("Execution failed for task ':linkMainExecutable'.");
        failure.assertHasCause("Link failed; see the error output for details.")
    }

    // TODO:DAZ Find a way to make library linking fail on linux
    @Requires(TestPrecondition.NOT_LINUX)
    def "build fails when link shared library fails"() {
        given:
        buildFile << """
            apply plugin: "cpp-lib"
        """

        and:
        file("src/main/cpp/hello.cpp") << """
            #include "test.h"
            void hello() {
                test();
            }
"""
        // Header file available, but no implementation to link
        file("src/main/cpp/test.h") << """
            int test();
"""

        when:
        fails "mainSharedLibrary"

        then:
        failure.assertHasDescription("Execution failed for task ':linkMainSharedLibrary'.");
        failure.assertHasCause("Link failed; see the error output for details.")
    }

    // TODO:DAZ Add test for failing to link static library
}