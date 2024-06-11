## Client performance measurement SDK

Allows you to collect different client metrics during test run and push them to Loki. Anything can be measured - either it unit, integration or e2e test.

In order to use the library you have to set up the following environment variables either on CI or locally or set in local.properties file (see example in [build.gradle](../../../../../../../../build.gradle.kts)):
1. `LOKI_ENDPOINT` - loki endpoint accessible outside of Dev VPN.
2. `LOKI_PRIVATE_KEY` - loki private key issues for your team.
3. `LOKI_CERTIFICATE` - loki certificate issues for your team.

The main SDK building blocks are:
- [MeasurementRule](MeasurementRule.kt) - the test rule which extends TestWatcher() rule in order to hook into different test states and populate metrics with measured values.
- [MeasurementContext](MeasurementContext.kt) - the main measurement entry point which allows you to set [MeasurementConfig](MeasurementConfig.kt) and initialises [MeasurementProfile](MeasurementProfile.kt) after setting the workflow. There can be only one instance of [MeasurementContext](MeasurementContext.kt) per the whole test run. 
- [MeasurementProfile](MeasurementProfile.kt) - represents a shared set of labels, metadata and metrics which can be measured during the test run. Profile can be configured to either collect logs by setting the [LogcatFilter](LogcatFilter.kt) via `collectLogcatLogs(logcatFilter: LogcatFilter)` or represent Service Level Indicator measurement when `setServiceLevelIndicator(sli: String)` is used. There can be multiple profiles per test run. Each profile can be extended with [CustomMeasurement](measurement/CustomMeasurement.kt) hooks to register your own measurement per [MeasureBlock](MeasureBlock.kt). [MeasurementProfile](MeasurementProfile.kt) keeps a list of measure blocks in order to push their metrics to Loki after each test run.   
- [MeasureBlock](MeasureBlock.kt) - represents a single measure block where logs and majority of metrics will be collected. It has `addMetric()` interface to add custom metrics by implementing [CustomMeasurement](measurement/CustomMeasurement.kt).
- [MeasurementConfig](MeasurementConfig.kt) - keeps configuration values to configure [LokiClient](client/LokiClient.kt) as well as setters and getters for `buildCommitShortSha` (GitLab "CI_COMMIT_SHA"), `environment` (test environment name) and `runId` (GitLab "CI_JOB_ID").
- [Measurement](measurement/Measurement.kt) - allows to register your own custom measurement. See usage example in class. Examples of [Measurement](measurement/Measurement.kt) are: [AppSizeMeasurement](measurement/AppSizeMeasurement.kt) and [DurationMeasurement](measurement/DurationMeasurement.kt).
- [LogcatFilter](LogcatFilter.kt) - helper class to specify what logcat logs to filter and monitor per profile. Logs will be recorded only in measure block.

Usage examples:

1. First you need to set the configuration in base test class or in `@BeforeClass` function:
   ```kotlin
    val measurementConfig = MeasurementConfig
     .setEnvironment(BuildConfig.DYNAMIC_DOMAIN)
     .setLokiPrivateKey(BuildConfig.LOKI_PRIVATE_KEY)
     .setLokiCertificate(BuildConfig.LOKI_CERTIFICATE)
   ```
2. Pushing logcat logs:
    ```kotlin
    @Test
    @Measure
    fun measureLoginTimeWithLogcatFilter() {
        val logcatFilter = LogcatFilter
            .addTag(coreExampleTestTagOne)
            .addTag(coreExampleTestTagTwo)
            .addTag(coreExampleTestTagThree)
            .setLokiLogsId(logsId)
            .failTestOnEmptyLogs()

        val profile = measurementContext
            .setWorkflow("coreexample")
            .setServiceLevelIndicator("login_duration")
            .setLogcatFilter(logcatFilter)

        loginRobot
            .username(user.name)
            .password(user.password)
            .clickSignInButton()

        profile.measure {
            Thread.sleep(3000)  //Added for testing purposes. Remove from your code.
            Log.d(coreExampleTestTagOne, "Test logcat log line. One")
            CoreexampleRobot().verify { userStateIs(user, Ready, Authenticated) }
            Log.d(coreExampleTestTagTwo, "Test logcat log line. Two")
        }
   
        profile.pushLogcatLogs() //Push logs to loki filtered by pre-set logcatFilter.
        profile.clearLogcatLogs() //Clear logcat logs.
   
        profile.measure {
            Thread.sleep(3000)  //Added for testing purposes. Remove from your code.
            Log.d(coreExampleTestTagThree, "Test logcat log line. Three")
            CoreexampleRobot().verify { userStateIs(user, Ready, Authenticated) }
        }
        profile.pushLogcatLogs()
    }
    ```
   JSON payload example:
   ```json
   {
     "streams": [
       {
         "stream": {
           "workflow": "coreexample",
           "build_type": "debug",
           "product": "me.proton.android.core.coreexample.dev",
           "platform": "android",
           "os_version": "android 13",
           "device_model": "sdk_gphone64_arm64"
         },
         "values": [
           [
             "1716881324627885000",
             "Test logcat log line. Three",
             {
               "app_version": "1.18.10",
               "build_commit_sha1": "a534cd49ab55023f165b50ba7320137c9a3e3dba",
               "environment": "proton.testenv",
               "run_id": "unknown",
               "id": "c21fb6ab-8504-483f-a1da-f88db618e58e",
               "test": "measureLoginTimeWithLogcatFilter"
             }
           ]
         ]
       },
       {
         "stream": {
           "workflow": "coreexample",
           "build_type": "debug",
           "product": "me.proton.android.core.coreexample.dev",
           "platform": "android",
           "os_version": "android 13",
           "device_model": "sdk_gphone64_arm64"
         },
         "values": [
           [
             "1716881381228413000",
             "Test logcat log line. One",
             {
               "app_version": "1.18.10",
               "build_commit_sha1": "a534cd49ab55023f165b50ba7320137c9a3e3dba",
               "environment": "proton.testenv",
               "run_id": "unknown",
               "id": "c21fb6ab-8504-483f-a1da-f88db618e58e",
               "test": "measureLoginTimeWithLogcatFilter"
             }
           ],
           [
             "1716881387542054000",
             "Test logcat log line. Two",
             {
               "app_version": "1.18.10",
               "build_commit_sha1": "a534cd49ab55023f165b50ba7320137c9a3e3dba",
               "environment": "proton.testenv",
               "run_id": "unknown",
               "id": "c21fb6ab-8504-483f-a1da-f88db618e58e",
               "test": "measureLoginTimeWithLogcatFilter"
             }
           ]
         ]
       }
     ]
   }
   ```
3. Pushing Service Level Indicator metrics:
   ```kotlin
    @Test
    @Measure
    fun measureLoginTimeNoLogcatLogs() {
        loginRobot
            .username(user.name)
            .password(user.password)
            .clickSignInButton()

        val profile = measurementContext
            .setWorkflow("coreexample")
            .setServiceLevelIndicator("login_time")
            .addMeasurement(DurationMeasurement)
            .addMeasurement(AppSizeMeasurement)
        
        profile.measure {
            CoreexampleRobot().verify { userStateIs(user, Ready, Authenticated) }
        }
    }
    ```
   JSON payload example:
   ```json
   {
     "streams": [
       {
         "stream": {
           "workflow": "coreexample",
           "build_type": "debug",
           "product": "me.proton.android.core.coreexample.dev",
           "platform": "android",
           "os_version": "android 13",
           "device_model": "sdk_gphone64_arm64",
           "sli": "login_time"
         },
         "values": [
           [
             "1716881242491000000",
             "{\"app_size\":\"43.97\",\"duration\":\"8.12\",\"status\":\"succeeded\"}",
             {
               "app_version": "1.18.10",
               "build_commit_sha1": "a534cd49ab55023f165b50ba7320137c9a3e3dba",
               "environment": "proton.testenv",
               "run_id": "unknown",
               "id": "bec6a447-e75b-4448-a988-412e66a5e6ca",
               "test": "measureLoginTimeNoLogcatLogs"
             }
           ]
         ]
       }
     ]
   }
   ```