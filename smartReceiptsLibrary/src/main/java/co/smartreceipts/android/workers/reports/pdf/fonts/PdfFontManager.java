package co.smartreceipts.android.workers.reports.pdf.fonts;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.common.base.Preconditions;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.pdmodel.font.PDFont;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class PdfFontManager {

    private final PdfFontLoader fontLoader;
    private final Map<PdfFontStyle, PdfFontSpec> fontMap = new HashMap<>();

    private boolean isInitialized = false;

    public PdfFontManager(@NonNull Context context, @NonNull PDDocument document) {
        this(new LocalAssetPdfFontLoader(context, document));
    }

    public PdfFontManager(@NonNull PdfFontLoader fontLoader) {
        this.fontLoader = Preconditions.checkNotNull(fontLoader);
    }

    public void initialize() throws IOException {
        // Note: We'll want to refactor how these things are defined at some point to allow for font downloads (eg Asian char sets)
        final PDFont defaultFont = fontLoader.load("NotoSerif-Regular.ttf");
        final PDFont boldFont = fontLoader.load("NotoSerif-Bold.ttf");

        final int titleSize = 14;
        final int defaultSize = 12;
        final int smallSize = 10;

        fontMap.put(PdfFontStyle.Title, new PdfFontSpec(boldFont, titleSize));
        fontMap.put(PdfFontStyle.Default, new PdfFontSpec(defaultFont, defaultSize));
        fontMap.put(PdfFontStyle.TableHeader, new PdfFontSpec(boldFont, defaultSize));
        fontMap.put(PdfFontStyle.Small, new PdfFontSpec(defaultFont, smallSize));

        isInitialized = true;
    }

    @NonNull
    public PdfFontSpec getFont(@NonNull PdfFontStyle style) {
        Preconditions.checkArgument(isInitialized, "You must initialize this class before getting a font");
        return Preconditions.checkNotNull(fontMap.get(style));
    }
}
