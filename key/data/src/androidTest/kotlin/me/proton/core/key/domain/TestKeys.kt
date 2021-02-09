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

package me.proton.core.key.domain

import me.proton.core.crypto.common.pgp.Armored

object TestKeys {
    val privateKey1_Passphrase = "7NgO4d0h72zt4XuFLOUbg352vhrn.tu".toByteArray()
    val privateKey1: Armored =
        """
        -----BEGIN PGP PRIVATE KEY BLOCK-----
        Version: ProtonMail
        
        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
        dlEt7f8XNvX3HxQw9w==
        =FW0u
        -----END PGP PRIVATE KEY BLOCK-----
        """.trimIndent()

    val privateKey1_PublicKey: Armored =
        """
        -----BEGIN PGP PUBLIC KEY BLOCK-----
        Version: GopenPGP 2.1.1
        Comment: https://gopenpgp.org

        xsBNBF1BfxUBCADUpiiG3AhQK08E2nBmQ50XeztOWArmknINQV41pqGFW5VQkfbQ
        3FYsANhLGqbDBQ0XxmocjKL7W7W8Y4xmHCGgkCUy6gAqGbi+sXY9Sl8xqQNHuZDh
        WVdqT8+Rtv+DRxp/XrGkzC1U8CBYUmmKS92ldy0/zZIvgQXT6t5Q+v+BeUSv4jCs
        nY3BE0UBOljtrTXlOcXRZHQxORWG+kon0qgcJERdwwzhxY6eT8jEfAfJY0hzQaYg
        +6bj6ZR0zkMtY2Psq2M05kzEw4On/dezZETAu1e9fSqfk1mp+H6BeLJ9RUyrFK/P
        qIO48+pU8CmAvTdx5eIihyOM16CFg/3GgV85ABEBAAHNMWFkYW10c3RAcHJvdG9u
        bWFpbC5ibHVlIDxhZGFtdHN0QHByb3Rvbm1haWwuYmx1ZT7CwGgEEwEIABwFAl1B
        fxUJEBHDHo5eB/TQAhsDAhkBAgsJAhUIAADHoggACDYDZkyTMZX69k9uoAygAQ75
        2kb52r0L3dSLge+hUelxJOiVUznbavzVhzjzF2FucXP0csOSJygHNejjS62BDtsX
        iIoPiVDO1+Hr72X4gE277VeZ1b6VozJvKC0+H4fhg/EtkD07oVhHJRxOOVlgUXGK
        nP2lz2ojny0McggvN+Nk8ILqt6ImlvEk6CnTs9XdmcmosMiQU+U+THKrKZ+5Yec8
        4kzlHG8ee7Tim2yn9n/FuBStrYkTJUsDuAL/LOfF9DnzTzukK6kqpDB6kDfMeYQJ
        Lq+Tu642n74P0lqOO0Wy7imI/hxM1W8yqcNdafS7PCuGHD99mecdKWVeYHCCY87A
        TQRdQX8VAQgAyAIj6ytLB1CUinP6f6aVKlLSM9e9Figv1CAw53NHeQBbmwzU5bZn
        tE6UERnvJy6ul7hJr9Jop+O1/jA6zaGanF5wv0nEvTHcoYRpJ4QiJgiQxvhOdItH
        29+jBV1F44xOzlGnEzFAv7GbPecKHAsQgX9qYCj+5ydcttQ29gWQ6nN23G03R3Lb
        KRS9H2uw1SIYGgif8FgKpJemwJjuSibyViXTf3JC8ZUtYbq+vIXqATFFtbrUHfKM
        AKlHo0uLYGq1rRINGR6Dmhu6bGhZonuW0na4+5Wh86kg9c/YI7jSIIspRRkH+v7+
        RXH51h8Rbc2Tiv64qy7cIJIH0Bk0lFAaIQARAQABwsBfBBgBCAATBQJdQX8VCRAR
        wx6OXgf00AIbDAAAgvAIAGyLaHYTjiXG/ORIIAgdQhKBYOozrOS4EcOPNdMSPBwT
        6P+BpNS/GD33pzANVKM9Mft8+NnePUFOR2f4QJrQ1VvSj9ko8P2sX7IAe7+rG614
        LQfzjG5R16KlSVOMFW0K2L8ZxumDdYl/N0BhgtZmB1lg1xY2TPHoDetznMnHG8sL
        6u6vyhGl5a6qcW2g1urlF0VF/CEqg1lwAKhFHIFiNR+X6jCjg0KJa9MjAW6oICOx
        oX0jp195mWix6suRJSWVK14uieT6uL5yYC5tZMz+t9rs7YxCkHxFRT1H5ZLHUD/r
        93liqW+pzUx+bVdz5qNMb0ZonHZRLe3/Fzb19x8UMPc=
        =6gp8
        -----END PGP PUBLIC KEY BLOCK-----
        """.trimIndent()

    val privateKey2_Passphrase = "ikaAA3dimv9p7D.bqZ6mq.R45LRS6oi".toByteArray()
    val privateKey2 =
        """
        -----BEGIN PGP PRIVATE KEY BLOCK-----
        Version: ProtonMail

        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
        b5BlAx31rUZ+NdCZyPnU+83opOdYrRjy
        =kArG
        -----END PGP PRIVATE KEY BLOCK-----
        """.trimIndent()
}
