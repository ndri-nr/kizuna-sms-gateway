pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.10.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "KizunaGateway"

include(":app")
include(":domain")
include(":core:database")
include(":core:data")
include(":core:ui")
include(":core:network")
include(":core:template")
include(":core:service")
include(":core:worker")
include(":feature:dashboard")
include(":feature:sms")
include(":feature:webhook")
include(":feature:rules")
include(":feature:logs")
include(":feature:settings")
include(":feature:home")
include(":feature:about")
include(":feature:outbound")
