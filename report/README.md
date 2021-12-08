# Reports

The Reports module allows a user to send bug reports.

Once a report is written, and user submits it, it is enqueued into a WorkManager. The report is sent whenever an Internet connection is available. By default, once a report is enqueued, from the user perspective it is considered as sent, and the Bug Report activity will be finished.

### Quickstart

1. Add `me.proton.core:reports-presentation` module.
2. If you use Hilt, add `me.proton.core:reports-hilt` module.
3. Add `BugReportActivity` to your `AndroidManifest.xml` file:

```xml
<activity android:name="me.proton.core.reports.presentation.ui.BugReportActivity"
    android:windowSoftInputMode="adjustResize" />
```

4. Make sure WorkManager is configured:
    - add [WorkManager](https://developer.android.com/jetpack/androidx/releases/work) as your dependency
    - [configure `WorkManager` to be available in Hilt](https://developer.android.com/training/dependency-injection/hilt-jetpack#workmanager)
5. Whenever you want to display a Bug Report screen, use [ReportsOrchestrator](presentation/src/main/kotlin/me/proton/core/reports/presentation/ReportsOrchestrator.kt).
6. Refer to [BugReportViewModel](../coreexample/src/main/kotlin/me/proton/android/core/coreexample/viewmodel/BugReportViewModel.kt) from CoreExample app for sample usage.

