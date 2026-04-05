import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.TaskContainer
import org.gradle.kotlin.dsl.ExistingDomainObjectDelegate
import org.gradle.kotlin.dsl.RegisteringDomainObjectDelegateProviderWithTypeAndAction
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import kotlin.reflect.KProperty


@Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
@PublishedApi
internal operator fun <U : Task> RegisteringDomainObjectDelegateProviderWithTypeAndAction<out TaskContainer, U>.provideDelegate(
    receiver: Any?,
    property: KProperty<*>,
) = ExistingDomainObjectDelegate.of(
    delegateProvider.register(property.name, type.java, action),
)

@PublishedApi
internal val Project.sourceSets: SourceSetContainer
    get() = (this as ExtensionAware).extensions.getByName("sourceSets") as SourceSetContainer

@Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
@PublishedApi
internal operator fun <T> ExistingDomainObjectDelegate<out T>.getValue(
    receiver: Any?,
    property: KProperty<*>
): T = delegate

@OptIn(ExperimentalContracts::class)
inline fun <reified T> Any?.cast(): T {
    contract { returns() implies (this@cast is T) }
    return this as T
}

/**
 * Retrieves the [versionCatalogs][VersionCatalogsExtension] extension.
 */
internal val Project.versionCatalogs: VersionCatalogsExtension
    get() = (this as ExtensionAware).extensions.getByName("versionCatalogs") as VersionCatalogsExtension

/**
 * Configures the [versionCatalogs][VersionCatalogsExtension] extension.
 */
internal fun Project.versionCatalogs(configure: Action<VersionCatalogsExtension>): Unit =
    (this as ExtensionAware).extensions.configure("versionCatalogs", configure)

