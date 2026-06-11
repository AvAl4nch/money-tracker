package ava.sluff.money_tracker.sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import ava.sluff.money_tracker.domain.usecase.ProcessSmsUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SmsBroadcastReceiver : BroadcastReceiver() {

    @Inject
    lateinit var processSmsUseCase: ProcessSmsUseCase

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent) ?: return
        val bySender = messages
            .filterNotNull()
            .groupBy { it.displayOriginatingAddress ?: "" }
            .mapValues { (_, parts) -> parts.joinToString("") { it.displayMessageBody ?: "" } }

        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                bySender.forEach { (sender, body) ->
                    if (sender.isNotBlank() && body.isNotBlank()) {
                        processSmsUseCase(sender, body)
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
