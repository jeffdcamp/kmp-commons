package org.dbtools.kmp.commons.firebase.firestore

import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.Flow

/**
 * Create a [Flow] of [DocumentSnapshot] from a [DocumentReference]
 */
fun DocumentReference.documentFlow(): Flow<DocumentSnapshot> = documentFlow(listOf(this))

/**
 * Create a [Flow] of [Collection] from a set of [DocumentReference]s
 */
fun <T> Iterable<DocumentReference>.collectionFlow(convert: suspend (DocumentSnapshot) -> T?): Flow<List<T>> = collectionFlow(this, convert)

/**
 * Create a [Flow] of [Collection] from a [Query]
 */
fun <T> Query.collectionFlow(convert: suspend (DocumentSnapshot) -> T?): Flow<List<T>> = collectionFlow(this, convert)

/**
 * Create a [Flow] of [DocumentChange] from a [Query]
 */
fun Query.queryFlow(): Flow<List<DocumentChange>> = queryFlow(listOf(this))