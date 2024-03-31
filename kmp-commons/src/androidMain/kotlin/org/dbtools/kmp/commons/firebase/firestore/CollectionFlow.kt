@file:Suppress("unused")

package org.dbtools.kmp.commons.firebase.firestore

import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn

/**
 * Create a [Flow] of [Collection] from a set of [DocumentReference]s
 */
fun <T> collectionFlow(references: Iterable<DocumentReference>, convert: suspend (DocumentSnapshot) -> T?): Flow<List<T>> {
    if (!references.iterator().hasNext()) return flowOf(emptyList())
    val flow = documentFlow(references)
    return documentCollectionFlow(flow, convert)
}

/**
 * Create a [Flow] of [Collection] from a [Query]
 */
fun <T> collectionFlow(query: Query, convert: suspend (DocumentSnapshot) -> T?): Flow<List<T>> {
    val flow = queryFlow(query)
    return queryCollectionFlow(flow, convert)
}

/**
 * Create a [Flow] of [Collection] from a set of [Query]
 */
fun <T> queriesCollectionFlow(queries: Iterable<Query>, convert: suspend (DocumentSnapshot) -> T?): Flow<List<T>> {
    if (!queries.iterator().hasNext()) return flowOf(emptyList())
    val flow = queryFlow(queries)
    return queryCollectionFlow(flow, convert)
}

/*
 * Create a [Flow] of [Collection] from a [Flow] of [DocumentSnapshot]
 */
private fun <T> documentCollectionFlow(documents: Flow<DocumentSnapshot>, convert: suspend (DocumentSnapshot) -> T?): Flow<List<T>> = flow {
    val map = mutableMapOf<String, T>()

    emit(emptyList()) // start with an empty list because no more snapshots may be returned
    documents.collect { documentSnapshot ->
        val key = documentSnapshot.reference.path
        val value = convert(documentSnapshot)
        val values = synchronized(map) {
            when (value) {
                null -> map.remove(key)
                else -> map[key] = value
            }
            ArrayList(map.values)
        }
        emit(values)
    }
}.flowOn(Dispatchers.IO)

/*
 * Create a [Flow] of [Collection] from a [Flow] of [DocumentChange]
 */
private fun <T> queryCollectionFlow(changes: Flow<List<DocumentChange>>, convert: suspend (DocumentSnapshot) -> T?): Flow<List<T>> = flow {
    val map = mutableMapOf<String, T>()

    emit(emptyList()) // start with an empty list because no more snapshots may be returned
    changes.collect { documentChangeList ->
        documentChangeList.forEach { documentChange ->
            val key = documentChange.document.reference.path
            val value = convert(documentChange.document)
            synchronized(map) {
                when {
                    documentChange.type == DocumentChange.Type.REMOVED || value == null -> map.remove(key)
                    else -> map[key] = value
                }
            }
        }
        emit(map.values.toList())
    }
}.flowOn(Dispatchers.IO)
