package co.smartreceipts.android.workers

import android.content.Context
import android.content.Intent
import android.net.Uri
import co.smartreceipts.analytics.log.Logger
import co.smartreceipts.android.R
import co.smartreceipts.android.date.DateFormatter
import co.smartreceipts.android.model.Distance
import co.smartreceipts.android.model.Receipt
import co.smartreceipts.android.model.Trip
import co.smartreceipts.android.persistence.PersistenceManager
import co.smartreceipts.android.purchases.wallet.PurchaseWallet
import co.smartreceipts.android.settings.catalog.UserPreference
import co.smartreceipts.android.utils.IntentUtils
import co.smartreceipts.android.workers.reports.ReportResourcesManager
import co.smartreceipts.android.workers.reports.formatting.SmartReceiptsFormattableString
import co.smartreceipts.android.workers.widget.EmailResult
import co.smartreceipts.android.workers.widget.GenerationErrors
import com.hadisatrio.optional.Optional
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import java.io.File
import java.util.*

class EmailAssistant(
    private val context: Context,
    private val reportResourcesManager: ReportResourcesManager,
    private val persistenceManager: PersistenceManager,
    private val purchaseWallet: PurchaseWallet,
    private val dateFormatter: DateFormatter
) {

    enum class EmailOptions(val index: Int) {
        PDF_FULL(0), PDF_IMAGES_ONLY(1), CSV(2), ZIP(3), ZIP_WITH_METADATA(4);

    }

    companion object {

        private const val DEVELOPER_EMAIL = "supp" + "or" + "t@" + "smart" + "receipts" + "." + "co"

        private fun getEmailDeveloperIntent(): Intent {
            val intent = Intent(Intent.ACTION_SENDTO)
            intent.data = Uri.parse("mailto:$DEVELOPER_EMAIL")
            return intent
        }

        @JvmStatic
        fun getEmailDeveloperIntent(subject: String): Intent {
            val intent = getEmailDeveloperIntent()
            intent.putExtra(Intent.EXTRA_SUBJECT, subject)
            return intent
        }

        @JvmStatic
        fun getEmailDeveloperIntent(subject: String, body: String): Intent {
            val intent = getEmailDeveloperIntent(subject)
            intent.putExtra(Intent.EXTRA_TEXT, body)
            return intent
        }

        @JvmStatic
        fun getEmailDeveloperIntent(context: Context, subject: String, body: String, files: List<File>): Intent {
            val intent = IntentUtils.getSendIntent(context, files)
            intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(DEVELOPER_EMAIL))
            intent.putExtra(Intent.EXTRA_SUBJECT, subject)
            intent.putExtra(Intent.EXTRA_TEXT, body)
            return intent
        }
    }

    fun emailTrip(trip: Trip, options: EnumSet<EmailOptions>): Single<EmailResult> {
        if (options.isEmpty()) {
            return Single.just(EmailResult.Error(GenerationErrors.ERROR_NO_SELECTION))
        }

        return Single.zip(
            persistenceManager.database.receiptsTable.get(trip, true),
            persistenceManager.database.distanceTable.get(trip, true),
            BiFunction<List<Receipt>, List<Distance>, EmailResult> { receipts, distances ->
                val preGenerationIssues = checkPreGenerationIssues(receipts, distances, options)

                when {
                    preGenerationIssues.isPresent -> preGenerationIssues.get()
                    else -> writeReport(trip, receipts, distances, options)
                }
            })
    }


    private fun checkPreGenerationIssues(
        receipts: List<Receipt>,
        distances: List<Distance>,
        options: EnumSet<EmailOptions>
    ): Optional<EmailResult.Error> {
        if (receipts.isEmpty()) {
            if (distances.isEmpty() || !(options.contains(EmailOptions.CSV) || options.contains(EmailOptions.PDF_FULL))) {
                // Only allow report processing to continue with no receipts if we're doing a full pdf or CSV report with distances
                return Optional.of(EmailResult.Error(GenerationErrors.ERROR_NO_RECEIPTS))
            } else {
                if (options.contains(EmailOptions.CSV) && !persistenceManager.preferenceManager.get(UserPreference.Distance.PrintDistanceTableInReports)) {
                    // user wants to create CSV report with just distances but this option is disabled
                    return Optional.of(EmailResult.Error(GenerationErrors.ERROR_DISABLED_DISTANCES))
                }
            }
        }

        return Optional.absent()
    }

    private fun writeReport(trip: Trip, receipts: List<Receipt>, distances: List<Distance>, options: EnumSet<EmailOptions>): EmailResult {
        val writer = AttachmentFilesWriter(context, persistenceManager, reportResourcesManager, purchaseWallet, dateFormatter)

        val writerResults = writer.write(trip, receipts, distances, options)

        return when {
            writerResults.didMemoryErrorOccurre -> EmailResult.Error(GenerationErrors.ERROR_MEMORY)
            writerResults.didPDFFailCompletely -> {
                if (writerResults.didPDFFailTooManyColumns) {
                    EmailResult.Error(GenerationErrors.ERROR_TOO_MANY_COLUMNS)
                } else {
                    EmailResult.Error(GenerationErrors.ERROR_PDF_GENERATION)
                }
            }
            else -> {
                val sendIntent = prepareSendAttachmentsIntent(writerResults.files.filterNotNull(), trip)
                EmailResult.Success(sendIntent)
            }
        }
    }

    private fun prepareSendAttachmentsIntent(attachments: List<File>, trip: Trip): Intent {
        val bodyBuilder = StringBuilder()

        for (attachment in attachments) {
            if (attachment.length() > 5000000) { //Technically, this should be 5,242,880 but I'd rather give a warning buffer
                bodyBuilder.append("\n")
                bodyBuilder.append(context.getString(R.string.email_body_subject_5mb_warning, attachment.absolutePath))
            }
        }

        Logger.info(this, "Built the following files [{}].", attachments)

        var body = bodyBuilder.toString()

        if (body.isNotEmpty()) {
            body = "\n\n" + body
        }

        when {
            attachments.size == 1 -> body = context.getString(R.string.report_attached).toString() + body
            attachments.size > 1 -> body =
                context.getString(R.string.reports_attached, Integer.toString(attachments.size)).toString() + body
        }

        val emailIntent: Intent = IntentUtils.getSendIntent(context, attachments)
        val to = persistenceManager.preferenceManager.get(UserPreference.Email.ToAddresses).split(";".toRegex()).toTypedArray()
        val cc = persistenceManager.preferenceManager.get(UserPreference.Email.CcAddresses).split(";".toRegex()).toTypedArray()
        val bcc = persistenceManager.preferenceManager.get(UserPreference.Email.BccAddresses).split(";".toRegex()).toTypedArray()
        emailIntent.putExtra(Intent.EXTRA_EMAIL, to)
        emailIntent.putExtra(Intent.EXTRA_CC, cc)
        emailIntent.putExtra(Intent.EXTRA_BCC, bcc)

        val receipts: List<Receipt> = ArrayList(persistenceManager.database.receiptsTable.getBlocking(trip, false))
        val distances: List<Distance> = ArrayList(persistenceManager.database.distanceTable.getBlocking(trip, false))

        emailIntent.putExtra(
            Intent.EXTRA_SUBJECT,
            SmartReceiptsFormattableString(
                persistenceManager.preferenceManager.get(UserPreference.Email.Subject),
                trip,
                persistenceManager.preferenceManager,
                dateFormatter,
                receipts,
                distances
            ).toString()
        )
        emailIntent.putExtra(Intent.EXTRA_TEXT, body)

        Logger.debug(this, "Built the send intent {} with extras {}.", emailIntent, emailIntent.extras)

        return emailIntent
    }

}