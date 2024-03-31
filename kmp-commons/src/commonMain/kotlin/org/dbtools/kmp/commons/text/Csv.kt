package org.dbtools.kmp.commons.text

fun <T> List<T>.toCsv(
    headers: List<String>,
    itemToRowValues: (T) -> List<Any>
): String = buildString {
    // Headers
    val headersCount = headers.size
    append(headers.joinToString(",") { "\"$it\"" })
    append("\n")

    // Rows
    this@toCsv.forEach { rowItem ->
        val rowTextBuilder = StringBuilder()
        val rowValues = itemToRowValues(rowItem)

        // make sure we have matching headers for rows
        require(headersCount == rowValues.size) { "Row columns count (${rowValues.size}) does NOT match headers count ($headersCount) for: $rowItem" }

        rowValues.forEachIndexed { index, column ->
            if (index > 0) {
                rowTextBuilder.append(",")
            }

            when (column) {
                is Number -> rowTextBuilder.append("$column")
                is Boolean -> rowTextBuilder.append("${if (column) 1 else 0}")
                else -> rowTextBuilder.append("\"$column\"")
            }
        }

        append(rowTextBuilder.toString())
        append("\n")
    }
}
