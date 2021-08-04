## Presentation Version [0.10.1]

Aug 3, 2021

### API Changes

- ProtonCheckbox and ProtonRadioButton are now open classes.
- ProtonCheckbox and ProtonRadioButton don't use compound drawables to draw the button on the right. This means that setting a drawable with e.g. setCompoundDrawablesRelative(icon, null, null, null) doesn't unexpectedly break them.
- Add Proton.Text.Hero style.
- Add ProtonInput.clearTextAndOverwriteMemory() -  the method overwrites and clears the input's text buffer. It should be used to limit the time passwords are kept in memory.

## User Settings Version [1.5.1]

Jul 30, 2021

### Bug Fixes

- Fixed UserSettingsDatabase MIGRATION_0.

## Version [1.5.0]

Jul 30, 2021

Add user settings, with initial update recovery email option.

### Dependencies

- Auth 1.5.0.
- Account Manager 1.5.0.
- User 1.5.0.
- UserSettings 1.5.0.
- Plan 0.2.0.
- Presentation 0.10.0.

### New Modules

- **User Settings**: Get and update UserSettings.

### New Migration

- If you use ```Account Manager Data Db``` module, nothing to do, it's transparently applied.
- If you use your own AppDatabase, please apply changes as follow:
    - Add ```UserSettingsEntity``` to your AppDatabase ```entities```.
    - Add ```UserSettingsConverters``` to your AppDatabase ```TypeConverters```.
    - Extends ```UserSettingsDatabase``` from your AppDatabase.
    - Add a migration to your AppDatabase (```addMigration```):
```
val MIGRATION_X_Y = object : Migration(X, Y) {
    override fun migrate(database: SupportSQLiteDatabase) {
        UserSettingsDatabase.MIGRATION_0.migrate(database)
    }
}
```

### New Features

- Added ```UserSettings module``` with Update Recovery Email feature.
- Added ```FragmentActivity.addOnBackPressedCallback``` in presentation module.

### Recommendations

- Do not expect ```UserSettings``` properties to be stable, they could change in future.

## Crypto Version [1.1.5], Key Version [1.3.2]

Jul 28, 2021

### New Features

- Added Crypto encryptAndSignFile & decryptAndVerifyFile.

## Mail Settings Version [1.3.2]

Jul 28, 2021

### New Features

- Added MailSettingsRepository updateMailSettings & MailSettingsResponse.toMailSettings.

## Account Manager Version [1.3.2]

Jul 27, 2021

### Bug Fixes

- Changed Account Initials Count to 1.

## Presentation Version [0.9.9]

Jul 26, 2021

### New Features

- Added ```ProtonCheckbox``` and ```ProtonRadioButton``` that display their "button" (i.e. the checkbox or circle) to the right of the label.

## User Version [1.3.2]

Jul 23, 2021

### Bug Fixes

- Fixed UserAddressRepositoryImpl issue when no address returned from fetcher.

### Recommendations

- You must update to this version because it prevent to properly sign in/up without address.

## Version [1.3.1]

Jul 21, 2021

Refactor Core Database.

### Dependencies

- Account 1.3.1.
- AccountManager 1.3.1.
- Data 1.3.1.
- DataRoom 1.3.1.
- HumanVerification 1.3.1.
- Key 1.3.1.
- MailSettings 1.3.1.
- User 1.3.1.

### New Modules

- **Data Room**: New module containing all Android Room specifics.
- **AccountManager Data Db**: New module containing old AccountManagerDatabase, for backward compatibility purposes.

### New Features

- ```SupportSQLiteDatabase``` extensions: ```getTableColumns```, ```recreateTable```, ```addTableColumn```, ```dropTableColumn```, ```dropTableContent``` or ```dropTable```.
- ```UserRepository.updateUser```: function for event handing.
- ```UserAddressRepository.updateAddresses/deleteAddresses```: functions for event handling.

### Bug Fixes

- Fixed UserAddressRepositoryImpl to fetch addresses if DB table if empty.

### API Changes
  
- Removed Dagger Provides for ```AccountManagerDatabase``` from **AccountManager Dagger** module.
- Client need to provide all Database components:
```
@Module
@InstallIn(SingletonComponent::class)
object AppDatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AccountManagerDatabase =
        AccountManagerDatabase.buildDatabase(context)
}

@Module
@InstallIn(SingletonComponent::class)
abstract class DatabaseBindsModule {
    @Binds
    abstract fun provideAccountDatabase(db: AccountManagerDatabase): AccountDatabase

    @Binds
    abstract fun provideUserDatabase(db: AccountManagerDatabase): UserDatabase

    @Binds
    abstract fun provideAddressDatabase(db: AccountManagerDatabase): AddressDatabase

    @Binds
    abstract fun provideKeySaltDatabase(db: AccountManagerDatabase): KeySaltDatabase

    @Binds
    abstract fun providePublicAddressDatabase(db: AccountManagerDatabase): PublicAddressDatabase

    @Binds
    abstract fun provideHumanVerificationDatabase(db: AccountManagerDatabase): HumanVerificationDatabase

    @Binds
    abstract fun provideMailSettingsDatabase(db: AccountManagerDatabase): MailSettingsDatabase
}
```

### Recommendations

- You should **not use** ```AccountManagerDatabase``` anymore (could be deprecated in future).
- You should define your own Database, see CoreExample ```AppDatabase```, ```AppDatabaseMigrations``` and ```AppDatabaseBindsModule```.
