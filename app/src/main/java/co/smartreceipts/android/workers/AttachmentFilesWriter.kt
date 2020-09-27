package co.smartreceipts.android.workers

import android.content.Context
import android.graphics.*
import android.graphics.Bitmap.CompressFormat
import android.graphics.Paint.Align
import co.smartreceipts.analytics.log.Logger
import co.smartreceipts.android.R
import co.smartreceipts.android.date.DateFormatter
import co.smartreceipts.android.filters.LegacyReceiptFilter
import co.smartreceipts.android.model.*
import co.smartreceipts.android.model.comparators.ReceiptDateComparator
import co.smartreceipts.android.model.converters.DistanceToReceiptsConverter
import co.smartreceipts.android.model.factory.PriceBuilderFactory
import co.smartreceipts.android.model.impl.columns.categories.CategoryColumnDefinitions
import co.smartreceipts.android.model.impl.columns.distance.DistanceColumnDefinitions
import co.smartreceipts.android.persistence.PersistenceManager
import co.smartreceipts.android.persistence.database.controllers.grouping.GroupingController
import co.smartreceipts.android.purchases.wallet.PurchaseWallet
import co.smartreceipts.android.settings.UserPreferenceManager
import co.smartreceipts.android.settings.catalog.UserPreference
import co.smartreceipts.android.workers.EmailAssistant.EmailOptions
import co.smartreceipts.android.workers.reports.Report
import co.smartreceipts.android.workers.reports.ReportGenerationException
import co.smartreceipts.android.workers.reports.ReportResourcesManager
import co.smartreceipts.android.workers.reports.csv.CsvReportWriter
import co.smartreceipts.android.workers.reports.csv.CsvTableGenerator
import co.smartreceipts.android.workers.reports.pdf.PdfBoxFullPdfReport
import co.smartreceipts.android.workers.reports.pdf.PdfBoxImagesOnlyReport
import co.smartreceipts.android.workers.reports.pdf.misc.TooManyColumnsException
import java.io.File
import java.io.IOException
import java.util.*

class AttachmentFilesWriter(
    private val context: Context,
    private val persistenceManager: PersistenceManager,
    private val reportResourcesManager: ReportResourcesManager,
    private val purchaseWallet: PurchaseWallet,
    private val dateFormatter: DateFormatter
) {

    companion object {
        private const val IMG_SCALE_FACTOR = 2.1f
        private const val HW_RATIO = 0.75f
    }

    class WriterResults {
        var didPDFFailCompletely = false
        var didPDFFailTooManyColumns = false
        var didCSVFailCompletely = false
        var didZIPFailCompletely = false
        var didMemoryErrorOccurre = false

        val files: Array<File?> = arrayOfNulls(EmailOptions.values().size)
    }

    fun write(trip: Trip, receiptsList: List<Receipt>, distancesList: List<Distance>, options: EnumSet<EmailOptions>): WriterResults {

        // Make our trip output directory exists in a good state
        var dir = trip.directory
        if (!dir.exists()) {
            dir = persistenceManager.storageManager.getFile(trip.name)
            if (!dir.exists()) {
                dir = persistenceManager.storageManager.mkdir(trip.name)
            }
        }

        val results = WriterResults()

        Logger.info(this, "Generating the following report types {}.", options)

        if (options.contains(EmailOptions.PDF_FULL)) {
            generateFullPdf(trip, results)
        }

        if (options.contains(EmailOptions.PDF_IMAGES_ONLY) && receiptsList.isNotEmpty()) {
            generateImagesPdf(trip, results)
        }

        if (options.contains(EmailOptions.CSV)) {
            generateCsv(trip, receiptsList, distancesList, dir, results)
        }

        if (options.contains(EmailOptions.ZIP) && receiptsList.isNotEmpty()) {
            generateZip(trip, receiptsList, dir, results)
        }

        if (options.contains(EmailOptions.ZIP_WITH_METADATA) && receiptsList.isNotEmpty()) {
            generateZipWithMetadata(trip, receiptsList, dir, options.contains(EmailOptions.ZIP), results)
        }

        return results

    }

    private fun generateFullPdf(trip: Trip, results: WriterResults) {
        val pdfFullReport: Report = PdfBoxFullPdfReport(
            reportResourcesManager, persistenceManager.database,
            persistenceManager.preferenceManager, persistenceManager.storageManager,
            purchaseWallet, dateFormatter
        )

        try {
            results.files[EmailOptions.PDF_FULL.index] = pdfFullReport.generate(trip)
        } catch (e: ReportGenerationException) {
            if (e.cause is TooManyColumnsException) {
                results.didPDFFailTooManyColumns = true
            }
            results.didPDFFailCompletely = true
        }
    }

    private fun generateImagesPdf(trip: Trip, results: WriterResults) {
        val pdfImagesReport: Report = PdfBoxImagesOnlyReport(reportResourcesManager, persistenceManager, dateFormatter)
        try {
            results.files[EmailOptions.PDF_IMAGES_ONLY.index] = pdfImagesReport.generate(trip)
        } catch (e: ReportGenerationException) {
            results.didPDFFailCompletely = true
        }
    }

    private fun generateCsv(trip: Trip, receiptsList: List<Receipt>, distancesList: List<Distance>, dir: File, results: WriterResults) {
        val printFooters: Boolean = persistenceManager.preferenceManager.get(UserPreference.ReportOutput.ShowTotalOnCSV)

        try {
            persistenceManager.storageManager.delete(dir, dir.name + ".csv")

            // TODO: 04.09.2020 reduce using of blockingGet
            val csvColumns: List<Column<Receipt>> = persistenceManager.database.csvTable.get().blockingGet()
            val csvTableGenerator = CsvTableGenerator(
                reportResourcesManager,
                csvColumns,
                true,
                printFooters,
                LegacyReceiptFilter(persistenceManager.preferenceManager)
            )


            val receipts: MutableList<Receipt> = ArrayList(receiptsList)
            val distances: MutableList<Distance> = ArrayList(distancesList)

            // Receipts table
            if (persistenceManager.preferenceManager.get(UserPreference.Distance.PrintDistanceAsDailyReceiptInReports)) {
                receipts.addAll(DistanceToReceiptsConverter(context, dateFormatter).convert(distances))
                Collections.sort(receipts, ReceiptDateComparator())
            }

            var data = csvTableGenerator.generate(receipts)

            // Distance table
            if (persistenceManager.preferenceManager.get(UserPreference.Distance.PrintDistanceTableInReports)) {
                if (distances.isNotEmpty()) {
                    distances.reverse() // Reverse the list, so we print the most recent one first

                    // CSVs cannot print special characters
                    val distanceColumnDefinitions: ColumnDefinitions<Distance> =
                        DistanceColumnDefinitions(reportResourcesManager, persistenceManager.preferenceManager, dateFormatter, true)
                    val distanceColumns = distanceColumnDefinitions.allColumns
                    data += "\n\n"
                    data += CsvTableGenerator(
                        reportResourcesManager, distanceColumns,
                        true, printFooters
                    ).generate(distances)
                }
            }

            // Categorical summation table
            if (persistenceManager.preferenceManager.get(UserPreference.PlusSubscription.CategoricalSummationInReports)) {
                val sumCategoryGroupingResults =
                    GroupingController(persistenceManager.database, context, persistenceManager.preferenceManager)
                        .getSummationByCategory(trip)
                        .toList()
                        .blockingGet() // TODO: 04.09.2020 reduce using of blockingGet
                var isMultiCurrency = false
                for (sumCategoryGroupingResult in sumCategoryGroupingResults) {
                    if (sumCategoryGroupingResult.isMultiCurrency) {
                        isMultiCurrency = true
                        break
                    }
                }
                val taxEnabled: Boolean = persistenceManager.preferenceManager.get(UserPreference.Receipts.IncludeTaxField)
                val categoryColumns = CategoryColumnDefinitions(reportResourcesManager, isMultiCurrency, taxEnabled)
                    .allColumns
                data += "\n\n"
                data += CsvTableGenerator(
                    reportResourcesManager, categoryColumns,
                    true, printFooters
                ).generate(sumCategoryGroupingResults)
            }

            // Separated tables for each category
            if (persistenceManager.preferenceManager.get(UserPreference.PlusSubscription.SeparateByCategoryInReports)) {
                val groupingResults = GroupingController(persistenceManager.database, context, persistenceManager.preferenceManager)
                    .getReceiptsGroupedByCategory(trip)
                    .toList()
                    .blockingGet() // TODO: 04.09.2020 reduce using of blockingGet
                for (groupingResult in groupingResults) {
                    data += "\n\n" + groupingResult.category.name + "\n";
                    data += CsvTableGenerator(reportResourcesManager, csvColumns, true, printFooters).generate(groupingResult.receipts)
                }
            }

            val csvFile = File(dir, dir.name + ".csv")
            results.files[EmailOptions.CSV.index] = csvFile
            CsvReportWriter(csvFile).write(data)

        } catch (e: IOException) {
            Logger.error(this, "Failed to write the csv file", e)
            results.didCSVFailCompletely = true;
        }
    }

    private fun generateZip(trip: Trip, receiptsList: List<Receipt>, directory: File, results: WriterResults) {
        var dir = directory
        persistenceManager.storageManager.delete(dir, dir.name + ".zip")
        dir = persistenceManager.storageManager.mkdir(trip.directory, trip.name)
        for (i in receiptsList.indices) {
            val receipt = receiptsList[i]
            if (!filterOutReceipt(persistenceManager.preferenceManager, receipt) && receipt.file != null && receipt.file.exists()) {
                val data = persistenceManager.storageManager.read(receipt.file)
                if (data != null) persistenceManager.storageManager.write(dir, receipt.file.name, data)
            }
        }
        val zip: File = persistenceManager.storageManager.zipBuffered(dir, 2048)
        persistenceManager.storageManager.deleteRecursively(dir)
        results.files[EmailAssistant.EmailOptions.ZIP.index] = zip
    }

    private fun generateZipWithMetadata(
        trip: Trip,
        receiptsList: List<Receipt>,
        dir: File,
        isZipGenerationIncluded: Boolean,
        results: WriterResults
    ) {
        val zipDir = if (isZipGenerationIncluded) {
            persistenceManager.storageManager.delete(dir, dir.name + "_stamped" + ".zip")
            persistenceManager.storageManager.mkdir(trip.directory, trip.name + "_stamped")
        } else {
            persistenceManager.storageManager.delete(dir, dir.name + ".zip")
            persistenceManager.storageManager.mkdir(trip.directory, trip.name)
        }
        for (i in receiptsList.indices) {
            val receipt = receiptsList[i]
            if (!filterOutReceipt(persistenceManager.preferenceManager, receipt)) {
                if (receipt.hasImage()) {
                    val csvColumns: List<Column<Receipt>> =
                        persistenceManager.database.getCSVTable().get().blockingGet() // TODO: 04.09.2020 reduce using of blockingGet
                    val userCommentBuilder = StringBuilder()
                    for (col in csvColumns) {
                        userCommentBuilder.append(reportResourcesManager.getFlexString(col.headerStringResId))
                        userCommentBuilder.append(": ")
                        userCommentBuilder.append(col.getValue(receipt))
                        userCommentBuilder.append("\n")
                    }
                    val userComment = userCommentBuilder.toString()
                    try {
                        var b: Bitmap? = stampImage(trip, receipt, Bitmap.Config.ARGB_8888)
                        if (b != null) {
                            persistenceManager.storageManager.writeBitmap(zipDir, b, receipt.file!!.name, CompressFormat.JPEG, 85, userComment)
                            b.recycle()
                            b = null
                        }
                    } catch (e: OutOfMemoryError) {
                        Logger.error(this, "Trying to recover from OOM", e)
                        System.gc()
                        try {
                            val b: Bitmap? = stampImage(trip, receipt, Bitmap.Config.RGB_565)
                            if (b != null) {
                                persistenceManager.storageManager.writeBitmap(
                                    zipDir,
                                    b,
                                    receipt.file!!.name,
                                    CompressFormat.JPEG,
                                    85,
                                    userComment
                                )
                                b.recycle()
                            }
                        } catch (e2: OutOfMemoryError) {
                            Logger.error(this, "Failed to recover from OOM", e2)
                            results.didZIPFailCompletely = true
                            results.didMemoryErrorOccurre = true
                            break
                        }
                    }
                } else if (receipt.hasPDF()) {
                    val data = persistenceManager.storageManager.read(receipt.file)
                    if (data != null) persistenceManager.storageManager.write(zipDir, receipt.file!!.name, data)
                }
            }
        }
        val zipWithMetadata: File = persistenceManager.storageManager.zipBuffered(zipDir, 2048)
        persistenceManager.storageManager.deleteRecursively(zipDir)
        results.files[EmailOptions.ZIP_WITH_METADATA.index] = zipWithMetadata
    }

    /**
     * Applies a particular filter to determine whether or not this receipt should be
     * generated for this report
     *
     * @param preferences - User preferences
     * @param receipt     - The particular receipt
     * @return true if if should be filtered out, false otherwise
     */
    private fun filterOutReceipt(preferences: UserPreferenceManager, receipt: Receipt): Boolean {
        return if (preferences.get(UserPreference.Receipts.OnlyIncludeReimbursable) && !receipt.isReimbursable) {
            true
        } else receipt.price.priceAsFloat < preferences.get(UserPreference.Receipts.MinimumReceiptPrice)
    }

    private fun stampImage(trip: Trip, receipt: Receipt, config: Bitmap.Config): Bitmap? {
        if (!receipt.hasImage()) {
            return null
        }
        var foreground: Bitmap? = persistenceManager.storageManager.getMutableMemoryEfficientBitmap(receipt.file)
        return if (foreground != null) { // It can be null if file not found
            // Size the image
            var foreWidth = foreground.width
            var foreHeight = foreground.height
            if (foreHeight > foreWidth) {
                foreWidth = (foreHeight * HW_RATIO).toInt()
            } else {
                foreHeight = (foreWidth / HW_RATIO).toInt()
            }

            // Set up the padding
            val xPad = (foreWidth / IMG_SCALE_FACTOR).toInt()
            val yPad = (foreHeight / IMG_SCALE_FACTOR).toInt()

            // Set up an all white background for our canvas
            val background = Bitmap.createBitmap(foreWidth + xPad, foreHeight + yPad, config)
            val canvas = Canvas(background)
            canvas.drawARGB(0xFF, 0xFF, 0xFF, 0xFF) //This represents White color

            // Set up the paint
            val dither = Paint()
            dither.isDither = true
            dither.isFilterBitmap = false
            canvas.drawBitmap(
                foreground,
                (background.width - foreground.width) / 2.toFloat(),
                (background.height - foreground.height) / 2.toFloat(),
                dither
            )
            val brush = Paint()
            brush.isAntiAlias = true
            brush.typeface = Typeface.SANS_SERIF
            brush.color = Color.BLACK
            brush.style = Paint.Style.FILL
            brush.textAlign = Align.LEFT

            // Set up the number of items to draw
            var num = 5
            if (persistenceManager.preferenceManager.get(UserPreference.Receipts.IncludeTaxField)) {
                num++
            }
            if (receipt.hasExtraEditText1()) {
                num++
            }
            if (receipt.hasExtraEditText2()) {
                num++
            }
            if (receipt.hasExtraEditText3()) {
                num++
            }
            val spacing: Float = getOptimalSpacing(num, yPad / 2, brush)
            var y = spacing * 4
            canvas.drawText(trip.name, xPad / 2.toFloat(), y, brush)
            y += spacing
            canvas.drawText(
                dateFormatter.getFormattedDate(trip.startDisplayableDate) + " -- " + dateFormatter.getFormattedDate(trip.endDisplayableDate),
                xPad / 2.toFloat(),
                y,
                brush
            )
            y = background.height - yPad / 2 + spacing * 2
            canvas.drawText(
                reportResourcesManager.getFlexString(R.string.RECEIPTMENU_FIELD_NAME) + ": " + receipt.name,
                xPad / 2.toFloat(),
                y,
                brush
            )
            y += spacing
            canvas.drawText(
                reportResourcesManager.getFlexString(R.string.RECEIPTMENU_FIELD_PRICE) + ": " + receipt.price.decimalFormattedPrice + " " + receipt.price.currencyCode,
                xPad / 2.toFloat(),
                y,
                brush
            )
            y += spacing
            if (persistenceManager.preferenceManager.get(UserPreference.Receipts.IncludeTaxField)) {
                val totalTax = PriceBuilderFactory(receipt.tax).setPrice(receipt.tax.price.add(receipt.tax2.price)).build()
                canvas.drawText(
                    reportResourcesManager.getFlexString(R.string.RECEIPTMENU_FIELD_TAX) + ": " + totalTax.decimalFormattedPrice + " " + receipt.price.currencyCode,
                    xPad / 2.toFloat(),
                    y,
                    brush
                )
                y += spacing
            }
            canvas.drawText(
                reportResourcesManager.getFlexString(R.string.RECEIPTMENU_FIELD_DATE) + ": " + dateFormatter.getFormattedDate(
                    receipt.date,
                    receipt.timeZone
                ), xPad / 2.toFloat(), y, brush
            )
            y += spacing
            canvas.drawText(
                reportResourcesManager.getFlexString(R.string.RECEIPTMENU_FIELD_CATEGORY) + ": " + receipt.category.name,
                xPad / 2.toFloat(),
                y,
                brush
            )
            y += spacing
            canvas.drawText(
                reportResourcesManager.getFlexString(R.string.RECEIPTMENU_FIELD_COMMENT) + ": " + receipt.comment,
                xPad / 2.toFloat(),
                y,
                brush
            )
            y += spacing
            if (receipt.hasExtraEditText1()) {
                canvas.drawText(
                    reportResourcesManager.getFlexString(R.string.RECEIPTMENU_FIELD_EXTRA_EDITTEXT_1) + ": " + receipt.extraEditText1,
                    xPad / 2.toFloat(),
                    y,
                    brush
                )
                y += spacing
            }
            if (receipt.hasExtraEditText2()) {
                canvas.drawText(
                    reportResourcesManager.getFlexString(R.string.RECEIPTMENU_FIELD_EXTRA_EDITTEXT_2) + ": " + receipt.extraEditText2,
                    xPad / 2.toFloat(),
                    y,
                    brush
                )
                y += spacing
            }
            if (receipt.hasExtraEditText3()) {
                canvas.drawText(
                    reportResourcesManager.getFlexString(R.string.RECEIPTMENU_FIELD_EXTRA_EDITTEXT_3) + ": " + receipt.extraEditText3,
                    xPad / 2.toFloat(),
                    y,
                    brush
                )
            }

            // Clear out the dead data here
            foreground.recycle()
            foreground = null

            // And return
            background
        } else {
            null
        }
    }

    private fun getOptimalSpacing(count: Int, space: Int, brush: Paint): Float {
        var fontSize = 8f //Seed
        brush.textSize = fontSize
        while (space > (count + 2) * brush.fontSpacing) {
            brush.textSize = ++fontSize
        }
        brush.textSize = --fontSize
        return brush.fontSpacing
    }

}