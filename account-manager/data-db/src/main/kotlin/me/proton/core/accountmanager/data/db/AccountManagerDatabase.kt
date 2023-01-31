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

package me.proton.core.accountmanager.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.TypeConverters
import me.proton.core.account.data.db.AccountConverters
import me.proton.core.account.data.db.AccountDatabase
import me.proton.core.account.data.entity.AccountEntity
import me.proton.core.account.data.entity.AccountMetadataEntity
import me.proton.core.account.data.entity.SessionDetailsEntity
import me.proton.core.account.data.entity.SessionEntity
import me.proton.core.challenge.data.db.ChallengeConverters
import me.proton.core.challenge.data.db.ChallengeDatabase
import me.proton.core.challenge.data.entity.ChallengeFrameEntity
import me.proton.core.contact.data.local.db.ContactConverters
import me.proton.core.contact.data.local.db.ContactDatabase
import me.proton.core.contact.data.local.db.entity.ContactCardEntity
import me.proton.core.contact.data.local.db.entity.ContactEmailEntity
import me.proton.core.contact.data.local.db.entity.ContactEmailLabelEntity
import me.proton.core.contact.data.local.db.entity.ContactEntity
import me.proton.core.crypto.android.keystore.CryptoConverters
import me.proton.core.data.room.db.BaseDatabase
import me.proton.core.data.room.db.CommonConverters
import me.proton.core.eventmanager.data.db.EventManagerConverters
import me.proton.core.eventmanager.data.db.EventMetadataDatabase
import me.proton.core.eventmanager.data.entity.EventMetadataEntity
import me.proton.core.featureflag.data.db.FeatureFlagDatabase
import me.proton.core.featureflag.data.entity.FeatureFlagEntity
import me.proton.core.humanverification.data.db.HumanVerificationConverters
import me.proton.core.humanverification.data.db.HumanVerificationDatabase
import me.proton.core.humanverification.data.entity.HumanVerificationEntity
import me.proton.core.key.data.db.KeySaltDatabase
import me.proton.core.key.data.db.PublicAddressDatabase
import me.proton.core.key.data.entity.KeySaltEntity
import me.proton.core.key.data.entity.PublicAddressEntity
import me.proton.core.key.data.entity.PublicAddressKeyEntity
import me.proton.core.label.data.local.LabelConverters
import me.proton.core.label.data.local.LabelDatabase
import me.proton.core.label.data.local.LabelEntity
import me.proton.core.mailsettings.data.db.MailSettingsDatabase
import me.proton.core.mailsettings.data.entity.MailSettingsEntity
import me.proton.core.observability.data.db.ObservabilityDatabase
import me.proton.core.observability.data.entity.ObservabilityEventEntity
import me.proton.core.payment.data.local.db.PaymentDatabase
import me.proton.core.payment.data.local.entity.GooglePurchaseEntity
import me.proton.core.push.data.local.db.PushConverters
import me.proton.core.push.data.local.db.PushDatabase
import me.proton.core.push.data.local.db.PushEntity
import me.proton.core.user.data.db.AddressDatabase
import me.proton.core.user.data.db.UserConverters
import me.proton.core.user.data.db.UserDatabase
import me.proton.core.user.data.entity.AddressEntity
import me.proton.core.user.data.entity.AddressKeyEntity
import me.proton.core.user.data.entity.UserEntity
import me.proton.core.user.data.entity.UserKeyEntity
import me.proton.core.usersettings.data.db.OrganizationDatabase
import me.proton.core.usersettings.data.db.UserSettingsConverters
import me.proton.core.usersettings.data.db.UserSettingsDatabase
import me.proton.core.usersettings.data.entity.OrganizationEntity
import me.proton.core.usersettings.data.entity.OrganizationKeysEntity
import me.proton.core.usersettings.data.entity.UserSettingsEntity

@Database(
    entities = [
        // account-data
        AccountEntity::class,
        AccountMetadataEntity::class,
        SessionEntity::class,
        SessionDetailsEntity::class,
        // user-data
        UserEntity::class,
        UserKeyEntity::class,
        AddressEntity::class,
        AddressKeyEntity::class,
        // key-data
        KeySaltEntity::class,
        PublicAddressEntity::class,
        PublicAddressKeyEntity::class,
        // human-verification
        HumanVerificationEntity::class,
        // mail-settings
        MailSettingsEntity::class,
        // user-settings
        UserSettingsEntity::class,
        // organization
        OrganizationEntity::class,
        OrganizationKeysEntity::class,
        // contact
        ContactEntity::class,
        ContactCardEntity::class,
        ContactEmailEntity::class,
        ContactEmailLabelEntity::class,
        // event-manager
        EventMetadataEntity::class,
        // label
        LabelEntity::class,
        // feature-flag
        FeatureFlagEntity::class,
        // challenge
        ChallengeFrameEntity::class,
        // push
        PushEntity::class,
        // payment
        GooglePurchaseEntity::class,
        // observability
        ObservabilityEventEntity::class
    ],
    version = AccountManagerDatabase.version,
    exportSchema = true
)
@TypeConverters(
    CommonConverters::class,
    AccountConverters::class,
    UserConverters::class,
    CryptoConverters::class,
    HumanVerificationConverters::class,
    UserSettingsConverters::class,
    ContactConverters::class,
    EventManagerConverters::class,
    LabelConverters::class,
    ChallengeConverters::class,
    PushConverters::class,
)
abstract class AccountManagerDatabase :
    BaseDatabase(),
    AccountDatabase,
    UserDatabase,
    AddressDatabase,
    KeySaltDatabase,
    HumanVerificationDatabase,
    PublicAddressDatabase,
    MailSettingsDatabase,
    UserSettingsDatabase,
    OrganizationDatabase,
    ContactDatabase,
    EventMetadataDatabase,
    LabelDatabase,
    FeatureFlagDatabase,
    ChallengeDatabase,
    PushDatabase,
    PaymentDatabase,
    ObservabilityDatabase {

    companion object {
        const val name = "db-account-manager"
        const val version = 27

        val migrations = listOf(
            AccountManagerDatabaseMigrations.MIGRATION_1_2,
            AccountManagerDatabaseMigrations.MIGRATION_2_3,
            AccountManagerDatabaseMigrations.MIGRATION_3_4,
            AccountManagerDatabaseMigrations.MIGRATION_4_5,
            AccountManagerDatabaseMigrations.MIGRATION_5_6,
            AccountManagerDatabaseMigrations.MIGRATION_6_7,
            AccountManagerDatabaseMigrations.MIGRATION_7_8,
            AccountManagerDatabaseMigrations.MIGRATION_8_9,
            AccountManagerDatabaseMigrations.MIGRATION_9_10,
            AccountManagerDatabaseMigrations.MIGRATION_10_11,
            AccountManagerDatabaseMigrations.MIGRATION_11_12,
            AccountManagerDatabaseMigrations.MIGRATION_12_13,
            AccountManagerDatabaseMigrations.MIGRATION_13_14,
            AccountManagerDatabaseMigrations.MIGRATION_14_15,
            AccountManagerDatabaseMigrations.MIGRATION_15_16,
            AccountManagerDatabaseMigrations.MIGRATION_16_17,
            AccountManagerDatabaseMigrations.MIGRATION_17_18,
            AccountManagerDatabaseMigrations.MIGRATION_18_19,
            AccountManagerDatabaseMigrations.MIGRATION_19_20,
            AccountManagerDatabaseMigrations.MIGRATION_20_21,
            AccountManagerDatabaseMigrations.MIGRATION_21_22,
            AccountManagerDatabaseMigrations.MIGRATION_22_23,
            AccountManagerDatabaseMigrations.MIGRATION_23_24,
            AccountManagerDatabaseMigrations.MIGRATION_24_25,
            AccountManagerDatabaseMigrations.MIGRATION_25_26,
            AccountManagerDatabaseMigrations.MIGRATION_26_27,
        )

        fun databaseBuilder(context: Context): Builder<AccountManagerDatabase> =
            databaseBuilder<AccountManagerDatabase>(context, name)
                .apply { migrations.forEach { addMigrations(it) } }

        fun buildDatabase(context: Context): AccountManagerDatabase = databaseBuilder(context).build()
    }
}
