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
    api(IntelliJ.Platform.codeStyleImpl)
    api(IntelliJ.Platform.projectModel)
    api(IntelliJ.Platform.projectModelImpl)
    api(IntelliJ.Platform.lang)
    api(IntelliJ.Platform.langImpl) {
        exclude(group = "com.jetbrains.infra")
        exclude(group = "ai.grazie.spell")
        exclude(group = "com.jetbrains.intellij.platform")
    }


    // To use `AsyncExecutionServiceImpl`, uncomment this:
    api(IntelliJ.Platform.ideImpl) {
        exclude(group = "com.jetbrains.infra")
        exclude(group = "ai.grazie.spell")
        exclude(group = "com.jetbrains.intellij.platform")
    }

    // To use `NonProjectFileWritingAccessProvider`, uncomment the following:
    api(IntelliJ.Platform.ideCoreImpl) {
        exclude(group = "com.jetbrains.infra")
        exclude(group = "ai.grazie.spell")
        exclude(group = "com.jetbrains.intellij.platform")
    }

    // To access `com.intellij.psi.JspPsiUtil` as a transitive dependency
    // used by `com.intellij.psi.impl.source.codeStyle.ImportHelper`.
    implementation(IntelliJ.Jsp.jsp) {
        exclude(group = "com.jetbrains.infra")
        exclude(group = "ai.grazie.spell")
        exclude(group = "com.jetbrains.intellij.platform")
    }

    implementation(IntelliJ.Xml.xmlPsiImpl) {
        exclude(group = "com.jetbrains.infra")
        exclude(group = "ai.grazie.spell")
        exclude(group = "com.jetbrains.intellij.platform")
    }

    implementation(IntelliJ.Platform.analysisImpl) {
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
