package org.dbtools.kmp.commons.firebase.firestore

import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOn

/**
 * Create a [Flow] of [DocumentSnapshot] from a [DocumentReference]
 */
fun documentFlow(reference: DocumentReference): Flow<DocumentSnapshot> = documentFlow(listOf(reference))

/**
 * Create a [Flow] of [DocumentSnapshot] from a set of [DocumentReference]s
 */
fun documentFlow(references: Iterable<DocumentReference>): Flow<DocumentSnapshot> {
    if (!references.iterator().hasNext()) {
        return emptyFlow()
    }
    return callbackFlow {
        val listeners = references.map { documentReference ->
            DocumentListener(documentReference) {
                send(it)
            }
        }
        awaitClose {
            listeners.forEach { it.stop() }
        }
    }.flowOn(Dispatchers.IO)
}
