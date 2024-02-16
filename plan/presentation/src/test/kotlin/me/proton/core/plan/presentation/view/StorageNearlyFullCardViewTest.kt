package me.proton.core.plan.presentation.view

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import me.proton.core.domain.entity.Product
import org.junit.Rule
import kotlin.test.Test

class StorageNearlyFullCardViewTest {
    @get:Rule
    val paparazzi = Paparazzi(
        deviceConfig = DeviceConfig.PIXEL_5,
        theme = "ProtonTheme"
    )

    @Test
    fun initialState() {
        val view = StorageNearlyFullCardView(paparazzi.context)
        paparazzi.snapshot(view)
    }

    @Test
    fun driveStorageFull() {
        val view = StorageNearlyFullCardView(paparazzi.context)
        view.setStorageFull(Product.Drive)
        paparazzi.snapshot(view)
    }

    @Test
    fun mailStorageFull() {
        val view = StorageNearlyFullCardView(paparazzi.context)
        view.setStorageFull(Product.Mail)
        paparazzi.snapshot(view)
    }

    @Test
    fun driveStorageNearlyFull() {
        val view = StorageNearlyFullCardView(paparazzi.context)
        view.setStorageNearlyFull(Product.Drive)
        paparazzi.snapshot(view)
    }

    @Test
    fun mailStorageNearlyFull() {
        val view = StorageNearlyFullCardView(paparazzi.context)
        view.setStorageNearlyFull(Product.Mail)
        paparazzi.snapshot(view)
    }

    @Test
    fun storageNearlyFullUpgradeUnavailable() {
        val view = StorageNearlyFullCardView(paparazzi.context)
        view.onUpgradeUnavailable()
        view.setStorageNearlyFull(Product.Mail)
        paparazzi.snapshot(view)
    }
}
