package co.smartreceipts.android.workers.widget

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.Toast
import co.smartreceipts.analytics.Analytics
import co.smartreceipts.analytics.events.Events
import co.smartreceipts.analytics.log.Logger.debug
import co.smartreceipts.android.R
import co.smartreceipts.android.activities.NavigationHandler
import co.smartreceipts.android.activities.SmartReceiptsActivity
import co.smartreceipts.android.databinding.GenerateReportLayoutBinding
import co.smartreceipts.android.date.DateFormatter
import co.smartreceipts.android.fragments.ReportInfoFragment
import co.smartreceipts.android.fragments.WBFragment
import co.smartreceipts.android.model.Trip
import co.smartreceipts.android.persistence.PersistenceManager
import co.smartreceipts.android.purchases.wallet.PurchaseWallet
import co.smartreceipts.android.settings.UserPreferenceManager
import co.smartreceipts.android.settings.catalog.UserPreference
import co.smartreceipts.android.widget.tooltip.report.generate.GenerateInfoTooltipManager
import co.smartreceipts.android.workers.EmailAssistant
import co.smartreceipts.android.workers.reports.ReportResourcesManager
import com.google.common.base.Preconditions
import dagger.android.support.AndroidSupportInjection
import wb.android.flex.Flex
import java.util.*
import javax.inject.Inject

class GenerateReportFragment : WBFragment(), View.OnClickListener {

    @Inject
    lateinit var presenter: GenerateReportPresenter


    @Inject
    lateinit var flex: Flex

    @Inject
    lateinit var persistenceManager: PersistenceManager

    @Inject
    lateinit var analytics: Analytics

    @Inject
    lateinit var navigationHandler: NavigationHandler<SmartReceiptsActivity>

    @Inject
    lateinit var generateInfoTooltipManager: GenerateInfoTooltipManager

    @Inject
    lateinit var purchaseWallet: PurchaseWallet

    @Inject
    lateinit var preferenceManager: UserPreferenceManager

    @Inject
    lateinit var reportResourcesManager: ReportResourcesManager

    @JvmField
    @Inject
    var dateFormatter: DateFormatter? = null

    private var pdfFullCheckbox: CheckBox? = null
    private var pdfImagesCheckbox: CheckBox? = null
    private var csvCheckbox: CheckBox? = null
    private var zipCheckbox: CheckBox? = null
    private var zipWithMetadataCheckbox: CheckBox? = null

    private var _binding: GenerateReportLayoutBinding? = null
    private val binding get() = _binding!!

    private var trip: Trip? = null
    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = GenerateReportLayoutBinding.inflate(inflater, container, false)

        val root: View = binding.root

        pdfFullCheckbox = flex.getSubView(activity, root, R.id.dialog_email_checkbox_pdf_full) as CheckBox
        pdfImagesCheckbox = flex.getSubView(activity, root, R.id.dialog_email_checkbox_pdf_images) as CheckBox
        csvCheckbox = flex.getSubView(activity, root, R.id.dialog_email_checkbox_csv) as CheckBox
        zipWithMetadataCheckbox = flex.getSubView(activity, root, R.id.dialog_email_checkbox_zip_with_metadata) as CheckBox
        zipCheckbox = binding.dialogEmailCheckboxZip
        binding.receiptActionSend.setOnClickListener(this)
        binding.generateReportTooltip.setOnClickListener { v: View? ->
            analytics.record(Events.Informational.ConfigureReport)
            navigationHandler.navigateToSettingsScrollToReportSection()
        }

        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        debug(this, "onActivityCreated")
        trip = (parentFragment as ReportInfoFragment?)!!.trip
        Preconditions.checkNotNull(trip!!, "A valid trip is required")
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (isVisibleToUser) {
            val actionBar = supportActionBar
            if (actionBar != null) {
                actionBar.subtitle = null
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        debug(this, "pre-onSaveInstanceState")
        super.onSaveInstanceState(outState)
        debug(this, "onSaveInstanceState")
    }

    override fun onClick(v: View) {
        if (!pdfFullCheckbox!!.isChecked && !pdfImagesCheckbox!!.isChecked && !csvCheckbox!!.isChecked &&
            !zipCheckbox!!.isChecked && !zipWithMetadataCheckbox!!.isChecked
        ) {
            Toast.makeText(activity, flex.getString(activity, R.string.DIALOG_EMAIL_TOAST_NO_SELECTION), Toast.LENGTH_SHORT).show()
            return
        }
        analytics.record(Events.Generate.GenerateReports)
        generateInfoTooltipManager.reportWasGenerated()


        // TODO: 25.08.2020
        // TODO: Off the UI thread :/
        if (persistenceManager.database.receiptsTable.getBlocking(trip!!, true).isEmpty()) {
            if (persistenceManager.database.distanceTable.getBlocking(trip!!, true).isEmpty() ||
                !(pdfFullCheckbox!!.isChecked || csvCheckbox!!.isChecked)
            ) {
                // Only allow report processing to continue with no receipts if we're doing a full pdf or CSV report with distances
                Toast.makeText(activity, flex.getString(activity, R.string.DIALOG_EMAIL_TOAST_NO_RECEIPTS), Toast.LENGTH_SHORT).show()
                return
            } else {
                if (csvCheckbox!!.isChecked && !preferenceManager.get(UserPreference.Distance.PrintDistanceTableInReports)) {
                    // user wants to create CSV report with just distances but this option is disabled
                    Toast.makeText(
                        activity,
                        getString(R.string.toast_csv_report_distances, getString(R.string.pref_distance_print_table_title)),
                        Toast.LENGTH_LONG
                    )
                        .show()
                    navigationHandler.navigateToSettingsScrollToDistanceSection()
                    return
                }
                // Uncheck "Illegal" Items
                pdfImagesCheckbox!!.isChecked = false
                zipWithMetadataCheckbox!!.isChecked = false
                zipCheckbox!!.isChecked = false
            }
        }
        val options = EnumSet.noneOf(EmailAssistant.EmailOptions::class.java)
        if (pdfFullCheckbox!!.isChecked) {
            analytics.record(Events.Generate.FullPdfReport)
            options.add(EmailAssistant.EmailOptions.PDF_FULL)
        }
        if (pdfImagesCheckbox!!.isChecked) {
            analytics.record(Events.Generate.ImagesPdfReport)
            options.add(EmailAssistant.EmailOptions.PDF_IMAGES_ONLY)
        }
        if (csvCheckbox!!.isChecked) {
            analytics.record(Events.Generate.CsvReport)
            options.add(EmailAssistant.EmailOptions.CSV)
        }
        if (zipWithMetadataCheckbox!!.isChecked) {
            analytics.record(Events.Generate.ZipWithMetadataReport)
            options.add(EmailAssistant.EmailOptions.ZIP_WITH_METADATA)
        }
        if (zipCheckbox!!.isChecked) {
            analytics.record(Events.Generate.ZipReport)
            options.add(EmailAssistant.EmailOptions.ZIP)
        }
        val emailAssistant = EmailAssistant(
            activity, navigationHandler,
            reportResourcesManager, persistenceManager, trip, purchaseWallet, dateFormatter
        )
        emailAssistant.emailTrip(options)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        @JvmStatic
        fun newInstance(): GenerateReportFragment {
            return GenerateReportFragment()
        }
    }
}