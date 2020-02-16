// ------------------------------------------------------------------------------
// <auto-generated>
//
//     This code was generated.
//
//     - To turn off auto-generation set:
//
//         [TeamCity (AutoGenerate = false)]
//
//     - To trigger manual generation invoke:
//
//         nuke --generate-configuration TeamCity --host TeamCity
//
// </auto-generated>
// ------------------------------------------------------------------------------

import jetbrains.buildServer.configs.kotlin.v2018_1.*
import jetbrains.buildServer.configs.kotlin.v2018_1.buildFeatures.*
import jetbrains.buildServer.configs.kotlin.v2018_1.buildSteps.*
import jetbrains.buildServer.configs.kotlin.v2018_1.triggers.*
import jetbrains.buildServer.configs.kotlin.v2018_1.vcs.*

version = "2019.2"

project {
    buildType(Compile)
    buildType(Pack)
    buildType(Test_P1T2)
    buildType(Test_P2T2)
    buildType(Test)
    buildType(Coverage)
    buildType(Publish)
    buildType(Announce)

    buildTypesOrder = arrayListOf(Compile, Pack, Test_P1T2, Test_P2T2, Test, Coverage, Publish, Announce)

    params {
        select (
            "env.Verbosity",
            label = "Verbosity",
            description = "Logging verbosity during build execution. Default is 'Normal'.",
            value = "Normal",
            options = listOf("Minimal" to "Minimal", "Normal" to "Normal", "Quiet" to "Quiet", "Verbose" to "Verbose"),
            display = ParameterDisplay.NORMAL)
        select (
            "env.Configuration",
            label = "Configuration",
            description = "Configuration to build - Default is 'Debug' (local) or 'Release' (server)",
            value = "Release",
            options = listOf("Debug" to "Debug", "Release" to "Release"),
            display = ParameterDisplay.NORMAL)
        checkbox (
            "env.IgnoreFailedSources",
            label = "IgnoreFailedSources",
            value = "False",
            checked = "True",
            unchecked = "False",
            display = ParameterDisplay.NORMAL)
        text (
            "env.Source",
            label = "Source",
            description = "NuGet Source for Packages",
            value = "https://api.nuget.org/v3/index.json",
            allowEmpty = true,
            display = ParameterDisplay.NORMAL)
        text (
            "env.GitHubToken",
            label = "GitHubToken",
            description = "GitHub Token",
            value = "",
            allowEmpty = true,
            display = ParameterDisplay.NORMAL)
        checkbox (
            "env.AutoStash",
            label = "AutoStash",
            value = "True",
            checked = "True",
            unchecked = "False",
            display = ParameterDisplay.NORMAL)
        checkbox (
            "env.UseSSH",
            label = "UseSSH",
            value = "False",
            checked = "True",
            unchecked = "False",
            display = ParameterDisplay.NORMAL)
        param(
            "teamcity.runner.commandline.stdstreams.encoding",
            "UTF-8"
        )
    }
}
object Compile : BuildType({
    name = "⚙️ Compile"
    vcs {
        root(DslContext.settingsRoot)
        cleanCheckout = true
    }
    steps {
        exec {
            path = "build.cmd"
            arguments = "Restore Compile --skip"
        }
    }
})
object Pack : BuildType({
    name = "📦 Pack"
    vcs {
        root(DslContext.settingsRoot)
        cleanCheckout = true
    }
    artifactRules = "output/packages/*.nupkg => output/packages"
    steps {
        exec {
            path = "build.cmd"
            arguments = "Pack --skip"
        }
    }
    triggers {
        vcs {
            triggerRules = "+:**"
        }
        schedule {
            schedulingPolicy = daily {
                hour = 3
            }
            triggerRules = "+:**"
            triggerBuild = always()
            withPendingChangesOnly = false
            enableQueueOptimization = true
            param("cronExpression_min", "3")
        }
    }
    dependencies {
        snapshot(Compile) {
            onDependencyFailure = FailureAction.FAIL_TO_START
            onDependencyCancel = FailureAction.CANCEL
        }
    }
})
object Test_P1T2 : BuildType({
    name = "🚦 Test 🧩 1/2"
    vcs {
        root(DslContext.settingsRoot)
        cleanCheckout = true
    }
    artifactRules = """
        output/test-results/*.trx => output/test-results
        output/test-results/*.xml => output/test-results
    """.trimIndent()
    steps {
        exec {
            path = "build.cmd"
            arguments = "Test --skip --test-partition 1"
        }
    }
    dependencies {
        snapshot(Compile) {
            onDependencyFailure = FailureAction.FAIL_TO_START
            onDependencyCancel = FailureAction.CANCEL
        }
    }
})
object Test_P2T2 : BuildType({
    name = "🚦 Test 🧩 2/2"
    vcs {
        root(DslContext.settingsRoot)
        cleanCheckout = true
    }
    artifactRules = """
        output/test-results/*.trx => output/test-results
        output/test-results/*.xml => output/test-results
    """.trimIndent()
    steps {
        exec {
            path = "build.cmd"
            arguments = "Test --skip --test-partition 2"
        }
    }
    dependencies {
        snapshot(Compile) {
            onDependencyFailure = FailureAction.FAIL_TO_START
            onDependencyCancel = FailureAction.CANCEL
        }
    }
})
object Test : BuildType({
    name = "🚦 Test"
    type = Type.COMPOSITE
    vcs {
        root(DslContext.settingsRoot)
        cleanCheckout = true
        showDependenciesChanges = true
    }
    artifactRules = "**/*"
    triggers {
        vcs {
            triggerRules = "+:**"
        }
        schedule {
            schedulingPolicy = daily {
                hour = 3
            }
            triggerRules = "+:**"
            triggerBuild = always()
            withPendingChangesOnly = false
            enableQueueOptimization = true
            param("cronExpression_min", "3")
        }
    }
    dependencies {
        snapshot(Test_P1T2) {
            onDependencyFailure = FailureAction.FAIL_TO_START
            onDependencyCancel = FailureAction.CANCEL
        }
        snapshot(Test_P2T2) {
            onDependencyFailure = FailureAction.FAIL_TO_START
            onDependencyCancel = FailureAction.CANCEL
        }
        artifacts(Test_P1T2) {
            artifactRules = "**/*"
        }
        artifacts(Test_P2T2) {
            artifactRules = "**/*"
        }
    }
})
object Coverage : BuildType({
    name = "📊 Coverage"
    vcs {
        root(DslContext.settingsRoot)
        cleanCheckout = true
    }
    artifactRules = "output/coverage-report.zip => output"
    steps {
        exec {
            path = "build.cmd"
            arguments = "Coverage --skip"
        }
    }
    triggers {
        finishBuildTrigger {
            buildType = "${Test.id}"
        }
    }
    dependencies {
        snapshot(Test) {
            onDependencyFailure = FailureAction.FAIL_TO_START
            onDependencyCancel = FailureAction.CANCEL
        }
        artifacts(Test) {
            artifactRules = """
                output/test-results/*.trx => output/test-results
                output/test-results/*.xml => output/test-results
            """.trimIndent()
        }
    }
})
object Publish : BuildType({
    name = "🚚 Publish"
    type = Type.DEPLOYMENT
    vcs {
        root(DslContext.settingsRoot)
        cleanCheckout = true
    }
    steps {
        exec {
            path = "build.cmd"
            arguments = "Publish --skip"
        }
    }
    params {
        text (
            "env.ApiKey",
            label = "ApiKey",
            description = "NuGet Api Key",
            value = "",
            allowEmpty = false,
            display = ParameterDisplay.PROMPT)
        text (
            "env.SlackWebhook",
            label = "SlackWebhook",
            description = "Slack Webhook",
            value = "",
            allowEmpty = false,
            display = ParameterDisplay.PROMPT)
        text (
            "env.GitterAuthToken",
            label = "GitterAuthToken",
            description = "Gitter Auth Token",
            value = "",
            allowEmpty = false,
            display = ParameterDisplay.PROMPT)
    }
    dependencies {
        snapshot(Test) {
            onDependencyFailure = FailureAction.FAIL_TO_START
            onDependencyCancel = FailureAction.CANCEL
        }
        snapshot(Pack) {
            onDependencyFailure = FailureAction.FAIL_TO_START
            onDependencyCancel = FailureAction.CANCEL
        }
        artifacts(Pack) {
            artifactRules = "output/packages/*.nupkg => output/packages"
        }
    }
})
object Announce : BuildType({
    name = "🗣 Announce"
    vcs {
        root(DslContext.settingsRoot)
        cleanCheckout = true
    }
    steps {
        exec {
            path = "build.cmd"
            arguments = "Announce --skip"
        }
    }
    triggers {
        finishBuildTrigger {
            buildType = "${Publish.id}"
        }
    }
})
