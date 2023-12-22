import io.spine.internal.dependency.IntelliJ

repositories {
    intellijReleases
    jetBrainsCacheRedirector
}

dependencies {
    api(project(":psi"))
    api(IntelliJ.Platform.core)
    api(IntelliJ.Platform.util)
    api(IntelliJ.JavaPsi.api)
    api(IntelliJ.JavaPsi.impl)
}
