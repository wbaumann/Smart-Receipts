package co.smartreceipts.android.workers.widget

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ProgressBar
import android.widget.Toast
import co.smartreceipts.analytics.Analytics
import co.smartreceipts.analytics.events.Events
import co.smartreceipts.analytics.log.Logger.debug
import co.smartreceipts.android.R
import co.smartreceipts.android.activities.NavigationHandler
import co.smartreceipts.android.activities.SmartReceiptsActivity
import co.smartreceipts.android.databinding.GenerateReportLayoutBinding
import co.smartreceipts.android.fragments.ReportInfoFragment
import co.smartreceipts.android.fragments.WBFragment
import co.smartreceipts.android.workers.EmailAssistantKt.EmailOptions
import com.google.common.base.Preconditions
import com.jakewharton.rxbinding3.view.clicks
import dagger.android.support.AndroidSupportInjection
import io.reactivex.Observable
import wb.android.flex.Flex
import java.util.*
import javax.inject.Inject

class GenerateReportFragment : GenerateReportView, WBFragment() {

    @Inject
    lateinit var presenter: GenerateReportPresenter


    @Inject
    lateinit var flex: Flex

    @Inject
    lateinit var analytics: Analytics // to presenter

    @Inject
    lateinit var navigationHandler: NavigationHandler<SmartReceiptsActivity>


    private lateinit var pdfFullCheckbox: CheckBox
    private lateinit var pdfImagesCheckbox: CheckBox
    private lateinit var csvCheckbox: CheckBox
    private lateinit var zipCheckbox: CheckBox
    private lateinit var zipWithMetadataCheckbox: CheckBox
    private lateinit var progress: ProgressBar

    private var _binding: GenerateReportLayoutBinding? = null
    private val binding get() = _binding!!


    override val generateReportClicks: Observable<EnumSet<EmailOptions>>
        get() = binding.receiptActionSend.clicks()
            .map {
                val options = EnumSet.noneOf(EmailOptions::class.java)

                when {
                    pdfFullCheckbox.isChecked -> options.add(EmailOptions.PDF_FULL)
                }
                when {
                    pdfImagesCheckbox.isChecked -> options.add(EmailOptions.PDF_IMAGES_ONLY)
                }
                when {
                    csvCheckbox.isChecked -> options.add(EmailOptions.CSV)
                }
                when {
                    zipWithMetadataCheckbox.isChecked -> options.add(EmailOptions.ZIP_WITH_METADATA)
                }
                when {
                    zipCheckbox.isChecked -> options.add(EmailOptions.ZIP)
                }

                return@map options
            }

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
        progress = binding.progress

        binding.generateReportTooltip.setOnClickListener { v: View? ->
            analytics.record(Events.Informational.ConfigureReport)
            navigationHandler.navigateToSettingsScrollToReportSection()
        }

        return root
    }

    override fun onStart() {
        super.onStart()

        val trip = (parentFragment as ReportInfoFragment?)!!.trip
        Preconditions.checkNotNull(trip, "A valid trip is required")
        presenter.subscribe(trip)
    }

    override fun onStop() {
        presenter.unsubscribe()
        super.onStop()
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

    override fun present(result: EmailResult) {

        when (result) {
            is EmailResult.Success -> {
                // TODO: 04.09.2020
                progress.visibility = View.GONE
                Toast.makeText(activity, "success", Toast.LENGTH_SHORT).show()

            }

            is EmailResult.Error -> {
                progress.visibility = View.GONE
                handleGenerationError(result.errorType)
            }

            EmailResult.InProgress -> {
                progress.visibility = View.VISIBLE
                Toast.makeText(activity, "loading", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun handleGenerationError(error: GenerationErrors) {
        // TODO: 02.09.2020 handle other indicator states

        // TODO: 02.09.2020 handle navigation to the settings when special error occures
        // TODO: 02.09.2020 uncheck items when error
        when (error) {
            GenerationErrors.ERROR_NO_SELECTION -> {
                Toast.makeText(context, flex.getString(context, R.string.DIALOG_EMAIL_TOAST_NO_SELECTION), Toast.LENGTH_SHORT).show()
            }

            GenerationErrors.ERROR_NO_RECEIPTS -> {
                Toast.makeText(context, flex.getString(context, R.string.DIALOG_EMAIL_TOAST_NO_RECEIPTS), Toast.LENGTH_SHORT).show()
            }

            GenerationErrors.ERROR_DISABLED_DISTANCES -> {
                Toast.makeText(
                    context, requireContext().getString(
                        R.string.toast_csv_report_distances, requireContext().getString(R.string.pref_distance_print_table_title)
                    ),
                    Toast.LENGTH_SHORT
                ).show()

                navigationHandler.navigateToSettingsScrollToDistanceSection()
            }

            GenerationErrors.ERROR_TOO_MANY_COLUMNS -> {
                // TODO: 04.09.2020
                /*AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle(R.string.report_pdf_error_too_many_columns_title)
                            .setMessage(
                                    mPreferenceManager.get(UserPreference.ReportOutput.PrintReceiptsTableInLandscape)
                                            ? context.getString(R.string.report_pdf_error_too_many_columns_message)
                                            : context.getString(R.string.report_pdf_error_too_many_columns_message_landscape))
                            .setPositiveButton(R.string.report_pdf_error_go_to_settings, (dialog1, id) -> {
                                dialog1.cancel();
                                navigationHandler.navigateToSettingsScrollToReportSection();
                            })
                            .setNegativeButton(android.R.string.cancel, null)
                            .show();*/
            }

            GenerationErrors.ERROR_GENERAL_PDF -> {
                Toast.makeText(context, R.string.report_pdf_generation_error, Toast.LENGTH_SHORT).show()
            }

            GenerationErrors.ERROR_MEMORY -> {
                // TODO: 04.09.2020 translate
                Toast.makeText(
                    context,
                    "Error: Not enough memory to stamp the images. Try stopping some other apps and try again.",
                    Toast.LENGTH_LONG
                )
                    .show()
            }

            GenerationErrors.ERROR_UNDETERMINED -> {
                // TODO: 04.09.2020 translate
                Toast.makeText(context, "Something went wrong while generating report...", Toast.LENGTH_SHORT).show()
            }
        }
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