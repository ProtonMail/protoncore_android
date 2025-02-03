package me.proton.core.test.mockproxy

import me.proton.core.test.mockproxy.Constants.EMULATOR_LOCALHOST
import org.junit.After
import org.junit.Assert.assertThrows
import org.junit.Test
import java.io.FileNotFoundException
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MockProxyTest {

    private val mockClient = MockClient(EMULATOR_LOCALHOST)

    @After
    fun afterTest() {
        mockClient.resetAllMocks()
    }

    @Test
    fun testScenarioMockingFromTestAssets() {
        val scenarioFilePath = "scenarios/auth_scenario1/auth_scenario.json"
        val staticMocksResponseList: List<MockObject> =
            mockClient.setScenarioFromAssets(scenarioFilePath)
        assertTrue(staticMocksResponseList.count() == 2)
    }

    @Test
    fun testScenarioMockingFromAppAssets() {
        val scenarioFilePath = "scenarios/auth_scenario1/auth_scenario.json"
        mockClient.setLatency(LatencyLevel.EXTREME)
        val staticMocksResponseList: List<MockObject> =
            mockClient.setScenarioFromAssets(scenarioFilePath)
        assertTrue(staticMocksResponseList.count() == 2)
    }

    @Test
    fun testResetScenarioMockingFromAppAssets() {
        val scenarioFilePath = "scenarios/auth_scenario1/auth_scenario.json"
        mockClient.setLatency(LatencyLevel.EXTREME)
        var staticMocksResponseList: List<MockObject> =
            mockClient.setScenarioFromAssets(scenarioFilePath)
        assert(staticMocksResponseList.count() == 2)
        staticMocksResponseList.forEach { staticMock ->
            assertTrue(staticMock.enabled, "Static mock should be enabled but it is not.")
        }
        staticMocksResponseList = mockClient.resetScenarioFromAssets(scenarioFilePath)
        staticMocksResponseList.forEach { staticMock ->
            assertFalse(staticMock.enabled, "Static mock should be disabled but it is not.")
        }
    }

    @Test
    fun testResetScenarioMockingFromTestAssets() {
        val scenarioFilePath = "scenarios/auth_scenario1/auth_scenario.json"
        mockClient.setLatency(LatencyLevel.EXTREME)
        var staticMocksResponseList: List<MockObject> =
            mockClient.setScenarioFromAssets(scenarioFilePath)
        assert(staticMocksResponseList.count() == 2)
        staticMocksResponseList.forEach { staticMock ->
            assertTrue(staticMock.enabled, "Static mock should be enabled but it is not.")
        }
        staticMocksResponseList = mockClient.resetScenarioFromAssets(scenarioFilePath)
        staticMocksResponseList.forEach { staticMock ->
            assertFalse(staticMock.enabled, "Static mock should be disabled but it is not.")
        }
    }

    @Test
    fun testSingleAuthRouteMockingFromAppAssets() {
        val routeFilePath = "scenarios/auth_scenario1/auth_mock1.json"
        val staticMockResponse: MockObject = mockClient.setStaticMockFromAssets(routeFilePath)
        assertTrue(staticMockResponse.response.statusCode == 200)
    }

    @Test
    fun testSingleAuthRouteMockingFromTestAssets() {
        val routeFilePath = "scenarios/auth_scenario1/auth_mock1.json"
        val staticMockResponse: MockObject = mockClient.setStaticMockFromAssets(routeFilePath)
        assertTrue(staticMockResponse.response.statusCode == 200)
    }

    @Test
    fun testFailNotExistingFileLoadFromTestAssets() {
        val notExistingFilePath = "scenarios/auth_scenario1/not_exists.json"
        val exception = assertThrows(FileNotFoundException::class.java) {
            mockClient.setStaticMockFromAssets(notExistingFilePath)
        }
        assertTrue(exception.message?.contains(notExistingFilePath) == true)
    }

    @Test
    fun testFailNotExistingFileLoadFromAppAssets() {
        val notExistingFilePath = "scenarios/auth_scenario1/not_exists.json"
        val exception = assertThrows(FileNotFoundException::class.java) {
            mockClient.setStaticMockFromAssets(notExistingFilePath)
        }
        assertTrue(exception.message?.contains(notExistingFilePath) == true)
    }

    @Test
    fun testSetLatency() {
        mockClient.setLatency(LatencyLevel.HIGH)
        val latencyInfo = mockClient.getLatency()
        assertTrue(latencyInfo.enabled && latencyInfo.latency == LatencyLevel.HIGH.latencyMs)
    }

    @Test
    fun testSetAndResetLatency() {
        mockClient.setLatency(LatencyLevel.HIGH)
        var latencyInfo = mockClient.getLatency()
        assertTrue(latencyInfo.enabled && latencyInfo.latency == LatencyLevel.HIGH.latencyMs)

        mockClient.resetLatency()
        latencyInfo = mockClient.getLatency()
        assertTrue(!latencyInfo.enabled && latencyInfo.latency == LatencyLevel.NONE.latencyMs)
    }

    @Test
    fun testSetBandwidth() {
        mockClient.setBandwidth(BandwidthLimit.NONE)
        val bandwidthInfo = mockClient.getBandwidth()
        assertTrue(bandwidthInfo.enabled && bandwidthInfo.limit == BandwidthLimit.NONE.speedKbps)
    }

    @Test
    fun testSetAndResetBandwidth() {
        mockClient.setBandwidth(BandwidthLimit._4G)
        var bandwidthInfo = mockClient.getBandwidth()
        assert(bandwidthInfo.enabled && bandwidthInfo.limit == BandwidthLimit._4G.speedKbps)

        mockClient.resetBandwidth()
        bandwidthInfo = mockClient.getBandwidth()
        assertTrue(!bandwidthInfo.enabled && bandwidthInfo.limit == BandwidthLimit.NONE.speedKbps)
    }
}
