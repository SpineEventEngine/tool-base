import io.spine.internal.dependency.IntelliJ
import io.spine.internal.dependency.Spine

repositories {
    intellijReleases
    jetBrainsCacheRedirector
}

dependencies {
    api(project(":psi"))
    api(IntelliJ.Platform.core)
    api(IntelliJ.Platform.util)

    api(IntelliJ.Platform.codeStyle)
    api(IntelliJ.Platform.projectModel)
    api(IntelliJ.Platform.projectModelImpl)
    api(IntelliJ.Platform.lang)
    api(IntelliJ.Platform.langImpl) {
        exclude(group = "com.jetbrains.infra")
        exclude(group = "ai.grazie.spell")
        exclude(group = "com.jetbrains.intellij.platform")
    }

    api(IntelliJ.JavaPsi.api)
    api(IntelliJ.JavaPsi.impl)

    implementation(IntelliJ.Java.impl) {
        exclude(group = "ai.grazie.nlp")
        exclude(group = "ai.grazie.spell")
        exclude(group = "ai.grazie.utils")
        exclude(group = "org.jetbrains.teamcity")
        exclude(group = "com.jetbrains.infra")

        exclude(group = "com.jetbrains.intellij.platform")

        exclude(group = "com.jetbrains.intellij.jsp")
        exclude(group = "com.jetbrains.intellij.regexp")
        exclude(group = "com.jetbrains.intellij.spellchecker")
        exclude(group = "com.jetbrains.intellij.xml")
        exclude(group = "com.jetbrains.intellij.copyright")

        exclude(group = "com.sun.activation")
        exclude(group = "javax.xml.bind")
        exclude(group = "commons-collections")
        exclude(group = "net.jcip")
        exclude(group = "net.sourceforge.nekohtml")
        exclude(group = "one.util")
        exclude(group = "org.apache.velocity")
        exclude(group = "org.glassfish.jaxb")
        exclude(group = "oro")
    }

    testImplementation(Spine.base)
    testImplementation(Spine.testlib)
    testImplementation(project(":plugin-testlib"))
}
