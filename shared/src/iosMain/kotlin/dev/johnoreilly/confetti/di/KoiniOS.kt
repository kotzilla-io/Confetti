@file:OptIn(ExperimentalSettingsApi::class)

package dev.johnoreilly.confetti.di

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.cache.normalized.FetchPolicy
import com.apollographql.apollo3.cache.normalized.api.NormalizedCacheFactory
import com.apollographql.apollo3.cache.normalized.sql.SqlNormalizedCacheFactory
import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.NSUserDefaultsSettings
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.coroutines.toFlowSettings
import dev.johnoreilly.confetti.auth.Authentication
import dev.johnoreilly.confetti.auth.DefaultAuthentication
import dev.johnoreilly.confetti.utils.DateService
import dev.johnoreilly.confetti.utils.IosDateService
import io.kotzilla.cloudinject.CloudInjectCoreSDK
import io.kotzilla.cloudinject.config.Environment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import platform.Foundation.NSUserDefaults

fun initKoinIOS() = initKoin() {

    CloudInjectCoreSDK.setupAndConnect(
        appKey = "dev.johnoreilly.confetti",
        versionName = "1.0-iOS",
        environment = Environment.Staging,
//            environment = Environment.Dev("192.168.1.76")
    ).getCurrentService().config.apply {
        useDebugLogs = true
        refreshRate = 15_000
    }
}

actual fun platformModule() = module {
    single<Authentication> { Authentication.Disabled }
    single<ObservableSettings> { NSUserDefaultsSettings(NSUserDefaults.standardUserDefaults) }
    single { get<ObservableSettings>().toFlowSettings() }
    single<NormalizedCacheFactory> { SqlNormalizedCacheFactory("confetti.db") }
    singleOf(::IosDateService) { bind<DateService>() }
    single<FetchPolicy> { FetchPolicy.CacheAndNetwork }
    factory {
        ApolloClient.Builder()
            .serverUrl("https://confetti-app.dev/graphql")
    }
}

actual fun getDatabaseName(conference: String, uid: String?) = "$conference$uid.db"
