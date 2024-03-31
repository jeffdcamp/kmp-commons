@file:Suppress("unused")

package org.dbtools.kmp.commons.ext

inline fun <X, A> runNotNull(it1: X?, block:(X) -> A): A? {
    return if (it1 != null) {
        block(it1)
    } else null
}

inline fun <X, Y, A> runNotNull(it1: X?, it2: Y?, block:(X, Y) -> A): A? {
    return if (it1 != null && it2 != null) {
        block(it1, it2)
    } else null
}

inline fun <X, Y, Z, A> runNotNull(it1: X?, it2: Y?, it3: Z?, block:(X, Y, Z) -> A): A? {
    return if (it1 != null && it2 != null && it3 != null) {
        block(it1, it2, it3)
    } else null
}

inline fun <W, X, Y, Z, A> runNotNull(it1: W?, it2: X?, it3: Y?, it4: Z?, block:(W, X, Y, Z) -> A): A? {
    return if (it1 != null && it2 != null && it3 != null && it4 != null) {
        block(it1, it2, it3, it4)
    } else null
}

inline fun <X, A> runNotNull(iterable: Iterable<X>?, block:(iterable: Iterable<X>) -> A): A? {
    return if (iterable?.all { it != null } == true) {
        block(iterable)
    } else null
}
