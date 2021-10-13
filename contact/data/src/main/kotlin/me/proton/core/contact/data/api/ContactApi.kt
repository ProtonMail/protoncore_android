/*
 * Copyright (c) 2020 Proton Technologies AG
 * This file is part of Proton Technologies AG and ProtonCore.
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

package me.proton.core.contact.data.api

import me.proton.core.contact.data.api.request.CreateContactsRequest
import me.proton.core.contact.data.api.request.DeleteContactsRequest
import me.proton.core.contact.data.api.resource.ContactCardsResource
import me.proton.core.contact.data.api.response.CreateContactsResponse
import me.proton.core.contact.data.api.response.DeleteContactsResponse
import me.proton.core.contact.data.api.response.GetContactEmailsResponse
import me.proton.core.contact.data.api.response.GetContactResponse
import me.proton.core.contact.data.api.response.GetContactsResponse
import me.proton.core.contact.data.api.response.MutateContactResponse
import me.proton.core.network.data.protonApi.BaseRetrofitApi
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ContactApi : BaseRetrofitApi {

    @GET("contacts/v4/contacts/{id}")
    suspend fun getContact(@Path("id") contactId: String): GetContactResponse

    @POST("contacts/v4/contacts")
    suspend fun createContacts(@Body request: CreateContactsRequest): CreateContactsResponse

    @GET("contacts/v4/contacts")
    suspend fun getContacts(
        @Query("Page") page: Int,
        @Query("PageSize") pageSize: Int,
    ): GetContactsResponse

    @GET("contacts/v4/contacts/emails")
    suspend fun getContactEmails(
        @Query("Page") page: Int,
        @Query("PageSize") pageSize: Int,
    ): GetContactEmailsResponse

    @PUT("contacts/v4/contacts/delete")
    suspend fun deleteContacts(@Body request: DeleteContactsRequest): DeleteContactsResponse

    @PUT("contacts/v4/contacts/{id}")
    suspend fun updateContact(
        @Path("id") contactId: String,
        @Body request: ContactCardsResource
    ): MutateContactResponse
}
