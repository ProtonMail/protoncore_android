package me.proton.core.usersettings.domain.entity

data class OrganizationSettings(
    val logoId: String?,
    val allowedProducts: List<String>?,
)
