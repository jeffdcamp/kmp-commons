package org.dbtools.kmp.commons.text

import android.content.Intent
import android.net.Uri
import android.text.Html


object EmailIntentUtil {
    fun getSendEmailIntent(
        recipient: String,
        subject: String,
        message: String = "",
        messageIsHtml: Boolean = false,
        attachments: List<Uri> = emptyList(),
        intentTitle: String = ""
    ): Intent {
        val intent = Intent(Intent.ACTION_SEND_MULTIPLE)

        intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(recipient))
        intent.putExtra(Intent.EXTRA_SUBJECT, subject)

        intent.setType("*/*")
        // message
        when {
            messageIsHtml -> intent.putExtra(Intent.EXTRA_TEXT, Html.fromHtml(message)) // fromHtml(message, flags) requires min sdk 24 (change when this library supports min sdk 24)
            else -> intent.putExtra(Intent.EXTRA_TEXT, message)
        }

        // attachments
        if (attachments.isNotEmpty()) {
            intent.putExtra(Intent.EXTRA_STREAM, ArrayList<Uri>(attachments))
        }

        return Intent.createChooser(intent, intentTitle)
    }
}
