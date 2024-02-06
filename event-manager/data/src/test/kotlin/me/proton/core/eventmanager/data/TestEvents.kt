/*
 * Copyright (c) 2021 Proton Technologies AG
 * This file is part of Proton Technologies AG and ProtonCore.
 *
 * ProtonCore is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ProtonCore is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ProtonCore.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.proton.core.eventmanager.data

object TestEvents {
    val coreFullEventsResponse =
        """
        {
          "Code": 1000,
          "EventID": "ACXDmTaBub14w==",
          "Refresh": 0,
          "More": 0,

          "Contacts": [
            {
              "ID": "afeaefaeTaBub14w==",
              "Action": 1,
              "Contact": {
                "ID": "a29olIjFv0rnXxBhSMw==",
                "Name": "ProtonMail Features",
                "ContactEmails": [
                  {
                    "ID": "aefew4323jFv0BhSMw==",
                    "Name": "test1",
                    "Email": "features@protonmail.black",
                    "Type": [
                      "work"
                    ],
                    "Defaults": 1,
                    "Order": 1,
                    "ContactID": "a29olIjFv0rnXxBhSMw==",
                    "LabelIDs": [
                      "I6hgx3Ol-d3HYa3E394T_ACXDmTaBub14w=="
                    ]
                  }
                ],
                "LabelIDs": [
                  "I6hgx3Ol-d3HYa3E394T_ACXDmTaBub14w=="
                ],
                "Cards": [
                  {
                    "Type": 2,
                    "Data": "BEGIN:VCARD\\r\\nVERSION:4.0\\r\\nFN:ProtonMail Features\\r\\nUID:proton-legacy-139892c2-f691-4118-8c29-061196013e04\\r\\nitem1.EMAIL;TYPE=work;PREF=1:features@protonmail.black\\r\\nitem2.EMAIL;TYPE=home;PREF=2:features@protonmail.ch\\r\\nEND:VCARD\\r\\n",
                    "Signature": "-----BEGIN PGP SIGNATURE-----.*-----END PGP SIGNATURE-----"
                  }
                ]
              }
            }
          ],
          "ContactEmails": [
            {
              "ID": "sadfaACXDmTaBub14w==",
              "Action": 1,
              "ContactEmail": {
                "ID": "aefew4323jFv0BhSMw==",
                "Name": "test1",
                "Email": "features@protonmail.black",
                "Type": [
                  "work"
                ],
                "Defaults": 1,
                "Order": 1,
                "ContactID": "a29olIjFv0rnXxBhSMw==",
                "LabelIDs": [
                  "I6hgx3Ol-d3HYa3E394T_ACXDmTaBub14w=="
                ]
              }
            }
          ],
          "User": {
            "ID": "MJLke8kWh1BBvG95JBIrZvzpgsZ94hNNgjNHVyhXMiv4g9cn6SgvqiIFR5cigpml2LD_iUk_3DkV29oojTt3eA==",
            "Name": "jason",
            "UsedSpace": 96691332,
            "Currency": "USD",
            "Credit": 0,
            "CreateTime": 1000,
            "MaxSpace": 10737418240,
            "MaxUpload": 26214400,
            "Type": 1,
            "Role": 2,
            "Private": 1,
            "ToMigrate": 1,
            "MnemonicStatus": 1,
            "Subscribed": 1,
            "Services": 1,
            "Delinquent": 0,
            "Email": "jason@protonmail.ch",
            "DisplayName": "Jason",
            "Keys": [{
              "ID": "IlnTbqicN-2HfUGIn-ki8bqZfLqNj5ErUB0z24Qx5g-4NvrrIc6GLvEpj2EPfwGDv28aKYVRRrSgEFhR_zhlkA==",
              "Version": 3,
              "PrivateKey": "-----BEGIN PGP PRIVATE KEY BLOCK-----*-----END PGP PRIVATE KEY BLOCK-----",
              "Fingerprint": "c93f767df53b0ca8395cfde90483475164ec6353",
              "Activation": null,
              "Primary": 1
              "Active": 1
            }]
          },
          "UserSettings": {
            "Email": {
              "Value": "abc@gmail.com",
              "Status": 0,
              "Notify": 1,
              "Reset": 0
            },
            "Phone": {
              "Value": "+18005555555",
              "Status": 0,
              "Notify": 0,
              "Reset": 0
            },
            "Password": {
              "Mode": 2,
              "ExpirationTime": null
            },
            "2FA": {
              "Enabled": 3,
              "Allowed": 3,
              "ExpirationTime": null,
              "U2FKeys": [
                {
                  "Label": "A name",
                  "KeyHandle": "aKeyHandle",
                  "Compromised": 0
                }
              ]
            },
            "News": 244,
            "Locale": "en_US",
            "LogAuth": 2,
            "InvoiceText": "AnyText",
            "Density": 0,
            "Theme": "css",
            "ThemeType": 1,
            "WeekStart": 1,
            "DateFormat": 1,
            "TimeFormat": 1,
            "Welcome": "1",
            "WelcomeFlag": "1",
            "EarlyAccess": "1",
            "FontSize": "14",
            "Flags": {
              "Welcomed": 0
            }
          },
          "MailSettings": {
            "DisplayName": "Put Chinese Here",
            "Signature": "This is my signature",
            "Theme": "<CSS>",
            "AutoResponder": {
              "StartTime": 0,
              "Endtime": 0,
              "Repeat": 0,
              "DaysSelected": [
                "string"
              ],
              "Subject": "Auto",
              "Message": "",
              "IsEnabled": null,
              "Zone": "Europe/Zurich"
            },
            "AutoSaveContacts": 1,
            "AutoWildcardSearch": 1,
            "ComposerMode": 0,
            "MessageButtons": 0,
            "ShowImages": 2,
            "ShowMoved": 0,
            "ViewMode": 0,
            "ViewLayout": 0,
            "SwipeLeft": 3,
            "SwipeRight": 0,
            "AlsoArchive": 0,
            "Hotkeys": 1,
            "Shortcuts": 1,
            "PMSignature": 0,
            "ImageProxy": 0,
            "NumMessagePerPage": 50,
            "DraftMIMEType": "text/html",
            "ReceiveMIMEType": "text/html",
            "ShowMIMEType": "text/html",
            "EnableFolderColor": 0,
            "InheritParentFolderColor": 1,
            "TLS": 0,
            "RightToLeft": 0,
            "AttachPublicKey": 0,
            "Sign": 0,
            "PGPScheme": 16,
            "PromptPin": 0,
            "Autocrypt": 0,
            "StickyLabels": 0,
            "ConfirmLink": 1,
            "DelaySendSeconds": 10,
            "KT": 0,
            "FontSize": null,
            "FontFace": null
          },
          "Addresses": [
            {
              "ID": "q_9v-GXEPLagg81jsUz2mHQ==",
              "Action": 2,
              "Address": {
                "ID": "q_9v-GXEPLagg81jsUz2mHQ==",
                "DomainID": "l8vWAXHBQmvzmKUA==",
                "Email": "test@protonmail.com",
                "Send": 0,
                "Receive": 0,
                "Status": 0,
                "Type": 2,
                "Order": 8,
                "DisplayName": "Namey",
                "Signature": "Sent from <a href=\"https://protonmail.ch\">ProtonMail</a>",
                "HasKeys": 1,
                "Keys": [
                  {
                    "ID": "a0f5_q7xkcyON1blZKTPxmBceURtzhW5Jc1rhtWUw5w2QXCMkSzHNustWtTjUlma9JmiL8O71aimfMOyY3UUGQ==",
                    "Version": 3,
                    "PublicKey": "-----BEGIN PGP PUBLIC KEY BLOCK-----...-----END PGP PUBLIC KEY BLOCK-----",
                    "PrivateKey": "-----BEGIN PGP PRIVATE KEY BLOCK-----...-----END PGP PRIVATE KEY BLOCK-----",
                    "Token": "null or -----BEGIN PGP MESSAGE-----.*-----END PGP MESSAGE-----",
                    "Signature": "null or -----BEGIN PGP SIGNATURE-----.*-----END PGP SIGNATURE-----",
                    "Fingerprint": "e7e5466d21ff064ef870a7a393526f79e83004b0",
                    "Fingerprints": [
                      "e7e5466d21ff064ef870a7a393526f79e83004b0"
                    ],
                    "Activation": null,
                    "Primary": 1
                  }
                ]
              }
            }
          ]
        }
        """.trimIndent()

    val calendarFullEventsResponse =
        """
        {
          "Code": 1000,
          "CalendarModelEventID": "ACXDmTaBub14w==",
          "Refresh": 0,
          "More": 0,

          "Calendars": [
            {
              "ID": "afeaefaeTaBub14w==",
              "Action": 1,
              "Calendar": {
                "ID": "a29olIjFv0rnXxBhSMw=="
              }
            }
          ]
        }
        """.trimIndent()
}
