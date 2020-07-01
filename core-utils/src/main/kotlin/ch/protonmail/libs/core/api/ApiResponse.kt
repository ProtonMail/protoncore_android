//package ch.protonmail.libs.core.api
//
//import ch.protonmail.libs.api.Field.CODE
//import ch.protonmail.libs.api.Field.DETAILS
//import ch.protonmail.libs.api.Field.ERROR
//import kotlinx.serialization.SerialName
//import kotlinx.serialization.Serializable
//
///**
// * Sealed class for an Api Response
// * @author Davide Farella
// */
//@Serializable
//sealed class ApiResponse<DataType> {
//
//    @Serializable
//    open class Success<DataType> : ApiResponse<DataType>()
//
//    @Serializable
//    open class Failure<DetailType>(
//
//        @SerialName(CODE)
//        val code: Int,
//
//        @SerialName(ERROR)
//        val error: String,
//
//        @Suppress("unused") @SerialName(DETAILS)
//        val details: Map<String, DetailType>
//
//    ) : ApiResponse<Nothing>()
//}
