package org.dbtools.kmp.commons.firebase.firestore

import co.touchlab.kermit.Logger
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class QueryListener(
    private val query: Query,
    val name: String = query.toString(),
    private val onChange: suspend (List<DocumentChange>) -> Unit
) : EventListener<QuerySnapshot>, CoroutineScope {
    private val listenerUtil = FirebaseListenerUtil()
    private var listener: ListenerRegistration? = null
    private var scopeJob: Job? = null
    override val coroutineContext get() = Dispatchers.IO + requireNotNull(scopeJob)

    init {
        start()
    }

    fun start() {
        scopeJob?.cancel()
        scopeJob = Job()
        if (listener == null) {
            listener = query.addSnapshotListener(this)
        }
    }

    fun stop() {
        listener?.remove()
        listener = null
        scopeJob?.cancel()
    }

    override fun onEvent(querySnapshot: QuerySnapshot?, e: FirebaseFirestoreException?) {
        when {
            e != null -> {
                Logger.e(e) { name }
                // when an error occurs the listener is already dead, so we must restart it
                if (listener != null) {
                    listenerUtil.restartAfterError {
                        stop()
                        start()
                    }
                }
            }
            querySnapshot != null -> launch {
                onChange(querySnapshot.documentChanges)
            }
        }
    }
}
