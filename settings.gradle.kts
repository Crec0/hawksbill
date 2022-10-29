rootProject.name = "hawksbill"

enableFeaturePreview("VERSION_CATALOGS")

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("libs.versions.toml"))
        }
    }
}

include("core")
include("api")

include(":modules:chatbridge")
include(":modules:commands")
