package ch.protonmail.libs.lint.categories

import com.android.tools.lint.detector.api.Category

/**
 * Accessory categories to [com.android.tools.lint.detector.api.Category]
 * @author Davide Farella
 */
object Category {

    /** Issues related to performance  */
    @JvmField
    val TESTABILITY = Category.create("Testability", 65)
}
