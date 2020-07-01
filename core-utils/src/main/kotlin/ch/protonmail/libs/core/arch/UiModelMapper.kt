@file:Suppress("unused") // Public APIs

package ch.protonmail.libs.core.arch

import androidx.paging.DataSource
import ch.protonmail.libs.core.unsupported
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.map
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * A common interface for transform an Entity to an UiModel and transform back an UiModel to an
 * entity.
 *
 * @param Ein the type of the source entity to map to UiModel [UI]
 * @param UI the type of the UiModel
 * @param Eout the type of the entity generated from UiModel [UI]
 *
 * @author Davide Farella
 */
interface UiModelMapper<in Ein, UI, out Eout> {

    /** Create an UiModel [UI] from the given entity [Ein] */
    fun Ein.toUiModel(): UI = unsupported

    /** Create an entity [Eout] from the given UiModel [UI] */
    fun UI.toEntity(): Eout = unsupported
}

/**
 * Override of invoke operator for get access to `this` [UiModelMapper] as receiver of the
 * lambda for call extension functions declared in this class:
 * e.g. `userMapper { registrationParams.toUiModel() }`
 */
inline operator fun <T, Ein, UI, Eout, M: UiModelMapper<Ein, UI, Eout>> M.invoke( f: M.() -> T ) = f()


/**
 * Call [Collection.map] passing a [UiModelMapper] as receiver for the lambda [block]
 * E.g. `myListOfT.map( myTMapper ) { it.toEntity() }`
 */
inline fun <I, O, M: UiModelMapper<I, O, *>> Collection<I>.map(
        mapper: M,
        block: M.(I) -> O
) = map { mapper.block( it ) }

/**
 * Call [DataSource.Factory.map] passing a [UiModelMapper] as receiver for the lambda [block]
 * E.g. `myDataSourceOfT.map( myTMapper ) { it.toEntity() }`
 */
inline fun <T, V, Ein, UI, Eout, M: UiModelMapper<Ein, UI, Eout>> DataSource.Factory<Int, T>.map(
        mapper: M,
        crossinline block: M.(T) -> V
) = map { mapper.block( it ) }

/**
 * Call [Flow.map] passing a [UiModelMapper] as receiver for the lambda [block]
 * E.g. `myChannel.map( myTMapper ) { it.toEntity() }`
 */
inline fun <T, V, Ein, UI, Eout, M: UiModelMapper<Ein, UI, Eout>> Flow<T>.map(
    mapper: M,
    crossinline block: M.(T) -> V
) = map { mapper.block( it ) }

/**
 * Call [ReceiveChannel.map] passing a [UiModelMapper] as receiver for the lambda [block]
 * E.g. `myChannel.map( myTMapper ) { it.toEntity() }`
 */
@Suppress("DeprecatedCallableAddReplaceWith", "DEPRECATION")
@Deprecated(
    message = "From JetBrains: Channel operators are deprecated in favour of Flow and will be removed in 1.4",
    level = DeprecationLevel.WARNING
)
inline fun <T, V, Ein, UI, Eout, M: UiModelMapper<Ein, UI, Eout>> ReceiveChannel<T>.map(
    mapper: M,
    crossinline block: M.(T) -> V
) = map { mapper.block( it ) }
