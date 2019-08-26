package com.sneakers.sneakerschecker.constant

object Constant {
    const val CONTRACT_ADDRESS = "0xc482C0Ab3fAa90821c7a01d6D22df2DBcf82bA83"
    const val ETHEREUM_API_URL= "http://128.199.134.167:8545/"

    const val ACCOUNT_UNLINK= "ACCOUNT_UNLINK"

    const val USER_CREDENTIALS= "USER_CREDENTIALS"
    const val APP_CREDENTIALS= "APP_CREDENTIALS"
    const val FCM_TOKEN= "FCM_TOKEN"

    const val WALLET_USER= "wallet user"

    const val GRANT_TYPE_PASSWORD= "password"
    const val GRANT_TYPE_REFRESH_TOKEN= "refresh_token"

    const val AUTH_TOKEN_USERNAME= "truegrailmobile"
    const val AUTH_TOKEN_PASSWORD= "secret"

    const val GRID_COLUMN= 4

    const val EXTRA_SNEAKER_TOKEN = "EXTRA_SNEAKER_TOKEN"
    const val EXTRA_SNEAKER = "EXTRA_SNEAKER"
    const val EXTRA_IS_FROM_AUTHEN = "EXTRA_IS_FROM_AUTHEN"
    const val EXTRA_PRIVATE_KEY = "EXTRA_PRIVATE_KEY"
    const val EXTRA_USER_EMAiL = "EXTRA_USER_EMAiL"



    const val API_FIELD_USER_NAME = "username"
    const val API_FIELD_USER_EMAIL = "email"
    const val API_FIELD_USER_PASSWORD = "password"
    const val API_FIELD_NETWORK_ADDRESS = "networkAddress"
    const val API_FIELD_REGISTRATION_TOKEN = "registrationToken"


    const val CHANNEL_TRANSFER = "CHANNEL_TRANSFER"

    class BusMessage {
        companion object {
            val MESS_CLOSE_CHECK_SCREEN = "MESS_CLOSE_CHECK_SCREEN"
        }
    }

    class ItemCondition {
        companion object {
            val ISSUED = "issued"
            val STOLEN = "stolen"
        }
    }
}