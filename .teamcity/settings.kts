import jetbrains.buildServer.configs.kotlin.v2019_2.*
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.maven
import jetbrains.buildServer.configs.kotlin.v2019_2.vcs.GitVcsRoot

/*
The settings script is an entry point for defining a TeamCity
project hierarchy. The script should contain a single call to the
project() function with a Project instance or an init function as
an argument.

VcsRoots, BuildTypes, Templates, and subprojects can be
registered inside the project using the vcsRoot(), buildType(),
template(), and subProject() methods respectively.

To debug settings scripts in command-line, run the

    mvnDebug org.jetbrains.teamcity:teamcity-configs-maven-plugin:generate

command and attach your debugger to the port 8000.

To debug in IntelliJ Idea, open the 'Maven Projects' tool window (View
-> Tool Windows -> Maven Projects), find the generate task node
(Plugins -> teamcity-configs -> teamcity-configs:generate), the
'Debug' option is available in the context menu for the task.
*/

version = "2021.2"

project {

    vcsRoot(Maven)

    buildType(Build)
    buildType(FastTest)
    buildType(SlowTest)
    buildType(Package)

    val bts = sequential {
        buildType( Maven(name: "Build", goals: "clean compile"))
        parallel (options = { onDependencyFailure = FailureAction.CANCEL }) {
            buildType(Maven(name: "FastTest", goals: "clean test", runnerArgs: "-Dmaven.test.failure.ignore=true -Dtest=*.unit.*Test" ))
            buildType(Maven(name: "SlowTest", goals: "clean test", runnerArgs: "-Dmaven.test.failure.ignore=true -Dtest=*.integration.*Test"))
        }

        buildType(Maven(name: "Package", goals: "clean package", runnerArgs: "-DskipTests"))
    }.buildTypes()
    bts.forEach { buildType(it)}
}

class Maven(name: String, goals: String, runnerArgs: String? = null) : BuildType ({
    this.name =name
    id(name.toExtId())
    vcs {
        root(Maven)
    }
        steps {
            maven {
                this.goals = goals
                this.runnerArgs = runnerArgs
            }
        }
})


object Maven : GitVcsRoot({
    name = "maven"
    url = "https://github.com/marcobehlerjetbrains/anewtodolist.git"
    branch = "refs/heads/main"
})
