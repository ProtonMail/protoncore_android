package me.proton.core.humanverification.presentation.entity


/**
 * Created by dinokadrikj on 6/12/20.
 */
data class HumanVerificationModel(
    val verifyMethods: List<String>, // Only provided if Direct = 1
    val token: String
)
