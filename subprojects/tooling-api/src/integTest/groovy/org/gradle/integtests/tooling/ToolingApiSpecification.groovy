package org.gradle.integtests.tooling

import org.gradle.api.Incubating
import org.gradle.integtests.fixtures.executer.GradleDistribution
import org.gradle.integtests.fixtures.executer.UnderDevelopmentGradleDistribution
import org.gradle.integtests.tooling.fixture.ToolingApi
import org.gradle.test.fixtures.ConcurrentTestUtil
import org.gradle.test.fixtures.file.TestFile
import org.gradle.test.fixtures.file.TestNameTestDirectoryProvider
import org.gradle.tooling.internal.consumer.ConnectorServices
import org.junit.Rule
import spock.lang.Specification

/**
 * Created with IntelliJ IDEA.
 * User: bruchmann
 * Date: 9/4/13
 * Time: 9:40 PM
 * To change this template use File | Settings | File Templates.
 * /**
 * @since 1.8
 * @return The set of publications that this project exposes
 */
class ToolingApiSpecification  extends Specification {

    @Rule final ConcurrentTestUtil concurrent = new ConcurrentTestUtil()
    @Rule final TestNameTestDirectoryProvider temporaryFolder = new TestNameTestDirectoryProvider()
    final GradleDistribution dist = new UnderDevelopmentGradleDistribution()
    final ToolingApi toolingApi = new ToolingApi(dist, temporaryFolder)

    int threads = 3

    def setup() {
        //concurrent tooling api at the moment is only supported for forked mode
        toolingApi.isEmbedded = false
        concurrent.shortTimeout = 180000
        new ConnectorServices().reset()
    }

    def cleanup() {
        new ConnectorServices().reset()
    }

    def "handles the same target gradle version concurrently"() {
        file('build.gradle')  << "apply plugin: 'java'"

        when:
        threads.times {
            concurrent.start { useToolingApi(new UnderDevelopmentGradleDistribution()) }
        }

        then:
        concurrent.finished()
    }

    TestFile file(Object... s) {
        temporaryFolder.file(s)
    }


}
