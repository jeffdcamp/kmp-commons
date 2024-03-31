package org.dbtools.kmp.commons.firebase.firestore

import co.touchlab.kermit.Logger
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch


/**
 * Listener on a Firestore document reference. When the snapshot is available or changes the caller is notified.
 */
class DocumentListener(
    private val reference: DocumentReference,
    val name: String = reference.path,
    private val process: suspend (DocumentSnapshot) -> Unit
) : EventListener<DocumentSnapshot>, CoroutineScope {
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
            listener = reference.addSnapshotListener(this)
        }
    }

    fun stop() {
        listener?.remove()
        listener = null
        scopeJob?.cancel()
    }

    override fun onEvent(documentSnapshot: DocumentSnapshot?, e: FirebaseFirestoreException?) {
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
            documentSnapshot != null -> launch { process(documentSnapshot) }
        }
    }
}
