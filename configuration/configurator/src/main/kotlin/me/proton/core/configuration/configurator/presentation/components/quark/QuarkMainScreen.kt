package me.proton.core.configuration.configurator.presentation.components.quark

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import me.proton.core.compose.component.ProtonSettingsHeader
import me.proton.core.compose.component.ProtonSettingsItem
import me.proton.core.compose.component.ProtonSettingsList
import me.proton.core.compose.component.ProtonSnackbarHost
import me.proton.core.compose.component.ProtonSnackbarHostState
import me.proton.core.compose.component.appbar.ProtonTopAppBar

@Composable
fun QuarkMainScreen() {
    val navController = rememberNavController()

    NavHost(navController, startDestination = "main") {
        composable("main") { MainScreen(navController) }
        composable("createUser") { QuarkCreateUserScreen(navController) }
        composable("updateUser") { UpdateUserScreen(navController) }
        composable("drive") { DriveUserUpdateScreen(navController) }
        composable("mail") { MailUpdateScreen(navController) }
        composable("environmentManagement") { QuarkEnvironmentManagementScreen(navController) }
        composable("userAccountUpdate") { AccountUserUpdateScreen(navController) }
    }
}

@Composable
fun MainScreen(navController: NavHostController) {
    val isProduction = false
    val hostState = remember { ProtonSnackbarHostState() }

    Scaffold(
        snackbarHost = { ProtonSnackbarHost(hostState) },
        topBar = {
            ProtonTopAppBar(title = { Text("Quark commands") })
        },
        content = { paddingValues ->
            ProtonSettingsList(modifier = Modifier.padding(paddingValues)) {
                item {
                    ProtonSettingsHeader(
                        title = "Account management",
                        modifier = Modifier
                            .padding(paddingValues)
                            .fillMaxSize()
                    )
                }
                item {
                    ProtonSettingsItem(
                        modifier = Modifier
                            .padding(paddingValues)
                            .fillMaxSize(),
                        name = "Create user",
                        hint = "User account settings to be seeded",
                        isClickable = !isProduction,
                        onClick = {
                            navController.navigate("createUser")
                        }
                    )
                }
                item {
                    ProtonSettingsItem(
                        Modifier
                            .padding(paddingValues)
                            .fillMaxSize(),
                        name = "Update user",
                        hint = "Update existing user (should be seeded first if not)",
                        isClickable = !isProduction,
                        onClick = {
                            navController.navigate("updateUser")
                        }
                    )
                }
                item { ProtonSettingsHeader(title = "Environment management") }
                item {
                    ProtonSettingsItem(
                        name = "Environment",
                        hint = "Control atlas environment",
                        isClickable = !isProduction,
                        onClick = {
                            navController.navigate("environmentManagement")
                        }
                    )
                }
            }
        }
    )
}
