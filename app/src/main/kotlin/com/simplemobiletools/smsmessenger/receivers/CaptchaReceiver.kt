/*
 * Copyright (C) 2015-2019 The MoKee Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.simplemobiletools.smsmessenger.receivers

import android.content.*
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import com.simplemobiletools.commons.extensions.notificationManager
import com.simplemobiletools.commons.helpers.ensureBackgroundThread
import com.simplemobiletools.smsmessenger.R
import com.simplemobiletools.smsmessenger.extensions.conversationsDB
import com.simplemobiletools.smsmessenger.extensions.markThreadMessagesRead
import com.simplemobiletools.smsmessenger.extensions.updateUnreadCountBadge
import com.simplemobiletools.smsmessenger.helpers.MARK_AS_READ
import com.simplemobiletools.smsmessenger.helpers.THREAD_ID
import com.simplemobiletools.smsmessenger.helpers.refreshMessages


class CaptchaReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val extras: Bundle = intent.extras
            ?: // We have nothing, abort
            return
        val captcha: String? = extras.getString("captcha")
        if (!TextUtils.isEmpty(captcha)) {
            val clipboardManager =
                context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clipData: ClipData = ClipData.newPlainText(null, captcha)
            clipboardManager.setPrimaryClip(clipData)
            Toast.makeText(
                context,
                String.format(context.getString(R.string.captcha_has_copied), captcha),
                Toast.LENGTH_SHORT
            ).show()
        }
        when (intent.action) {
            MARK_AS_READ -> {
                val threadId = intent.getLongExtra(THREAD_ID, 0L)
                context.notificationManager.cancel(threadId.hashCode())
                ensureBackgroundThread {
                    context.markThreadMessagesRead(threadId)
                    context.conversationsDB.markRead(threadId)
                    context.updateUnreadCountBadge(context.conversationsDB.getUnreadConversations())
                    refreshMessages()
                }
            }
        }
    }
}
