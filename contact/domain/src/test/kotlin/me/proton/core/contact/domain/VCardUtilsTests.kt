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

package me.proton.core.contact.domain

import ezvcard.Ezvcard
import org.junit.Test
import kotlin.test.assertTrue

class VCardUtilsTests {

    @Test
    fun `extract pinned keys from VCard according to PREF order`() {

        val vCardWithPref2BeforePref1 = """
            BEGIN:VCARD
            VERSION:4.0
            FN;PREF=1:calendar@proton.black
            ITEM1.EMAIL;PREF=1:calendar@proton.black
            UID:proton-web-4e57f941-d1b4-7909-c879-73a2df5513f1
            ITEM1.KEY;PREF=2:data:application/pgp-keys;base64,xjMEZKMDghYJKwYBBAHaRw8BA
             QdAGems3V25edmSzHRFKePBU7692nBprwN3uZmapAjgH+bNLWNhbGVuZGFyQHByb3Rvbi5ibGFj
             ayA8Y2FsZW5kYXJAcHJvdG9uLmJsYWNrPsKPBBMWCABBBQJkowOCCZDXZ5WySpsJSRYhBIhRe96
             tJFIFkIiG7ddnlbJKmwlJAhsDAh4BAhkBAwsJBwIVCAMWAAIFJwkCBwIAAKfmAQDwrg8xuiTdYi
             xQFoK1fr/UREL2FpHE1pE9QLwO0slSsAD7BKMYfsSykTUxonqiJ+CK67EETbYIt+fIndA2GVaP5
             gDOOARkowOCEgorBgEEAZdVAQUBAQdAVllU+SypP4bqiTvgGIgg2O3GEpMUTE8MBkRdZEZIGRkD
             AQoJwngEGBYIACoFAmSjA4IJkNdnlbJKmwlJFiEEiFF73q0kUgWQiIbt12eVskqbCUkCGwwAAFd
             vAQCCv7i8tCLAp4Mwzh7fsOKQxqdq6tlq+FB/TPuaKGQiIgEAoSVwDasnAMoeH9GKjP29ExWORv
             PmxsmDMe0uzZy4xA0=
            ITEM1.KEY;PREF=1:data:application/pgp-keys;base64,xjMEYIE/zBYJKwYBBAHaRw8BA
             QdAU0kzBdPct+/iReob+92uE1hEJPzoXnrrTqx5p8EoOa7NLWNhbGVuZGFyQHByb3Rvbi5ibGFj
             ayA8Y2FsZW5kYXJAcHJvdG9uLmJsYWNrPsKPBBAWCgAgBQJggT/MBgsJBwgDAgQVCAoCBBYCAQA
             CGQECGwMCHgEAIQkQ9LTBFWUbz9MWIQQL9ztQ8o2jSXASlPX0tMEVZRvP09xeAQD3ioSt4E6SyV
             xOeS8xBQvhuEXkqBKKZCkMO10fd0P2LgD/WvtGpRv8JAll0feMgG2y1lufZtJImTeLr0ciYb7AE
             gnOOARggT/MEgorBgEEAZdVAQUBAQdAJYTJ0NuH3zSCNxk+gsFNTVHuPDLQQLRsyNermAbrEXID
             AQgHwngEGBYIAAkFAmCBP8wCGwwAIQkQ9LTBFWUbz9MWIQQL9ztQ8o2jSXASlPX0tMEVZRvP0/e
             ZAQC9vSk4lPi9v1dMHsbKCChrYPR2WCMSUXykpNcDuP2TBgEA0jjgSKW351PQTmHU15UcSFY71O
             pD+j04Cs4EcONklw0=
            ITEM1.X-PM-ENCRYPT:true
            ITEM1.X-PM-SIGN:true
            END:VCARD
        """.trimIndent()

        val vCard = Ezvcard.parse(vCardWithPref2BeforePref1).first()

        val vCardKeyPref2 = vCard.keys[0]
        val vCardKeyPref1 = vCard.keys[1]

        val extractedKeysInPrefOrder = vCard.getKeysForGroup("ITEM1")

        assertTrue(extractedKeysInPrefOrder[0].contentEquals(vCardKeyPref1.data))
        assertTrue(extractedKeysInPrefOrder[1].contentEquals(vCardKeyPref2.data))

    }

}

