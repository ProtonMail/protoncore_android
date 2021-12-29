# Crypto Validator

It will automatically run as soon as the app becomes visible and if the KeyStoreCrypto is not working properly, it will show a blocking error dialog to the user warning about the issue and allowing them to either continue using the app knowing the security risk or remove all accounts. 

To use it you need to add a new initializer and activity to the `AndroidManifest.xml`:

```xml
<provider ...>
    <meta-data
        android:name="me.proton.core.crypto.validator.presentation.init.CryptoValidatorInitializer"
        android:value="androidx.startup" />
    ...
</provider>

<!-- Remember to change this to your app's theme, if applies -->
<activity
    android:name="me.proton.core.crypto.validator.presentation.ui.CryptoValidatorErrorDialogActivity"
    android:theme="@style/ProtonTheme.Transparent" />
```

## Modules:

* crypto-validator (wraps all the other modules)
* crypto-validator-data
* crypto-validator-domain
* crypto-validator-presentation
* crypto-validator-dagger

If you want to provide your own dependencies just include `-data`, `-domain` and `-presentation` instead of adding the whole `crypto-validator` parent module.
