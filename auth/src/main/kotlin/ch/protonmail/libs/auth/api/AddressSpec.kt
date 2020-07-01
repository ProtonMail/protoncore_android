package ch.protonmail.libs.auth.api

import ch.protonmail.libs.auth.model.request.AddressSetupBody
import ch.protonmail.libs.auth.model.response.AddressSetupResponse

internal interface AddressSpec {

    //suspend fun fetchAddresses() : AddressesResponse

    //suspend fun updateAlias(addressIds: List<String>): ResponseBody

    suspend fun setupAddress(addressSetupBody: AddressSetupBody): AddressSetupResponse

    //suspend fun editAddress(addressId: String, displayName: String, signature: String): ResponseBody
}
