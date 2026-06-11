package ava.sluff.money_tracker.notification

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import ava.sluff.money_tracker.MoneyTrackerApp
import ava.sluff.money_tracker.R
import ava.sluff.money_tracker.util.MoneyFormat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryNotificationManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    @SuppressLint("MissingPermission")
    fun showCategoryPrompt(
        transactionId: Long,
        amount: Double,
        merchantName: String?,
        topCategories: List<Pair<Long, String>>
    ) {
        val formatted = MoneyFormat.amount(amount)
        val contentText = if (merchantName != null) {
            "$merchantName — $formatted"
        } else {
            "Transaction: $formatted"
        }
        val builder = NotificationCompat.Builder(context, MoneyTrackerApp.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_money)
            .setContentTitle("Categorize Transaction")
            .setContentText(contentText)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        topCategories.take(3).forEach { (categoryId, categoryName) ->
            val intent = Intent(context, NotificationActionReceiver::class.java).apply {
                action = NotificationActionReceiver.ACTION_CATEGORIZE
                putExtra(NotificationActionReceiver.EXTRA_TRANSACTION_ID, transactionId)
                putExtra(NotificationActionReceiver.EXTRA_CATEGORY_ID, categoryId)
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                (10 * transactionId + categoryId).toInt(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.addAction(0, categoryName, pendingIntent)
        }

        if (NotificationManagerCompat.from(context).areNotificationsEnabled()) {
            NotificationManagerCompat.from(context).notify(transactionId.toInt(), builder.build())
        }
    }
}
