@file:Suppress("unused")
@file:OptIn(ExperimentalAtomicApi::class)

package org.dbtools.kmp.commons.flow

import kotlinx.coroutines.flow.AbstractFlow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.concurrent.atomics.incrementAndFetch

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
    private val refreshCount = AtomicInt(0)
    private val refreshCountFlow = MutableStateFlow(refreshCount.load())

    fun refresh() {
        refreshCountFlow.value = refreshCount.incrementAndFetch()
    }

    override suspend fun collectSafely(collector: FlowCollector<Int>) {
        collector.emitAll(refreshCountFlow)
    }
}