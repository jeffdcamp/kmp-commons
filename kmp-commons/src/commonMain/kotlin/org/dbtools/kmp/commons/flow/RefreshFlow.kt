@file:Suppress("unused")

package org.dbtools.kmp.commons.flow

import kotlinx.atomicfu.atomic
import kotlinx.coroutines.flow.AbstractFlow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emitAll

/**
 * A Flow that acts as a refresh emitter to restart other Flows.
 *
 * Example Usage:
 * private val refreshFlow = RefreshFlow()
 * val myDataFlow = refreshFlow.flatMapLatest {
 *    repository.getMyDataFromStoreFlow()
 * }
 *
 * fun refresh() {
 *    refreshFlow.refresh()
 * }
 */
class RefreshFlow : AbstractFlow<Int>() {
    private val refreshCount =  atomic(0)
    private val refreshCountFlow = MutableStateFlow(refreshCount.value)

    fun refresh() {
        refreshCountFlow.value = refreshCount.incrementAndGet()
    }

    override suspend fun collectSafely(collector: FlowCollector<Int>) {
        collector.emitAll(refreshCountFlow)
    }
}