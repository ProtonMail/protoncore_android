
# Core UI tests functionality that can be reused in multiple Proton projects

Copyright (c) 2021 Proton Technologies AG

## License

The code and data files in this distribution are licensed under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version. See <https://www.gnu.org/licenses/> for a copy of this license.

### UI tests structure:

- [instrumented](instrumented) - domain independent test framework 
    - [builders](instrumented/builders) - builder like classes to perform actions and assertions on `View`, `ListView` and `RecycleView` elements. Also it contains a class to easily build Root view matchers.  
    - [matchers](instrumented/matchers) - common matchers 
    - [ui](instrumented/ui) - UI actions
    - [utils](instrumented/utils) - various utility and helper functions
    - [waits](instrumented/waits) - `ConditionWatcher` interface and different waiters
    - [Robot.kt](instrumented/Robot.kt) - builders reference. Should be a superclass for Robot classes.
    - [ProtonTest.kt](instrumented/ProtonTest.kt) - shared `setUp()` and `tearDown()` functions.
- [plugins](plugins) - Proton specific helper classes
    - [Database.kt](plugins/Database.kt) - Various database management functions
    - [Requests.kt](plugins/Requests.kt) - Helper class for communication with internal Proton API
- [robots](robots) - Core robots
    - [humanverification](robots/humanverification)
    - [login](robots/auth/login)
    - [other](robots/other)
    - [payments](robots/payments)
    - [signup](robots/auth/signup)
    - [CoreRobot.kt](robots/CoreRobot.kt) - common core specific ui actions
    - [CoreVerify.kt](robots/CoreVerify.kt) - common core specific verifications

### Operating on Views

Simple usage with one `ViewMatcher`:
```kotlin
class ContactsRobot : Robot() {

    fun addContact(): AddContactRobot {
        view
            .withId(R.id.addContactItem)
            .click()
        return AddContactRobot()
    }
}
```
A bit more complex usage when multiple matchers have to be applied to locate a `View`:
```kotlin
class ContactsRobot : Robot() {

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
```kotlin
class AddContactRobot: Robot() {

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

```kotlin
class MessageRobot : Robot {

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
```kotlin
class ContactsRobot : Robot {

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
```kotlin
class MultiuserManagementTests : Robot {

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

In order to use test user data helper a json file must be present in `assets` directory. Json file must have a list of json objects of type:

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

If no values are provided for any fields, empty or random values are assigned to User object automatically.

The object of type 
```json
{
    "name": "testUser",
    "password": "somePassword"
}
```
will also be accepted.

Test user data then can be loaded and used:

```kotlin
import me.proton.core.test.android.plugins.data.User.Users

class YourTests {
    private val users = Users(pathToJsonInsideAssets)
    val user = users.getUser { it.name == "yourName" }
}
```