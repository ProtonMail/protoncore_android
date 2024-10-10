/*
 * Copyright (c) 2021 Proton Technologies AG
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

package me.proton.android.core.coreexample.db

import android.content.Context
import androidx.room.Database
import androidx.room.TypeConverters
import me.proton.core.account.data.db.AccountConverters
import me.proton.core.account.data.db.AccountDatabase
import me.proton.core.account.data.entity.AccountEntity
import me.proton.core.account.data.entity.AccountMetadataEntity
import me.proton.core.account.data.entity.SessionDetailsEntity
import me.proton.core.account.data.entity.SessionEntity
import me.proton.core.auth.data.db.AuthConverters
import me.proton.core.auth.data.db.AuthDatabase
import me.proton.core.auth.data.entity.AuthDeviceEntity
import me.proton.core.auth.data.entity.DeviceSecretEntity
import me.proton.core.auth.data.entity.MemberDeviceEntity
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
import me.proton.core.key.data.entity.PublicAddressInfoEntity
import me.proton.core.key.data.entity.PublicAddressKeyDataEntity
import me.proton.core.key.data.entity.PublicAddressKeyEntity
import me.proton.core.keytransparency.data.local.KeyTransparencyDatabase
import me.proton.core.keytransparency.data.local.entity.AddressChangeEntity
import me.proton.core.keytransparency.data.local.entity.SelfAuditResultEntity
import me.proton.core.label.data.local.LabelConverters
import me.proton.core.label.data.local.LabelDatabase
import me.proton.core.label.data.local.LabelEntity
import me.proton.core.mailsettings.data.db.MailSettingsDatabase
import me.proton.core.mailsettings.data.entity.MailSettingsEntity
import me.proton.core.notification.data.local.db.NotificationConverters
import me.proton.core.notification.data.local.db.NotificationDatabase
import me.proton.core.notification.data.local.db.NotificationEntity
import me.proton.core.observability.data.db.ObservabilityDatabase
import me.proton.core.observability.data.entity.ObservabilityEventEntity
import me.proton.core.payment.data.local.db.PaymentDatabase
import me.proton.core.payment.data.local.entity.GooglePurchaseEntity
import me.proton.core.payment.data.local.entity.PurchaseEntity
import me.proton.core.push.data.local.db.PushConverters
import me.proton.core.push.data.local.db.PushDatabase
import me.proton.core.push.data.local.db.PushEntity
import me.proton.core.telemetry.data.db.TelemetryDatabase
import me.proton.core.telemetry.data.entity.TelemetryEventEntity
import me.proton.core.user.data.db.AddressDatabase
import me.proton.core.user.data.db.UserConverters
import me.proton.core.user.data.db.UserDatabase
import me.proton.core.user.data.entity.AddressEntity
import me.proton.core.user.data.entity.AddressKeyEntity
import me.proton.core.user.data.entity.UserEntity
import me.proton.core.user.data.entity.UserKeyEntity
import me.proton.core.userrecovery.data.db.DeviceRecoveryDatabase
import me.proton.core.userrecovery.data.entity.RecoveryFileEntity
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
        PublicAddressInfoEntity::class,
        PublicAddressKeyDataEntity::class,
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
        // feature-flags
        FeatureFlagEntity::class,
        // challenge
        ChallengeFrameEntity::class,
        // push
        PushEntity::class,
        // payment
        PurchaseEntity::class,
        GooglePurchaseEntity::class,
        // observability
        ObservabilityEventEntity::class,
        // telemetry
        TelemetryEventEntity::class,
        // key-transparency
        AddressChangeEntity::class,
        SelfAuditResultEntity::class,
        // notifications
        NotificationEntity::class,
        // user-recovery
        RecoveryFileEntity::class,
        // auth-data
        DeviceSecretEntity::class,
        AuthDeviceEntity::class,
        MemberDeviceEntity::class
    ],
    version = AppDatabase.version,
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
    NotificationConverters::class,
    AuthConverters::class
)
abstract class AppDatabase :
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
    ObservabilityDatabase,
    TelemetryDatabase,
    KeyTransparencyDatabase,
    NotificationDatabase,
    DeviceRecoveryDatabase,
    AuthDatabase {

    companion object {
        const val name = "db-account-manager"
        const val version = 57

        val migrations = listOf(
            AppDatabaseMigrations.MIGRATION_1_2,
            AppDatabaseMigrations.MIGRATION_2_3,
            AppDatabaseMigrations.MIGRATION_3_4,
            AppDatabaseMigrations.MIGRATION_4_5,
            AppDatabaseMigrations.MIGRATION_5_6,
            AppDatabaseMigrations.MIGRATION_6_7,
            AppDatabaseMigrations.MIGRATION_7_8,
            AppDatabaseMigrations.MIGRATION_8_9,
            AppDatabaseMigrations.MIGRATION_9_10,
            AppDatabaseMigrations.MIGRATION_10_11,
            AppDatabaseMigrations.MIGRATION_11_12,
            AppDatabaseMigrations.MIGRATION_12_13,
            AppDatabaseMigrations.MIGRATION_13_14,
            AppDatabaseMigrations.MIGRATION_14_15,
            AppDatabaseMigrations.MIGRATION_15_16,
            AppDatabaseMigrations.MIGRATION_16_17,
            AppDatabaseMigrations.MIGRATION_17_18,
            AppDatabaseMigrations.MIGRATION_18_19,
            AppDatabaseMigrations.MIGRATION_19_20,
            AppDatabaseMigrations.MIGRATION_20_21,
            AppDatabaseMigrations.MIGRATION_21_22,
            AppDatabaseMigrations.MIGRATION_22_23,
            AppDatabaseMigrations.MIGRATION_23_24,
            AppDatabaseMigrations.MIGRATION_24_25,
            AppDatabaseMigrations.MIGRATION_25_26,
            AppDatabaseMigrations.MIGRATION_26_27,
            AppDatabaseMigrations.MIGRATION_27_28,
            AppDatabaseMigrations.MIGRATION_28_29,
            AppDatabaseMigrations.MIGRATION_29_30,
            AppDatabaseMigrations.MIGRATION_30_31,
            AppDatabaseMigrations.MIGRATION_31_32,
            AppDatabaseMigrations.MIGRATION_32_33,
            AppDatabaseMigrations.MIGRATION_33_34,
            AppDatabaseMigrations.MIGRATION_34_35,
            AppDatabaseMigrations.MIGRATION_35_36,
            AppDatabaseMigrations.MIGRATION_36_37,
            AppDatabaseMigrations.MIGRATION_37_38,
            AppDatabaseMigrations.MIGRATION_38_39,
            AppDatabaseMigrations.MIGRATION_39_40,
            AppDatabaseMigrations.MIGRATION_40_41,
            AppDatabaseMigrations.MIGRATION_41_42,
            AppDatabaseMigrations.MIGRATION_42_43,
            AppDatabaseMigrations.MIGRATION_43_44,
            AppDatabaseMigrations.MIGRATION_44_45,
            AppDatabaseMigrations.MIGRATION_45_46,
            AppDatabaseMigrations.MIGRATION_46_47,
            AppDatabaseMigrations.MIGRATION_47_48,
            AppDatabaseMigrations.MIGRATION_48_49,
            AppDatabaseMigrations.MIGRATION_49_50,
            AppDatabaseMigrations.MIGRATION_50_51,
            AppDatabaseMigrations.MIGRATION_51_52,
            AppDatabaseMigrations.MIGRATION_52_53,
            AppDatabaseMigrations.MIGRATION_53_54,
            AppDatabaseMigrations.MIGRATION_54_55,
            AppDatabaseMigrations.MIGRATION_55_56,
            AppDatabaseMigrations.MIGRATION_56_57,
        )

        fun buildDatabase(context: Context): AppDatabase =
            databaseBuilder<AppDatabase>(context, name)
                .apply { migrations.forEach { addMigrations(it) } }
                .build()
    }
}
