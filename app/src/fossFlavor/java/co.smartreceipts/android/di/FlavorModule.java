package co.smartreceipts.android.di;

import java.util.Arrays;

import co.smartreceipts.android.ExtraInitializer;
import co.smartreceipts.android.ExtraInitializerPlusImpl;
import co.smartreceipts.core.analytics.Analytics;
import co.smartreceipts.android.analytics.AnalyticsManager;
import co.smartreceipts.android.analytics.impl.firebase.FirebaseAnalytics;
import co.smartreceipts.core.analytics.impl.logger.AnalyticsLogger;
import co.smartreceipts.core.di.scopes.ApplicationScope;
import co.smartreceipts.android.purchases.wallet.PlusPurchaseWallet;
import co.smartreceipts.android.purchases.wallet.PurchaseWallet;
import co.smartreceipts.core.settings.UserPreferenceManager;
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
    public static ExtraInitializer provideExtraInitializer(ExtraInitializerPlusImpl plusInitializer) {
        return plusInitializer;
    }

    @Provides
    @ApplicationScope
    public static Analytics provideAnalytics(UserPreferenceManager userPreferenceManager) {
        return new AnalyticsManager(Arrays.asList(new AnalyticsLogger()), userPreferenceManager);
    }
}
