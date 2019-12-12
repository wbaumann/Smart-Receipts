package co.smartreceipts.android.di;

import java.util.Arrays;

import co.smartreceipts.android.ExtraInitializer;
import co.smartreceipts.android.ExtraInitializerFlossImpl;
import co.smartreceipts.android.analytics.Analytics;
import co.smartreceipts.android.analytics.AnalyticsManager;
import co.smartreceipts.android.analytics.impl.logger.AnalyticsLogger;
import co.smartreceipts.aws.cognito.CognitoManager;
import co.smartreceipts.aws.cognito.NoOpCognitoManager;
import co.smartreceipts.core.di.scopes.ApplicationScope;
import co.smartreceipts.android.ocr.NoOpOcrManager;
import co.smartreceipts.android.ocr.OcrManager;
import co.smartreceipts.android.purchases.wallet.DefaultPurchaseWallet;
import co.smartreceipts.android.purchases.wallet.PurchaseWallet;
import co.smartreceipts.android.settings.UserPreferenceManager;
import dagger.Module;
import dagger.Provides;

@Module
public class FlavorModule {

    @Provides
    @ApplicationScope
    public static PurchaseWallet providePurchaseWallet(DefaultPurchaseWallet defaultPurchaseWallet) {
        return defaultPurchaseWallet;
    }

    @Provides
    @ApplicationScope
    public static ExtraInitializer provideExtraInitializer(ExtraInitializerFlossImpl flossInitializer) {
        return flossInitializer;
    }

    @Provides
    @ApplicationScope
    public static Analytics provideAnalytics(UserPreferenceManager userPreferenceManager) {
        return new AnalyticsManager(Arrays.asList(new AnalyticsLogger()), userPreferenceManager);
    }

    @Provides
    @ApplicationScope
    public static OcrManager provideOcrManager(NoOpOcrManager ocrManager) {
        return ocrManager;
    }

    @Provides
    @ApplicationScope
    public static CognitoManager provideCognitoManager(NoOpCognitoManager cognitoManager) {
        return cognitoManager;
    }
}
