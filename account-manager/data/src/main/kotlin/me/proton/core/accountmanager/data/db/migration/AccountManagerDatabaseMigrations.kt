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

package me.proton.core.accountmanager.data.db.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

internal val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Added Table HumanVerificationDetailsEntity
        database.execSQL("CREATE TABLE IF NOT EXISTS `HumanVerificationDetailsEntity` (`sessionId` TEXT NOT NULL, `verificationMethods` TEXT NOT NULL, `captchaVerificationToken` TEXT, PRIMARY KEY(`sessionId`), FOREIGN KEY(`sessionId`) REFERENCES `SessionEntity`(`sessionId`) ON UPDATE NO ACTION ON DELETE CASCADE )")
        database.execSQL("CREATE INDEX IF NOT EXISTS `index_HumanVerificationDetailsEntity_sessionId` ON `HumanVerificationDetailsEntity` (`sessionId`)")

    }
}

internal val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Added Table SessionDetailsEntity.
        database.execSQL("CREATE TABLE IF NOT EXISTS `SessionDetailsEntity` (`sessionId` TEXT NOT NULL, `initialEventId` TEXT NOT NULL, `requiredAccountType` TEXT NOT NULL, `secondFactorEnabled` INTEGER NOT NULL, `twoPassModeEnabled` INTEGER NOT NULL, `password` TEXT, PRIMARY KEY(`sessionId`), FOREIGN KEY(`sessionId`) REFERENCES `SessionEntity`(`sessionId`) ON UPDATE NO ACTION ON DELETE CASCADE )")
        // Added Table UserEntity, UserKeyEntity, AddressEntity, AddressKeyEntity, KeySaltEntity, PublicAddressEntity, PublicAddressKeyEntity.
        database.execSQL("CREATE TABLE IF NOT EXISTS `UserEntity` (`userId` TEXT NOT NULL, `email` TEXT, `name` TEXT, `displayName` TEXT, `currency` TEXT NOT NULL, `credit` INTEGER NOT NULL, `usedSpace` INTEGER NOT NULL, `maxSpace` INTEGER NOT NULL, `maxUpload` INTEGER NOT NULL, `role` INTEGER, `private` INTEGER NOT NULL, `subscribed` INTEGER NOT NULL, `services` INTEGER NOT NULL, `delinquent` INTEGER, `passphrase` BLOB, PRIMARY KEY(`userId`), FOREIGN KEY(`userId`) REFERENCES `AccountEntity`(`userId`) ON UPDATE NO ACTION ON DELETE CASCADE )")
        database.execSQL("CREATE INDEX IF NOT EXISTS `index_UserEntity_userId` ON `UserEntity` (`userId`)")

        database.execSQL("CREATE TABLE IF NOT EXISTS `UserKeyEntity` (`userId` TEXT NOT NULL, `keyId` TEXT NOT NULL, `version` INTEGER NOT NULL, `privateKey` TEXT NOT NULL, `isPrimary` INTEGER NOT NULL, `fingerprint` TEXT, `activation` TEXT, PRIMARY KEY(`keyId`), FOREIGN KEY(`userId`) REFERENCES `UserEntity`(`userId`) ON UPDATE NO ACTION ON DELETE CASCADE )")
        database.execSQL("CREATE INDEX IF NOT EXISTS `index_UserKeyEntity_userId` ON `UserKeyEntity` (`userId`)")
        database.execSQL("CREATE INDEX IF NOT EXISTS `index_UserKeyEntity_keyId` ON `UserKeyEntity` (`keyId`)")

        database.execSQL("CREATE TABLE IF NOT EXISTS `AddressEntity` (`userId` TEXT NOT NULL, `addressId` TEXT NOT NULL, `email` TEXT NOT NULL, `displayName` TEXT, `domainId` TEXT, `canSend` INTEGER NOT NULL, `canReceive` INTEGER NOT NULL, `enabled` INTEGER NOT NULL, `type` INTEGER, `order` INTEGER NOT NULL, PRIMARY KEY(`addressId`), FOREIGN KEY(`userId`) REFERENCES `UserEntity`(`userId`) ON UPDATE NO ACTION ON DELETE CASCADE )")
        database.execSQL("CREATE INDEX IF NOT EXISTS `index_AddressEntity_addressId` ON `AddressEntity` (`addressId`)")
        database.execSQL("CREATE INDEX IF NOT EXISTS `index_AddressEntity_userId` ON `AddressEntity` (`userId`)")

        database.execSQL("CREATE TABLE IF NOT EXISTS `AddressKeyEntity` (`addressId` TEXT NOT NULL, `keyId` TEXT NOT NULL, `version` INTEGER NOT NULL, `privateKey` TEXT NOT NULL, `isPrimary` INTEGER NOT NULL, `flags` INTEGER NOT NULL, `token` TEXT, `signature` TEXT, `fingerprint` TEXT, `fingerprints` TEXT, `activation` TEXT, `active` INTEGER NOT NULL, PRIMARY KEY(`keyId`), FOREIGN KEY(`addressId`) REFERENCES `AddressEntity`(`addressId`) ON UPDATE NO ACTION ON DELETE CASCADE )")
        database.execSQL("CREATE INDEX IF NOT EXISTS `index_AddressKeyEntity_addressId` ON `AddressKeyEntity` (`addressId`)")
        database.execSQL("CREATE INDEX IF NOT EXISTS `index_AddressKeyEntity_keyId` ON `AddressKeyEntity` (`keyId`)")

        database.execSQL("CREATE TABLE IF NOT EXISTS `KeySaltEntity` (`userId` TEXT NOT NULL, `keyId` TEXT NOT NULL, `keySalt` TEXT, PRIMARY KEY(`userId`, `keyId`))")
        database.execSQL("CREATE INDEX IF NOT EXISTS `index_KeySaltEntity_userId` ON `KeySaltEntity` (`userId`)")
        database.execSQL("CREATE INDEX IF NOT EXISTS `index_KeySaltEntity_keyId` ON `KeySaltEntity` (`keyId`)")

        database.execSQL("CREATE TABLE IF NOT EXISTS `PublicAddressEntity` (`email` TEXT NOT NULL, `recipientType` INTEGER NOT NULL, `mimeType` TEXT, PRIMARY KEY(`email`))")
        database.execSQL("CREATE INDEX IF NOT EXISTS `index_PublicAddressEntity_email` ON `PublicAddressEntity` (`email`)")

        database.execSQL("CREATE TABLE IF NOT EXISTS `PublicAddressKeyEntity` (`email` TEXT NOT NULL, `flags` INTEGER NOT NULL, `publicKey` TEXT NOT NULL, `isPrimary` INTEGER NOT NULL, PRIMARY KEY(`email`, `publicKey`), FOREIGN KEY(`email`) REFERENCES `PublicAddressEntity`(`email`) ON UPDATE NO ACTION ON DELETE CASCADE )")
        database.execSQL("CREATE INDEX IF NOT EXISTS `index_PublicAddressKeyEntity_email` ON `PublicAddressKeyEntity` (`email`)")
    }
}
