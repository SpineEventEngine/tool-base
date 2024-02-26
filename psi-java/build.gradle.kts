import io.spine.internal.dependency.Guava
import io.spine.internal.dependency.IntelliJ
import io.spine.internal.dependency.Spine

repositories {
    intellijReleases
    jetBrainsCacheRedirector
}

dependencies {
    implementation(Guava.lib)
    api(project(":psi"))
    api(IntelliJ.Platform.core)
    api(IntelliJ.Platform.util)

    api(IntelliJ.Platform.codeStyle)
    api(IntelliJ.Platform.codeStyleImpl)
    api(IntelliJ.Platform.projectModel)
    api(IntelliJ.Platform.projectModelImpl)
    api(IntelliJ.Platform.lang)

    val exclusions = listOf(
        "com.jetbrains.infra",
        "ai.grazie.spell",
        "com.jetbrains.intellij.platform",
        "org.codehaus.groovy",
        "org.apache.groovy"
    )
    fun ModuleDependency.excludeMany(excl: Iterable<String> = exclusions) {
        excl.forEach { exclude(it) }
    }

    api(IntelliJ.Platform.langImpl) { excludeMany() }

    // To use `AsyncExecutionServiceImpl`, uncomment this:
    api(IntelliJ.Platform.ideImpl) { excludeMany() }

    // To use `NonProjectFileWritingAccessProvider`, uncomment the following:
    api(IntelliJ.Platform.ideCoreImpl) { excludeMany() }

    // To access `com.intellij.psi.JspPsiUtil` as a transitive dependency
    // used by `com.intellij.psi.impl.source.codeStyle.ImportHelper`.
    implementation(IntelliJ.Jsp.jsp) { excludeMany() }

    implementation(IntelliJ.Xml.xmlPsiImpl) { excludeMany() }
    implementation(IntelliJ.Platform.analysisImpl) { excludeMany() }
    implementation(IntelliJ.Platform.indexingImpl) { excludeMany() }

    api(IntelliJ.JavaPsi.api)
    api(IntelliJ.JavaPsi.impl)

    implementation(IntelliJ.Java.impl) {
        excludeMany(listOf(
            "ai.grazie.nlp",
            "ai.grazie.spell",
            "ai.grazie.utils",
            "org.jetbrains.teamcity",
            "com.jetbrains.infra",

            "com.jetbrains.intellij.platform",
            "org.apache.groovy",

            "com.jetbrains.intellij.jsp",
            "com.jetbrains.intellij.regexp",
            "com.jetbrains.intellij.spellchecker",
            "com.jetbrains.intellij.xml",
            "com.jetbrains.intellij.copyright",

            "com.sun.activation",
            "javax.xml.bind",
            "commons-collections",
            "net.jcip",
            "net.sourceforge.nekohtml",
            "one.util",
            "org.apache.velocity",
            "org.glassfish.jaxb",
            "oro"
        ))
    }

    testImplementation(Spine.base)
    testImplementation(Spine.testlib)
    testImplementation(project(":plugin-testlib"))
}
