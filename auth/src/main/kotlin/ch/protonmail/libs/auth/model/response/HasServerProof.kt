package ch.protonmail.libs.auth.model.response

/**
 * Common interface for models that have [serverProof]
 * @author Davide Farella
 */
internal interface HasServerProof {
    val serverProof: String
}
