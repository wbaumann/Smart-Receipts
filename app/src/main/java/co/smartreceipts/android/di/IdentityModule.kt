package co.smartreceipts.android.di

import co.smartreceipts.android.identity.IdentityManagerImpl
import co.smartreceipts.core.di.scopes.ApplicationScope
import co.smartreceipts.core.identity.IdentityManager
import dagger.Module
import dagger.Provides

@Module
class IdentityModule {

    @Provides
    @ApplicationScope
    fun provideIdentityManager(identityManager: IdentityManagerImpl) : IdentityManager {
        return identityManager
    }
}