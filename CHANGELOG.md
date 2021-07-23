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
