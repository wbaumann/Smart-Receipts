package co.smartreceipts.android.workers.widget

import co.smartreceipts.android.workers.EmailAssistantKt
import io.reactivex.Observable
import java.util.*

interface GenerateReportView {

    val generateReportClicks: Observable<EnumSet<EmailAssistantKt.EmailOptions>>


    fun present(result: EmailResult)
}