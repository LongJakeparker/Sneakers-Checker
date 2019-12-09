package com.sneakers.sneakerschecker.constant

object Constant {
    const val ETHEREUM_API_URL= "http://128.199.134.167:8545/"

    const val ACCOUNT_UNLINK= "ACCOUNT_UNLINK"

    const val USER_CREDENTIALS= "USER_CREDENTIALS"
    const val APP_CREDENTIALS= "APP_CREDENTIALS"
    const val FCM_TOKEN= "FCM_TOKEN"

    const val LOGIN_USER= "login user"
    const val USER_ROLE_COLLECTOR= "collector"

    const val GRANT_TYPE_PASSWORD= "password"
    const val GRANT_TYPE_REFRESH_TOKEN= "refresh_token"

    const val AUTH_TOKEN_USERNAME= "truegrail_mobile"
    const val AUTH_TOKEN_PASSWORD= "secret"

    const val GRID_COLUMN= 4

    const val EXTRA_SNEAKER_TOKEN = "EXTRA_SNEAKER_TOKEN"
    const val EXTRA_SNEAKER = "EXTRA_SNEAKER"
    const val EXTRA_VALIDATE_SNEAKER = "EXTRA_VALIDATE_SNEAKER"
    const val EXTRA_IS_FROM_AUTHEN = "EXTRA_IS_FROM_AUTHEN"
    const val EXTRA_PRIVATE_KEY = "EXTRA_PRIVATE_KEY"
    const val EXTRA_USER_EMAiL = "EXTRA_USER_EMAiL"
    const val EXTRA_SCAN_TYPE = "EXTRA_SCAN_TYPE"
    const val EXTRA_VERIFICATION_ID = "EXTRA_VERIFICATION_ID"
    const val EXTRA_RESEND_TOKEN = "EXTRA_RESEND_TOKEN"
    const val EXTRA_USER_PHONE = "EXTRA_USER_PHONE"
    const val EXTRA_USER_PASSWORD = "EXTRA_USER_PASSWORD"
    const val EXTRA_USER_PHONE_AUTH_CREDENTIAL = "EXTRA_USER_PHONE_AUTH_CREDENTIAL"
    const val EXTRA_USER_EOS_NAME = "EXTRA_USER_EOS_NAME"



    const val API_FIELD_USER_NAME = "username"
    const val API_FIELD_USER_EMAIL = "email"
    const val API_FIELD_USER_PHONE = "phone"
    const val API_FIELD_USER_PASSWORD = "password"
    const val API_FIELD_REGISTRATION_TOKEN = "registrationToken"
    const val API_FIELD_PUBLIC_KEY = "publicKey"
    const val API_FIELD_ENCRYPTED_PRIVATE_KEY = "encryptedPrivateKey"
    const val API_FIELD_USER_ROLE = "role"
    const val API_FIELD_USER_ADDRESS = "address"
    const val API_FIELD_EOS_NAME = "eosName"
    const val API_FIELD_USER_IDENTITY = "userIdentity"

    const val DIALOG_REQUEST_CODE = 1004

    const val CONTRACT_TABLE_SNEAKER = "sneakers"
    const val CONTRACT_TABLE_USERS = "users"


    const val CHANNEL_TRANSFER = "CHANNEL_TRANSFER"

    class BusMessage {
        companion object {
            val MESS_CLOSE_CHECK_SCREEN = "MESS_CLOSE_CHECK_SCREEN"
        }
    }

    class ItemCondition {
        companion object {
            val NEW = "new"
            val NOT_NEW = "not new"
            val STOLEN = "stolen"
            val FRAUD = "fraud"
        }
    }
}