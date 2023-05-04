# Plan module

Contains sub-modules:
- dagger: Dagger Injection/Module(s).
- data: Data layer.
- domain: Domain layer.
- presentation: Presentation layer.

### Quickstart

Product have to provide a configuration:

```
@Module
@InstallIn(SingletonComponent::class)
object PlansModule {

    @Provides
    @SupportSignupPaidPlans
    fun provideSupportSignupPaidPlans() = true

    @Provides
    @SupportUpgradePaidPlans
    fun provideSupportUpgradePaidPlans() = true

    @Provides
    @ProductOnlyPaidPlans
    fun provideProductOnlyPaidPlans() = false

    @Provides
    fun provideClientPlansFilterPredicate(): ClientPlanFilter? = null
}
```
- SupportSignupPaidPlans: true to show Paid plan(s) during SignUp.
- SupportUpgradePaidPlans: true to show Paid plan(s) during Upgrade.
- ProductOnlyPaidPlans: true if Product support only Paid plan(s).
- ClientPlanFilter: Any client additional plan filter (e.g. during SignUp or Upgrade).

### Plan Layouts & Mapping

By default the presentation module will use predefined layouts to show plans.

There are 2 different layout categories:
- **Current** plan layout: displayed on subscription screen.
- **Paid** plan layout: displayed on SignUp/Upgrade screen, for plan selection.

Here is the list of existing plans (with corresponding default layout):
- free: `plan_id_free`.
- plus: `plan_id_plus`.
- vpnbasic: `plan_id_vpnbasic`.
- vpnplus: `plan_id_vpnplus`.
- professional: `plan_id_professional`.
- visionary: `plan_id_visionary`.
- mail2022: `plan_id_mail2022`.
- vpn2022: `plan_id_vpn2022`.
- drive2022: `plan_id_drive2022`.
- bundle2022: `plan_id_bundle2022`.
- pass2023: `plan_id_pass2023`.

Corresponding layouts are defined in `values/plans_layouts.xml`.

Defining a layout consist of declaring 3 arrays :
- The order array (`${prefix}_order`) is defining item type:
  - `string`: Any string.
  - `#proton_users#`: Predefined users feature (e.g. "10 users" or "1 of 10 users").
  - `#proton_storage#`: Predefined storage feature (e.g. "500GB storage").
  - `#proton_addresses#`: Predefined addresses feature (e.g. "15 email addresses" or "1 of 15 addresses").
  - `#proton_calendars#`: Predefined calendars feature (e.g. "25 personal calendars" or "1 of 25 calendars").
  - `#proton_vpn#`: Predefined vpn feature (e.g. "Free VPN on single device" or "High-speed VPN on 10 devices").
  - `#proton_domains#`: Predefined domains feature (e.g. "Support for 1 custom email domain").
  - Note: Predefined features are dynamic according plan capabilities and support singular and plurals (e.g. "1 of 1 address" vs "1 of 10 addresses").
- The icons array (`${prefix}_icons`) is defining item icons (e.g. "@drawable/ic_proton_storage").
- The text array ((`${prefix}`)): Define text of item (e.g. "@string/plan_id_free_header"):
  - The first item is the text header, the plan description during plan selection.
  - The next items are the corresponding texts for other items.

For example, `plan_id_free`:
```
<string-array name="plan_id_free_order" comment="Ordered. Changing these will break the parsing.">
    <item>#proton_storage#</item>
    <item>#proton_addresses#</item>
    <item>#proton_calendars#</item>
    <item>#proton_vpn#</item>
</string-array>
<integer-array name="plan_id_free_icons" comment="Ordered. Changing these will break the parsing.">
    <item>@drawable/ic_proton_storage</item>
    <item>@drawable/ic_proton_envelope</item>
    <item>@drawable/ic_proton_calendar_checkmark</item>
    <item>@drawable/ic_proton_shield</item>
</integer-array>
<integer-array name="plan_id_free">
    <item>@string/plan_id_free_header</item>
    <item>@plurals/item_storage_free</item>
    <item>@plurals/item_address</item>
    <item>@plurals/item_calendar</item>
    <item>@plurals/item_connections</item>
</integer-array>
```

Layouts to be used are defined in `values/plans_mapping.xml`.
- Client can **override** or add any plan layouts if needed, in **their own codebase**.
- Client can **override** any plan mapping if needed, in **their own codebase**.
- Client **cannot** add a new plan id. It must be added in Core first.

By default:
- All plans are using the same current plan layout.
- All plans have their corresponding paid plan layout.
```
<resources>
    <string-array name="plan_mapping_plan_ids">
        <item>free</item>
        <item>plus</item>
        <item>vpnbasic</item>
        <item>vpnplus</item>
        <item>professional</item>
        <item>visionary</item>
        <item>mail2022</item>
        <item>vpn2022</item>
        <item>drive2022</item>
        <item>bundle2022</item>
        <item>pass2023</item>
    </string-array>
    <string-array name="plan_mapping_current_plan_layouts">
        <item>plan_current</item>
        <item>plan_current</item>
        <item>plan_current</item>
        <item>plan_current</item>
        <item>plan_current</item>
        <item>plan_current</item>
        <item>plan_current</item>
        <item>plan_current</item>
        <item>plan_current</item>
        <item>plan_current</item>
        <item>plan_current</item>
    </string-array>
    <string-array name="plan_mapping_paid_plan_layouts">
        <item>plan_id_free</item>
        <item>plan_id_plus</item>
        <item>plan_id_vpnbasic</item>
        <item>plan_id_vpnplus</item>
        <item>plan_id_professional</item>
        <item>plan_id_visionary</item>
        <item>plan_id_mail2022</item>
        <item>plan_id_vpn2022</item>
        <item>plan_id_drive2022</item>
        <item>plan_id_bundle2022</item>
        <item>plan_id_pass2023</item>
    </string-array>
</resources>
```

Here is an example where a client override **current** plans mapping.

Add new layouts in a new `plans_layouts.xml` file, in client side:
```
<resources>
    <string-array name="plan_pass_free_order" comment="Ordered. Changing these will break the parsing.">
        <item>string</item>
        <item>string</item>
        <item>string</item>
        <item>string</item>
    </string-array>
    <integer-array name="plan_pass_free_icons" comment="Ordered.">
        <item>@drawable/ic_proton_infinite</item>
        <item>@drawable/ic_proton_infinite</item>
        <item>@drawable/ic_proton_alias</item>
        <item>@drawable/ic_proton_vault</item>
    </integer-array>
    <integer-array name="plan_pass_free">
        <item>@string/plan_id_pass2023_header</item>
        <item>@string/unlimited_logins_and_notes</item>
        <item>@string/unlimited_devices</item>
        <item>@string/ten_hide_my_email_aliases</item>
        <item>@string/one_vault</item>
    </integer-array>

    <string-array name="plan_pass_bundle2022_order" translatable="false" comment="Ordered.">
        <item>string</item>
        <item>string</item>
        <item>string</item>
        <item>#proton_storage#</item>
        <item>#proton_addresses#</item>
        <item>#proton_vpn#</item>
    </string-array>
    <integer-array name="plan_pass_bundle2022_icons" translatable="false" comment="Ordered.">
        <item>@drawable/ic_proton_lock</item>
        <item>@drawable/ic_proton_vault</item>
        <item>@drawable/ic_proton_alias</item>
        <item>@drawable/ic_proton_storage</item>
        <item>@drawable/ic_proton_at</item>
        <item>@drawable/ic_proton_shield</item>
    </integer-array>
    <integer-array name="plan_pass_bundle2022" comment="Ordered.>
        <item>@string/plan_id_bundle2022_header</item>
        <item>@string/integrated_2fa_authenticator</item>
        <item>@string/multiple_vaults</item>
        <item>@string/unlimited_email_alias</item>
        <item>@plurals/item_storage</item>
        <item>@plurals/item_address</item>
        <item>@plurals/item_connections</item>
    </integer-array>

</resources>
```
And then override mapping in a new `plans_mapping.xml` file, in client side:
```
<resources>
    <string-array name="plan_mapping_plan_ids" comment="Ordered.>
        <item>free</item>
        <item>plus</item>
        <item>vpnbasic</item>
        <item>vpnplus</item>
        <item>professional</item>
        <item>visionary</item>
        <item>mail2022</item>
        <item>vpn2022</item>
        <item>drive2022</item>
        <item>bundle2022</item>
        <item>pass2023</item>
    </string-array>
    <!-- region current plan mapping -->
    <string-array name="plan_mapping_current_plan_layouts" comment="Ordered.>
        <item>plan_pass_free</item>
        <item>plan_pass_free</item>
        <item>plan_pass_free</item>
        <item>plan_pass_free</item>
        <item>plan_pass_bundle2022</item>
        <item>plan_pass_bundle2022</item>
        <item>plan_pass_free</item>
        <item>plan_pass_free</item>
        <item>plan_pass_free</item>
        <item>plan_pass_bundle2022</item>
        <item>plan_id_pass2023</item>
    </string-array>
</resources>
```

