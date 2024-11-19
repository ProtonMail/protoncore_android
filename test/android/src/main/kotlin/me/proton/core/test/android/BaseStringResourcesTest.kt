/*
 * Copyright (c) 2024 Proton AG
 * This file is part of Proton AG and ProtonCore.
 *
 * ProtonCore is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ProtonCore is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ProtonCore.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.proton.core.test.android

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.icu.text.PluralRules
import android.os.Build
import android.text.SpannableString
import androidx.annotation.RequiresApi
import androidx.test.core.app.ApplicationProvider
import org.junit.Before
import org.junit.Test
import java.lang.reflect.Field
import java.lang.reflect.Modifier
import java.util.Locale

private val languageRegex = Regex("[a-z]+") // "en"
private val languageWithCountryRegex = Regex("([a-z]+)-r([A-Z]+)") // "en-rUS
private val languageWithRegionRegex = Regex("b\\+([a-z]+)\\+([0-9]+)") // "b+es+419"

/**
 * When you extend from this class, make sure to annotate it with `@RunWith(RobolectricTestRunner::class)`.
 * @param locales The list of locales to check.
 *  If the list is empty, the locales will be read from [android.content.res.AssetManager.getLocales].
 * @param stringResourcesClass The string resources class to check.
 * @param pluralsResourcesClass The plurals resources class to check.
 */
@RequiresApi(Build.VERSION_CODES.N)
abstract class BaseStringResourcesTest(
    private var locales: List<Locale> = emptyList(),
    private val stringResourcesClass: Class<*>,
    private val pluralsResourcesClass: Class<*>
) {
    private lateinit var appContext: Context

    /**
     * @param locales An array of locales in the format on Android's configuration qualifiers
     *  (e.g. "en", "en-rUS" or "b+es+419").
     */
    constructor(
        locales: Array<String>,
        stringResourcesClass: Class<*>,
        pluralsResourcesClass: Class<*>
    ) : this(locales.toLocales(), stringResourcesClass, pluralsResourcesClass)

    @Before
    fun setUp() {
        appContext = ApplicationProvider.getApplicationContext()
        if (locales.isEmpty()) {
            locales = appContext.assets.locales.map { Locale.forLanguageTag(it) }
        }
    }

    @Test
    fun validatePlurals() {
        val referenceLocale = Locale.ENGLISH
        val referenceResources = resourcesForLocale(appContext, referenceLocale)
        val referencePlurals = getPluralResources(pluralsResourcesClass, referenceLocale, referenceResources)

        val validationErrors = locales.flatMap { locale ->
            val localizedResources = resourcesForLocale(appContext, locale)
            getPluralResources(pluralsResourcesClass, locale, localizedResources).flatMap { localizedPluralRes ->
                val referencePluralRes = referencePlurals.find { it.id == localizedPluralRes.id }
                if (referencePluralRes == null) {
                    listOf(
                        PluralValidationError(
                            message = "There is no base plural for ${localizedPluralRes.id}.",
                            resource = localizedPluralRes,
                            quantity = null,
                            quantityKeyword = null
                        )
                    )
                } else {
                    validatePlural(
                        locale,
                        localizedPluralRes,
                        localizedResources,
                        referenceResources = referenceResources
                    )
                }
            }
        }

        checkForErrors(validationErrors)
    }

    @Test
    fun validateStrings() {
        val appContext = ApplicationProvider.getApplicationContext<Context>()
        val referenceLocale = Locale.ENGLISH
        val referenceResources = resourcesForLocale(appContext, referenceLocale)
        val referenceStrings = getStringResources(stringResourcesClass, referenceLocale, referenceResources)

        val validationErrors = locales.flatMap { locale ->
            val localizedResources = resourcesForLocale(appContext, locale)
            getStringResources(stringResourcesClass, locale, localizedResources).mapNotNull { localizedStringRes ->
                val referenceStringRes = referenceStrings.find { it.id == localizedStringRes.id }
                if (referenceStringRes == null) {
                    StringValidationError(
                        message = "There is no base string for ${localizedStringRes.id}.",
                        resource = localizedStringRes
                    )
                } else {
                    validateString(localizedStringRes, localizedResources, referenceStringRes.text)
                }
            }
        }

        checkForErrors(validationErrors)
    }

    private fun validatePlural(
        locale: Locale,
        pluralRes: PluralRes,
        resources: Resources,
        referenceResources: Resources
    ): List<ValidationError> {
        val localizedRules = PluralRules.forLocale(locale)
        return pluralRes.texts.mapNotNull { (quantityKeyword, _) ->
            validatePluralQuantity(
                localizedRules = localizedRules,
                pluralRes,
                quantityKeyword,
                resources = resources,
                referenceResources = referenceResources
            )
        }
    }

    @Suppress("ReturnCount")
    private fun validatePluralQuantity(
        localizedRules: PluralRules,
        pluralRes: PluralRes,
        quantityKeyword: QuantityKeyword,
        resources: Resources,
        referenceResources: Resources
    ): ValidationError? {
        val text = pluralRes.texts[quantityKeyword]!!
        val textParams = extractParamTypes(text)
        val quantitySamples = localizedRules.getSamples(quantityKeyword).filterInts()
        val quantity = quantitySamples.first()

        // For reference, use the "other" quantity, which should always contain at least one parameter.
        val referenceText = referenceResources.getQuantityText(pluralRes.id, 10)
        val referenceParams = extractParamTypes(referenceText)

        checkSpans(referenceText, text)?.let { errorMessage ->
            return PluralValidationError(
                message = errorMessage,
                resource = pluralRes,
                quantity = quantity,
                quantityKeyword = quantityKeyword
            )
        }

        // Note: in some languages, there can be more than one possible value for the quantity "one" (e.g. `1` and `0`).
        // However, most translations ignore that, and they don't include the string parameter.
        // That's why we include `|| quantityKeyword == "one"` in the first condition.
        if (quantitySamples.size == 1 || quantityKeyword == "one") {
            // The `text` may have one less parameter than the reference.
            if (textParams.size !in referenceParams.size - 1..referenceParams.size ||
                !referenceParams.containsAll(textParams)
            ) {
                return PluralValidationError(
                    message = "mismatched params, expected a subset of $referenceParams but got $textParams",
                    resource = pluralRes,
                    quantity = quantity,
                    quantityKeyword = quantityKeyword
                )
            }
        } else {
            // The `text` should have the same number of parameters as the reference.
            if (textParams != referenceParams) {
                return PluralValidationError(
                    message = "mismatched params, expected a set of $referenceParams but got $textParams",
                    resource = pluralRes,
                    quantity = quantity,
                    quantityKeyword = quantityKeyword
                )
            }
        }

        return try {
            if (textParams.isNotEmpty()) {
                resources.getQuantityString(pluralRes.id, quantity, *textParams.toValuesArray(quantity))
            }
            null
        } catch (e: Exception) {
            PluralValidationError(
                message = "getQuantityString() throws: $e",
                resource = pluralRes,
                quantity = quantity,
                quantityKeyword = quantityKeyword
            )
        }
    }

    @Suppress("ReturnCount")
    private fun validateString(
        string: StringRes,
        resources: Resources,
        referenceText: CharSequence
    ): ValidationError? {
        val paramTypes = extractParamTypes(string.text)
        val referenceParamTypes = extractParamTypes(referenceText)

        // Note: it's possible that a translation will not include a string parameter (on purpose),
        // even though the reference string does.
        if (!referenceParamTypes.containsAll(paramTypes) || paramTypes.size > referenceParamTypes.size) {
            return StringValidationError(
                message = "mismatched params, expected a set of $referenceParamTypes but got $paramTypes",
                resource = string
            )
        }

        checkSpans(referenceText, string.text)?.let { errorMessage ->
            return StringValidationError(
                message = errorMessage,
                resource = string
            )
        }

        return try {
            if (paramTypes.isNotEmpty()) {
                resources.getString(string.id, *referenceParamTypes.toValuesArray())
            }
            null
        } catch (e: Exception) {
            StringValidationError(
                message = "getString() throws: $e",
                resource = string
            )
        }
    }

    private companion object {
        private val simpleParamRegex = Regex("""%([a-z])""")
        private val paramRegex = Regex("""%(\d+)\$([0-9.]*[a-z])""")

        private fun Array<String>.toLocales() = map {
            val locale = languageRegex.matchEntire(it)?.let { m -> Locale(m.groupValues[0]) }
                ?: languageWithCountryRegex.matchEntire(it)?.let { m -> Locale(m.groupValues[1], m.groupValues[2]) }
                ?: languageWithRegionRegex.matchEntire(it)
                    ?.let { m -> Locale.Builder().setLanguage(m.groupValues[1]).setRegion(m.groupValues[2]).build() }
            requireNotNull(locale) { "Unexpected locale: $it." }
        }

        fun Collection<Double>.filterInts(): List<Int> = filter { it.toInt().toDouble() == it }.map { it.toInt() }

        fun Set<StringParam>.toValuesArray(intValue: Int = 5) = sortedBy { it.position }
            .map { valueForType(it.type, intValue) }
            .toTypedArray()

        private fun SpannableString.getAllSpans(): Array<out Any> = getSpans(0, length, Object::class.java)

        private fun checkForErrors(validationErrors: List<ValidationError>) {
            if (validationErrors.isEmpty()) return
            val errorMessages = validationErrors.joinToString(separator = "\n") { " - $it" }
            if (errorMessages.isNotEmpty()) {
                throw AssertionError("Found ${validationErrors.size} errors in translations:\n${errorMessages}")
            }
        }

        /**
         * Checks if any spans defined in [referenceText] are also present in [text].
         * @return Error message, or null if no error has been found.
         */
        private fun checkSpans(referenceText: CharSequence, text: CharSequence): String? {
            val referenceSpans = SpannableString(referenceText).getAllSpans()
            val textSpans = SpannableString(text).getAllSpans()

            if (referenceSpans.size != textSpans.size) {
                return "mismatched spans, expected a set of ${referenceSpans.contentToString()}" +
                        " but got ${textSpans.contentToString()}"
            }

            return null
        }

        fun extractParamTypes(text: CharSequence): Set<StringParam> {
            return buildSet {
                paramRegex.findAll(text).forEach { match ->
                    val (_, positionString, typeString) = match.groupValues

                    val position = Integer.parseInt(positionString)
                    add(StringParam(position, typeString))
                }
                simpleParamRegex.findAll(text).forEachIndexed { index, matchResult ->
                    add(StringParam(index + 1, matchResult.groupValues[1]))
                }
            }
        }

        fun getPluralResources(
            clazz: Class<*>,
            locale: Locale,
            resources: Resources
        ): Iterable<PluralRes> = getResources(clazz) { resField ->
            val resId = resField.getInt(clazz)
            val rules = PluralRules.forLocale(locale)
            val texts = rules.keywords.mapNotNull { quantityKeyword ->
                rules.getSamples(quantityKeyword)?.filterInts()?.firstOrNull()?.let { quantity ->
                    Pair(quantityKeyword, resources.getQuantityText(resId, quantity))
                }
            }.toMap()
            PluralRes(id = resId, locale = locale, name = resField.name, texts = texts)
        }

        fun getStringResources(
            clazz: Class<*>,
            locale: Locale,
            resources: Resources
        ): Iterable<StringRes> = getResources(clazz) { resField ->
            val resId = resField.getInt(clazz)
            StringRes(id = resId, locale = locale, name = resField.name, text = resources.getText(resId))
        }

        fun <T> getResources(clazz: Class<*>, extractor: (Field) -> T): Iterable<T> = clazz.declaredFields
            .filter { Modifier.isPublic(it.modifiers) && Modifier.isStatic(it.modifiers) }
            .map(extractor)

        fun resourcesForLocale(appContext: Context, locale: Locale): Resources {
            val configuration = Configuration().apply { setLocale(locale) }
            val context = appContext.createConfigurationContext(configuration)
            return context.resources
        }

        // Use quantity for int value in plurals to make them less confusing when an error is reported.
        fun valueForType(typeString: String, intValue: Int): Any = when (typeString) {
            "d" -> intValue
            "s" -> "text"
            "f" -> 1.123f
            else -> throw IllegalArgumentException("Unknown parameter type: $typeString")
        }
    }
}

private sealed interface Resource
private data class StringRes(val id: Int, val locale: Locale, val name: String, val text: CharSequence) : Resource
private data class PluralRes(
    val id: Int,
    val locale: Locale,
    val name: String,
    val texts: Map<QuantityKeyword, CharSequence>
) : Resource
private typealias QuantityKeyword = String // e.g. "one", "few", "other" etc.

private data class StringParam(val position: Int, val type: String) {
    override fun toString(): String = "%$position\$$type"
}

private sealed interface ValidationError {
    val message: String
    val resource: Resource
}

private data class PluralValidationError(
    override val message: String,
    override val resource: PluralRes,
    val quantity: Int?,
    val quantityKeyword: QuantityKeyword?
) : ValidationError {
    override fun toString(): String {
        return "${resource.locale}/${resource.name}[${quantityKeyword}]#${quantity}: $message"
    }
}

private data class StringValidationError(
    override val message: String,
    override val resource: StringRes
) : ValidationError {
    override fun toString(): String {
        return "${resource.locale}/${resource.name}: $message"
    }
}
