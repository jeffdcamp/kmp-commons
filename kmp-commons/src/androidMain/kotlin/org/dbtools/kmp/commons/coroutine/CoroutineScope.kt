@file:Suppress("unused")

package org.dbtools.kmp.commons.coroutine

import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CoroutineScope

/**
 * Replaces GlobalScope
 */
@Deprecated("Use CoroutinesModule to provide ioDispatcher, defaultDispatcher, appScope", ReplaceWith(""))
val ProcessScope: CoroutineScope get() = ProcessLifecycleOwner.get().lifecycleScope
