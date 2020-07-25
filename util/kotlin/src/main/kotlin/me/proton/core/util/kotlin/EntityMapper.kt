package me.proton.core.util.kotlin

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
inline operator fun <T, Ein, UI, Eout, M : UiModelMapper<Ein, UI, Eout>> M.invoke(f: M.() -> T) =
    f()

/**
 * Call [Collection.map] passing a [UiModelMapper] as receiver for the lambda [block]
 * E.g. `myListOfT.map( myTMapper ) { it.toEntity() }`
 */
inline fun <I, O, M : UiModelMapper<I, O, *>> Collection<I>.map(
    mapper: M,
    block: M.(I) -> O
) = map { mapper.block(it) }
