package co.smartreceipts.aws.cognito

import com.amazonaws.auth.CognitoCachingCredentialsProvider
import com.hadisatrio.optional.Optional
import io.reactivex.Observable

interface CognitoManager {

    fun initialize()

    fun getCognitoCachingCredentialsProvider(): Observable<Optional<CognitoCachingCredentialsProvider>>
}