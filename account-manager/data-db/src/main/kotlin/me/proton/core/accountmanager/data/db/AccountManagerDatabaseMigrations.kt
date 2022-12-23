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

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import me.proton.core.account.data.db.AccountDatabase
import me.proton.core.challenge.data.db.ChallengeDatabase
import me.proton.core.contact.data.local.db.ContactDatabase
import me.proton.core.eventmanager.data.db.EventMetadataDatabase
import me.proton.core.featureflag.data.db.FeatureFlagDatabase
import me.proton.core.humanverification.data.db.HumanVerificationDatabase
import me.proton.core.key.data.db.KeySaltDatabase
import me.proton.core.key.data.db.PublicAddressDatabase
import me.proton.core.label.data.local.LabelDatabase
import me.proton.core.mailsettings.data.db.MailSettingsDatabase
import me.proton.core.payment.data.local.db.PaymentDatabase
import me.proton.core.push.data.local.db.PushDatabase
import me.proton.core.user.data.db.AddressDatabase
import me.proton.core.user.data.db.UserDatabase
import me.proton.core.usersettings.data.db.OrganizationDatabase
import me.proton.core.usersettings.data.db.UserSettingsDatabase

object AccountManagerDatabaseMigrations {

    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            AccountDatabase.MIGRATION_1.migrate(database)
        }
    }

    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(database: SupportSQLiteDatabase) {
            AccountDatabase.MIGRATION_2.migrate(database)
            UserDatabase.MIGRATION_0.migrate(database)
            AddressDatabase.MIGRATION_0.migrate(database)
            KeySaltDatabase.MIGRATION_0.migrate(database)
            PublicAddressDatabase.MIGRATION_0.migrate(database)
        }
    }

    val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(database: SupportSQLiteDatabase) {
            AddressDatabase.MIGRATION_1.migrate(database)
        }
    }

    val MIGRATION_4_5 = object : Migration(4, 5) {
        override fun migrate(database: SupportSQLiteDatabase) {
            AccountDatabase.MIGRATION_3.migrate(database)
            HumanVerificationDatabase.MIGRATION_0.migrate(database)
        }
    }

    val MIGRATION_5_6 = object : Migration(5, 6) {
        override fun migrate(database: SupportSQLiteDatabase) {
            MailSettingsDatabase.MIGRATION_0.migrate(database)
        }
    }

    val MIGRATION_6_7 = object : Migration(6, 7) {
        override fun migrate(database: SupportSQLiteDatabase) {
            UserSettingsDatabase.MIGRATION_0.migrate(database)
        }
    }

    val MIGRATION_7_8 = object : Migration(7, 8) {
        override fun migrate(database: SupportSQLiteDatabase) {
            OrganizationDatabase.MIGRATION_0.migrate(database)
        }
    }

    val MIGRATION_8_9 = object : Migration(8, 9) {
        override fun migrate(database: SupportSQLiteDatabase) {
            ContactDatabase.MIGRATION_0.migrate(database)
        }
    }

    val MIGRATION_9_10 = object : Migration(9, 10) {
        override fun migrate(database: SupportSQLiteDatabase) {
            AddressDatabase.MIGRATION_2.migrate(database)
            PublicAddressDatabase.MIGRATION_1.migrate(database)
        }
    }

    val MIGRATION_10_11 = object : Migration(10, 11) {
        override fun migrate(database: SupportSQLiteDatabase) {
            EventMetadataDatabase.MIGRATION_0.migrate(database)
        }
    }

    val MIGRATION_11_12 = object : Migration(11, 12) {
        override fun migrate(database: SupportSQLiteDatabase) {
            AccountDatabase.MIGRATION_4.migrate(database)
            AddressDatabase.MIGRATION_3.migrate(database)
            UserDatabase.MIGRATION_1.migrate(database)
        }
    }

    val MIGRATION_12_13 = object : Migration(12, 13) {
        override fun migrate(database: SupportSQLiteDatabase) {
            LabelDatabase.MIGRATION_0.migrate(database)
        }
    }

    val MIGRATION_13_14 = object : Migration(13, 14) {
        override fun migrate(database: SupportSQLiteDatabase) {
            FeatureFlagDatabase.MIGRATION_0.migrate(database)
        }
    }

    val MIGRATION_14_15 = object : Migration(14, 15) {
        override fun migrate(database: SupportSQLiteDatabase) {
            OrganizationDatabase.MIGRATION_1.migrate(database)
        }
    }

    val MIGRATION_15_16 = object : Migration(15, 16) {
        override fun migrate(database: SupportSQLiteDatabase) {
            FeatureFlagDatabase.MIGRATION_1.migrate(database)
        }
    }

    val MIGRATION_16_17 = object : Migration(16, 17) {
        override fun migrate(database: SupportSQLiteDatabase) {
            ChallengeDatabase.MIGRATION_0.migrate(database)
        }
    }

    val MIGRATION_17_18 = object : Migration(17, 18) {
        override fun migrate(database: SupportSQLiteDatabase) {
            ChallengeDatabase.MIGRATION_1.migrate(database)
        }
    }

    val MIGRATION_18_19 = object : Migration(18, 19) {
        override fun migrate(database: SupportSQLiteDatabase) {
            UserSettingsDatabase.MIGRATION_1.migrate(database)
        }
    }

    val MIGRATION_19_20 = object : Migration(19, 20) {
        override fun migrate(database: SupportSQLiteDatabase) {
            PushDatabase.MIGRATION_0.migrate(database)
        }
    }

    val MIGRATION_20_21 = object : Migration(20, 21) {
        override fun migrate(database: SupportSQLiteDatabase) {
            FeatureFlagDatabase.MIGRATION_2.migrate(database)
        }
    }

    val MIGRATION_21_22 = object : Migration(21, 22) {
        override fun migrate(database: SupportSQLiteDatabase) {
            FeatureFlagDatabase.MIGRATION_3.migrate(database)
        }
    }

    val MIGRATION_22_23 = object : Migration(22, 23) {
        override fun migrate(database: SupportSQLiteDatabase) {
            HumanVerificationDatabase.MIGRATION_1.migrate(database)
        }
    }

    val MIGRATION_23_24 = object : Migration(23, 24) {
        override fun migrate(database: SupportSQLiteDatabase) {
            HumanVerificationDatabase.MIGRATION_2.migrate(database)
        }
    }

    val MIGRATION_24_25 = object : Migration(24, 25) {
        override fun migrate(database: SupportSQLiteDatabase) {
            PaymentDatabase.MIGRATION_0.migrate(database)
        }
    }

    val MIGRATION_25_26 = object : Migration(25, 26) {
        override fun migrate(database: SupportSQLiteDatabase) {
            AccountDatabase.MIGRATION_5.migrate(database)
        }
    }
}
