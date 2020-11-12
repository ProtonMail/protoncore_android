Provide support for base ProtonTheme and styles, as long as base classes and custom views that 
should be default for all Proton Android clients.

## Gradle:
```kotlin
    implementation("me.proton.core.android:presentation:$version")
```

 
## Features
Below you can find the features presentation module is offering


### **`Theme`**
***(will be extended/added more styles incrementally as needed in the future)***

Use `ProtonTheme` which extends `Theme.MaterialComponents.DayNight.DarkActionBar` theme. 
It provides common colors, sizes and styles for base views and on a disposal for extension according to the client needs. 
In the future support for the night mode will be out of the box.


### **`Styles`**
***(will be extended/added more styles incrementally as needed in the future)***

Provides base (ready to use) styles for the views. More details: [styles](src/main/res/values)


### **`Base classes`**
***(still under development, not finalized)***

Base abstract Activity, Fragment and ViewModels are available for extension by the client. 


### `Custom views`
Currently supported according to the Android Core designs (*ask for link) are the views below.

***`ProtonButton`***

- Class that should be base if the client want to extend and create a specific implementation for their needs. 
Also, all Android Core buttons will extend this class.


***`ProtonProgressButton`***
- Custom Proton button that includes a loading spinner (indefinite progress).
- Supports normal button mode (default) or text-only mode by supplying the appropriate style (the module is shipped with it).
- The styles are default `@style/ProtonButton` (which is a default button style in `ProtonTheme` and is not mandatory to be set).
- For the text-only button use `@style/ProtonButton.Borderless.Text`. This one needs to be set in the xml.

Supported attributes:
```xml
<declare-styleable name="ProtonProgressButton">
    <!-- Whether the loading state must be set on click. Default is true -->
    <attr name="autoLoading" format="boolean" />
    <attr name="initialState" format="enum">
        <enum name="idle" value="0"/>
        <enum name="loading" value="1"/>
    </attr>
    <!-- This must be an animated drawable -->
    <attr name="progressDrawable" format="reference" />
</declare-styleable>
```

Example usage:
```xml
<me.proton.core.presentation.ui.view.ProtonProgressButton
    android:id="@+id/loadingButton"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_marginTop="@dimen/default_top_margin"
    android:text="@string/example_loading"
    app:initialState="loading"
    app:layout_constraintEnd_toEndOf="parent"
    app:layout_constraintStart_toStartOf="parent"
    app:layout_constraintTop_toBottomOf="@id/textOnlyButton" />
```

***`ProtonInput`***

- Custom Proton input (advanced `EditText`) view which includes an `EditText` as a default input 
view, additionally it includes a **`Label`** located above the input view and an 
**`optional assistive text`** located below the input view.
- The assistive text can act also as a validation error message if an error message is passed in 
`setInputError()` function.
- ProtonInput supports displaying error according to the latest Proton Android design guidelines. 

Supported attributes: 
```xml
<declare-styleable name="ProtonInput">
    <attr name="label" format="string" />
    <attr name="assistiveText" format="string" />
    <attr name="android:text" />
    <attr name="android:hint" format="string" />
    <attr name="android:inputType" />
</declare-styleable>
```
Example usage (for password mode just set `android:inputType="textPassword"`):
```xml
<me.proton.core.presentation.ui.view.ProtonInput
    android:id="@+id/inputExample"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_marginTop="@dimen/double_top_margin"
    android:hint="@string/example_hint"
    app:assistiveText="@string/example_assistive_text"
    app:label="@string/example_label"
    app:layout_constraintEnd_toEndOf="parent"
    app:layout_constraintStart_toStartOf="parent"
    app:layout_constraintTop_toBottomOf="@id/loadingTextOnlyButton" />
```
