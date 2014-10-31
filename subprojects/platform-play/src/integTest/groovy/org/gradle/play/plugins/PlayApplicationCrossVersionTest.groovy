/*
 * Copyright 2014 the original author or authors.
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

package org.gradle.play.plugins
import org.gradle.integtests.fixtures.MultiVersionIntegrationSpec
import org.gradle.integtests.fixtures.TargetCoverage
import org.gradle.integtests.fixtures.TestResources
import org.gradle.play.fixtures.PlayCoverage
import org.gradle.test.fixtures.archive.JarTestFixture
import org.junit.Rule

@TargetCoverage({PlayCoverage.DEFAULT})
class PlayApplicationCrossVersionTest extends MultiVersionIntegrationSpec{

    @Rule
    public final TestResources resources = new TestResources(temporaryFolder)

    def setup() {
        buildFile << """
        plugins {
            id 'play-application'
        }

        model {
            components {
                myApp(PlayApplicationSpec)
            }
        }

        tasks.withType(ScalaCompile) {
            scalaCompileOptions.fork = true
            scalaCompileOptions.useAnt = false
            scalaCompileOptions.forkOptions.memoryMaximumSize = '1g'
            scalaCompileOptions.forkOptions.jvmArgs = ['-XX:MaxPermSize=512m']
        }

        tasks.withType(TwirlCompile) {
            fork = true
            forkOptions.memoryInitialSize =  "256m"
            forkOptions.memoryMaximumSize =  "512m"
        }
"""
    }

    def "can build play apps generated by 'play new'"() {
        given:
        resources.maybeCopy("PlayApplicationPluginIntegrationTest/playNew")
        buildFile << """
        repositories{
            jcenter()
            maven{
                name = "typesafe-maven-release"
                url = "http://repo.typesafe.com/typesafe/maven-releases"
            }

            dependencies{
                playAppCompile '${version.playDependency}'
                twirl '${version.twirlDependency}'
                playRoutes '${version.routesDependency}'
            }
        }
"""
        when:
        executer.withArgument("-i")
        succeeds("assemble")
        then:
        executed(":routesCompileMyAppBinary", ":twirlCompileMyAppBinary", ":createMyAppBinaryJar", ":myAppBinary", ":assemble")
        def jarTestFixture = new JarTestFixture(file("build/jars/myApp/myAppBinary.jar"))
        jarTestFixture.assertContainsFile("Routes.class")
        jarTestFixture.assertContainsFile("views/html/index.class")
        jarTestFixture.assertContainsFile("views/html/main.class")
        jarTestFixture.assertContainsFile("controllers/Application.class")
        jarTestFixture.assertContainsFile("images/favicon.png")
        jarTestFixture.assertContainsFile("stylesheets/main.css")
        jarTestFixture.assertContainsFile("javascripts/hello.js")
        jarTestFixture.assertContainsFile("application.conf")

        when:
        succeeds("createMyAppBinaryJar")
        then:
        skipped(":createMyAppBinaryJar", ":twirlCompileMyAppBinary")
    }
}
