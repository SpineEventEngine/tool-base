package io.spine.tools.psi

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger

/**
 * Configures the environment for the standalone execution of the IntelliJ IDEA
 * modules associated with PSI.
 *
 * Original code is in `org.jetbrains.kotlin.cli.jvm.compiler.compat.kt`.
 */
public object IdeaStandaloneExecution {

    /**
     * We use the logger from the IntelliJ IDEA codebase in the hope that
     * IDEA would pick up its output log viewer.
     */
    private val LOG: Logger = Logger.getInstance(IdeaStandaloneExecution::class.java)

    // Copy-pasted from com.intellij.openapi.util.BuildNumber#FALLBACK_VERSION
    private const val FALLBACK_IDEA_BUILD_NUMBER = "999.SNAPSHOT"

    private var configured: Boolean = false;

    /**
     * Sets up the environment for the standalone execution of the IntelliJ IDEA modules
     * associated with PSI.
     *
     * Call this method before creating other PSI environment objects.
     */
    public fun setup() {
        synchronized(this) {
            if (!configured) {
                checkInHeadlessMode()
                setSystemProperties()
                configured = true
            }
        }
    }

    private fun checkInHeadlessMode() {
        // If `application` is `null` it means that we are in progress of set-up
        // application environment, i.e., we are not in the running IDEA.
        val application = ApplicationManager.getApplication() ?: return
        if (!application.isHeadlessEnvironment) {
            LOG.error(Throwable(
                "`${this::class.simpleName}` should be called only in headless environment."
            ))
        }
    }

    private fun setSystemProperties() {
        turnHeadlessIfUndefined()

        // As in `org.jetbrains.kotlin.cli.common.environment.setIdeaIoUseFallback()`.
        System.setProperty("idea.io.use.nio2", "true")

        // As in `org.jetbrains.kotlin.cli.jvm.compiler.compat.kt`.
        System.getProperties().let {
            it["project.structure.add.tools.jar.to.new.jdk"] = "false"
            it["psi.track.invalidation"] = "true"
            it["psi.incremental.reparse.depth.limit"] = "1000"
            it["ide.hide.excluded.files"] = "false"
            it["ast.loading.filter"] = "false"
            it["idea.ignore.disabled.plugins"] = "true"
            // Setting the build number explicitly avoids the command-line compiler
            // reading /tmp/build.txt in an attempt to get a build number from there.
            // See intellij platform PluginManagerCore.getBuildNumber.
            it["idea.plugins.compatible.build"] = FALLBACK_IDEA_BUILD_NUMBER
        }
    }

    /**
     * We depend on swing (indirectly through PSI or something), so we want to declare
     * headless mode, to avoid accidentally starting the UI thread.
     *
     * Original code is in `org.jetbrains.kotlin.cli.common.CLITool.doMain()`.
     */
    private fun turnHeadlessIfUndefined() {
        setIfNull("java.awt.headless", "true")
    }

    @Suppress("SameParameterValue")
    private fun setIfNull(propertyName: String, value: String) {
        if (System.getProperty(propertyName) == null) {
            System.setProperty(propertyName, value)
        }
    }
}

