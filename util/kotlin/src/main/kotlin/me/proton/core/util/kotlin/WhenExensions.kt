package me.proton.core.util.kotlin

/**
 * Convert a statement to an expression.
 *
 * Usage: when (it) { ... }.exhaustive, will cause the compiler to enforce all cases are covered.
 *
 * Note : 'when' can be used either as an expression or as a statement.
 *
 * If it is used as an expression, the value of the satisfied branch becomes the value of the overall expression.
 * If it is used as a statement, the values of individual branches are ignored.
 * If it is used as an expression, the else branch is mandatory, unless the compiler can prove that all possible cases are covered.
 */
val <T> T.exhaustive: T
    get() = this
