# Mock Proxy SDK

Mock proxy SDK allows to control mock proxy server parameters from inside your test.

Supported features:
1. Single route mock.
2. Scenario mocking (collection of routes).
3. Control bandwidth.
4. Control latency.

## Start using
To start using SDK create an instance of `MockClient(baseUrl: String)`:
```kotlin
val mockClient = MockClient(baseUrl)
```
Here `baseUrl` can be an atlas environment or the local host URL.

If you run tests locally using emulator use Android localhost baseUrl value from `Constants`:
```kotlin
public const val EMULATOR_LOCALHOST: String = "http://10.0.2.2:3001/"
```

`MockClient` instance creation with pre-defined local host value for Android emulator:
```kotlin
val mockClient = MockClient(EMULATOR_LOCALHOST)
```

## Mock single route
SDK assumes mock files are stored in app or test assets.

Mock single route from app assets:
```kotlin
mockClient.setStaticMockFromAppAssets(routeFilePath)
```

Mock single route from test assets:
```kotlin
mockClient.setStaticMockFromTestAssets(routeFilePath)
```
## Mock scenario
Mock scenario route from app assets (both app and test assets):
```kotlin
mockClient.setScenarioFromAssets(scenarioFilePath)
```

Reset scenario from app assets:
```kotlin
mockClient.resetScenarioFromAppAssets(scenarioFilePath)
```

Reset scenario from test assets:
```kotlin
mockClient.resetScenarioFromTestAssets(scenarioFilePath)
```

## Get, set and reset latency

```kotlin

val latencyInfo = mockClient.getLatency()
mockClient.setLatency(LatencyLevel.HIGH)
mockClient.resetLatency()
```

## Get, set and reset bandwidth

```kotlin
val bandwidth = mockClient.getBandwidth()
mockClient.setBandwidth(BandwidthLimit._4G)
mockClient.resetBandwidth()
```

## Resetting the state (reset all)
```kotlin
mockClient.resetAllMocks()
```