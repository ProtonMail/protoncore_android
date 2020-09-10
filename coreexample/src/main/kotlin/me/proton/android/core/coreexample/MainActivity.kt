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

package me.proton.android.core.coreexample

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.proton.android.core.coreexample.databinding.ActivityMainBinding
import me.proton.android.core.coreexample.ui.CustomViewsActivity
import me.proton.android.core.presentation.ui.ProtonActivity
import me.proton.android.core.presentation.utils.onClick
import me.proton.core.auth.presentation.ui.LoginActivity
import me.proton.core.humanverification.presentation.HumanVerificationChannel
import me.proton.core.humanverification.presentation.entity.HumanVerificationResult
import me.proton.core.humanverification.presentation.ui.HumanVerificationActivity
import me.proton.core.humanverification.presentation.ui.HumanVerificationDialogFragment
import me.proton.core.humanverification.presentation.utils.showHumanVerification
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ProtonActivity<ActivityMainBinding>() {

    override fun layoutId(): Int = R.layout.activity_main

    @Inject
    @HumanVerificationChannel
    lateinit var channel: Channel<HumanVerificationResult>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.humanVerification.onClick {
            startHumanVerificationAsActivity()
        }

        binding.customViews.onClick {
            startActivity(Intent(this, CustomViewsActivity::class.java))
        }

        binding.login.onClick {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

    fun startHumanVerificationAsFragment() {
        supportFragmentManager.setFragmentResultListener(
            HumanVerificationDialogFragment.KEY_VERIFICATION_DONE,
            this
        ) { _, bundle ->
            val tokenCode = bundle.getString(HumanVerificationDialogFragment.ARG_TOKEN_CODE)
            val tokenType = bundle.getString(HumanVerificationDialogFragment.ARG_TOKEN_TYPE)

            Toast.makeText(this, "Fragment: Code $tokenCode done with $tokenType", Toast.LENGTH_LONG).show()
        }

        supportFragmentManager.showHumanVerification(
            largeLayout = false
        )
    }

    private fun startHumanVerificationAsActivity() {
        lifecycleScope.launch {
            withContext(Dispatchers.Main) {
                val result = channel.receive()
                Toast.makeText(this@MainActivity, "Channel ${result.tokenCode}", Toast.LENGTH_SHORT).show()
            }
        }

        val humanVerificationIntent = Intent(this, HumanVerificationActivity::class.java)
        startActivity(humanVerificationIntent)
    }
}
