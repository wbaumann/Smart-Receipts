package co.smartreceipts.android.imports.importer

import android.os.Parcelable
import co.smartreceipts.android.ocr.apis.model.OcrResponse
import com.hadisatrio.optional.Optional
import kotlinx.android.parcel.Parcelize
import java.io.File

@Parcelize
data class ActivityFileResultImporterResponse(
    /**
     * Throwable if the importer produced an error
     */
    val throwable: Optional<Throwable>,

    /**
     * The resultant file that was imported
     */
    val file: File?,

    /**
     * The OCR scan response
     */
    val ocrResponse: OcrResponse?,

    /**
     * The request code that triggered the import
     */
    val requestCode: Int,

    /**
     * The result code that from the response
     */
    val resultCode: Int
) : Parcelable {

    companion object {

        @JvmStatic
        fun importerError(throwable: Throwable): ActivityFileResultImporterResponse =
            ActivityFileResultImporterResponse(Optional.of(throwable), null, null, 0, 0)

        @JvmStatic
        fun importerResponse(file: File, ocrResponse: OcrResponse, requestCode: Int, resultCode: Int): ActivityFileResultImporterResponse =
            ActivityFileResultImporterResponse(Optional.absent(), file, ocrResponse, requestCode, resultCode)

    }
}
