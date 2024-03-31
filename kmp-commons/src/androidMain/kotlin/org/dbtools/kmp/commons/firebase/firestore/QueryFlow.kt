package org.dbtools.kmp.commons.firebase.firestore

import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOn

/**
 * Create a [Flow] of [DocumentChange] from a [Query]
 */
fun queryFlow(query: Query): Flow<List<DocumentChange>> = queryFlow(listOf(query))

/**
 * Create a [Flow] of [DocumentChange] from as set of [Query]s
 */
fun queryFlow(queries: Iterable<Query>): Flow<List<DocumentChange>> {
    if (!queries.iterator().hasNext()) {
        return emptyFlow()
    }
    return callbackFlow {
        val listeners = queries.map { query ->
            QueryListener(query) {
                send(it)
            }
        }
        awaitClose {
            listeners.forEach { it.stop() }
        }
    }.flowOn(Dispatchers.IO)
}
