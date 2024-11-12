package me.proton.core.paymentiap.domain

public object LogTag {
    /** Errors related to Google In App Purchases. */
    public const val GIAP_ERROR: String = "core.paymentiap.giap.error"

    /** Info related to Google In App Purchases. */
    public const val GIAP_INFO: String = "core.paymentiap.giap.info"

    /** Tag related to Dynamic Plan Prices. */
    public const val PRICE_ERROR: String = "core.paymentiap.giap.error.price"

    /** Errors related to Google In App Product query. */
    public const val GIAP_ERROR_QUERY_PRODUCT: String = "core.paymentiap.giap.error.query.product"

    /** Errors related to Google In App Purchase query. */
    public const val GIAP_ERROR_QUERY_PURCHASE: String = "core.paymentiap.giap.error.query.purchase"

    /** Errors related to Google In App acknowledgment. */
    public const val GIAP_ERROR_ACK: String = "core.paymentiap.giap.error.ack"

}
