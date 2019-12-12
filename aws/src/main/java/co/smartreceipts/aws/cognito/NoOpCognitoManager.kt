package co.smartreceipts.aws.cognito

import co.smartreceipts.core.di.scopes.ApplicationScope
import com.amazonaws.auth.CognitoCachingCredentialsProvider
import com.hadisatrio.optional.Optional
import io.reactivex.Observable
import javax.inject.Inject

@ApplicationScope
class NoOpCognitoManager @Inject constructor() : CognitoManager {
    override fun initialize() {}

    override fun getCognitoCachingCredentialsProvider(): Observable<Optional<CognitoCachingCredentialsProvider>> =
        Observable.just(Optional.absent())

}