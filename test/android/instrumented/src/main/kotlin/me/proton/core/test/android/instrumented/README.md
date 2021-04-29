
# Core UI tests functionality that can be reused in multiple Proton projects

Copyright (c) 2020 Proton Technologies AG

## License

The code and data files in this distribution are licensed under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version. See <https://www.gnu.org/licenses/> for a copy of this license.

### UI tests structure:

- [builders](./builders) package - contains builder like classes to perform actions and assertions on `View`, `ListView` and `RecycleView` elements. Also it contains a class to easily build Root view matchers.  
- [devicesetup](./devicesetup) package - contains methods that can be used to set up test device in desired state before running UI tests. 
- [failurehandler](./failurehandler) package - contains `ProtonFailureHandler` class that is used to intercept Espresso test failures, take screenshots and save them on device. Also contains a logic to not fail test until Wait function timeout is not reached.
- [intentutils](./intentutils) package - contains `IntentHelper` and `MimeType` classes that are used to send application intents via device shell.
- [testrail](./testrail) package - TestRail related classes to report test results to TestRail platform.
- [uiactions](./uiactions) package - contains custom `ViewActions`.
- [uiautomator](./uiautomator) package - contains `DeviceRobot` class that holds multiple functions to operate outside of Proton application context. For example: clicking home button, showing recent applications or expanding notifications. 
- [uimatchers](./uimatchers) package - `ViewMatchers` to match system UI elements like: OK, Cancel buttons.
- [uiwaits](./uiwaits) package - contains wait functions that wait for ViewInteraction or DataInteraction conditions.
- [utils](./utils) package - contains `ActivityProvider` and `StringUtils` classes.
- [watchers](./watchers) package - contains ProtonWatcher and TestExecutionWatcher. `ProtonWatcher` - a mechanism that allows watching for a condition to be met within specified timeout and with specified interval. `TestExecutionWatcher` - monitors test run results and performs actions on Success or Failure.
- [robots](./robots) package - contains Core robots.
- [data](./data) package - contains test data related classes.
- [CoreRobot.kt](CoreRobot.kt) class - holds builders reference. Should be a superclass for Robot classes.
- [CoreTest.kt](./CoreTest.kt) class - holds shared `setUp()` and `tearDown()` functions.

### Operating on Views

Simple usage with one `ViewMatcher`:
```
class ContactsRobot : BaseRobot() {

    fun addContact(): AddContactRobot {
        view
            .withId(R.id.addContactItem)
            .click()
        return AddContactRobot()
    }
}
```
A bit more complex usage when multiple matchers have to be applied to locate a `View`:
```
class ContactsRobot : BaseRobot() {

    fun addGroup(): AddContactGroupRobot {
        view
            .instanceOf(FloatingActionButton::class.java)
            .withVisibility(ViewMatchers.Visibility.VISIBLE)
            .click()
        return AddContactGroupRobot()
    }
}
```

### Building Root view matchers
Sometimes, when target `View` is located inside a platform pop-up or dialog `RootMatchers` should be provided to point Espresso to use specific root view hierarchy:
```
class AddContactRobot: BaseRobot() {

    private fun displayName(name: String): AddContactRobot {
        view
            .withId(R.id.contact_display_name)
            .inRoot(rootView.isDialog())
            .typeText(name)
        return this
    }
}
``` 

### Operating on ListViews
To operate on an item inside the `ListView` you have to provide the item matcher `onListItem(withFolderName(folderName))` and if there is more than one `ListView` in the hierarchy specify exactly what adapter should be used: `inAdapter(view.withId(R.id.folders_list_view).matcher())`    

```
class MessageRobot : CoreRobot {

    fun selectFolder(folderName: String) {
        listView
            .onListItem(withFolderName(folderName))
            .inAdapter(view.withId(R.id.folders_list_view))
            .click()
    }
}
```


### Operating on RecycleViews

Below example shows how to perform action on `RecyclerView.ViewHolder` item (which represents an item in RecyclerView list) that matches provided `withContactEmail(email)` matcher:
```
class ContactsRobot : CoreRobot {

    fun clickContactByEmail(email: String): ContactDetailsRobot {
        recyclerView
            .withId(R.id.contactsRecyclerView)
            .waitUntilPopulated()
            .onHolderItem(withContactEmail(email))
            .click()
        return ContactDetailsRobot()
    }
}
```

And in the example given below you can see how to click on a `RecyclerView.ViewHolder` item child or descendant view using `onItemChildView()` function that takes as an argument child/descendant view matcher `view.withId(R.id.accUserMoreMenu).matcher()`:
```
class MultiuserManagementTests : CoreRobot {

    fun accountMoreMenu(email: String): AccountManagerRobot {
        recyclerView
            .withId(R.id.accountsRecyclerViewId)
            .onHolderItem(withAccountEmailInAccountManager(email))
            .onItemChildView(view.withId(R.id.accUserMoreMenu))
            .click()
        return AccountManagerRobot()
    }
}
```

### Test data

#### Test user data

In order to use test user data helper a `users.json` file must be present in test-android-instrumented/assets/sensitive directory. `users.json` must have a list of json objects of type:

```json
{
    "firstName": "String",
    "lastName": "String",
    "email": "String",
    "type": "Int(1|2)",
    "password": "String",
    "name": "String",
    "passphrase": "String",
    "phone": "String",
    "country": "String",
    "twoFa": "String",
    "paymentMethods": [
      {
        "paymentMethodType": "PaymentMethodType",
        "details": "JsonObject"
      }
    ],
    "plan": "String"
}
```

If no values are provided, empty values are assigned to User object automatically. 

Once ```users.json``` is correctly setup, you can load users from that file via ```getUser()``` function:

```kotlin
import me.proton.core.test.android.instrumented.data.User.Users.getUser

private val twoPassUser = getUser { it.passphrase.isNotEmpty() }
```

```twoPassUser``` will contain a random test user with passphrase set
