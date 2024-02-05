import io.spine.internal.dependency.IntelliJ
import io.spine.internal.dependency.Spine

repositories {
    intellijReleases
    jetBrainsCacheRedirector
}

dependencies {
    api(Spine.base)
    api(project(":psi"))
    api(IntelliJ.Platform.core)
    api(IntelliJ.Platform.util)
    api(IntelliJ.JavaPsi.api)
    api(IntelliJ.JavaPsi.impl)

    testImplementation(Spine.base)
    testImplementation(Spine.testlib)
}
