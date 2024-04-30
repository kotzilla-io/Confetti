package dev.johnoreilly.confetti

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.work.WorkManager
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.crashlytics.setCustomKeys
import com.google.firebase.ktx.Firebase
import dev.johnoreilly.confetti.di.appModule
import dev.johnoreilly.confetti.di.initKoin
import dev.johnoreilly.confetti.work.SessionNotificationWorker
import dev.johnoreilly.confetti.work.setupDailyRefresh
import io.kotzilla.cloudinject.CloudInjectSDK
import io.kotzilla.cloudinject.analytics.koin.analyticsLogger
import io.kotzilla.cloudinject.dev.dev
import io.kotzilla.cloudinject.dev.logs
import io.kotzilla.cloudinject.dev.prod
import io.kotzilla.cloudinject.dev.refreshRate
import io.kotzilla.cloudinject.dev.staging
import kotlinx.coroutines.launch
import org.koin.android.ext.android.get
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.core.time.measureDuration

class ConfettiApplication : Application() {

    private val isFirebaseInstalled
        get() = try {
            FirebaseApp.getInstance()
            true
        } catch (ise: IllegalStateException) {
            false
        }

    override fun onCreate() {
        super.onCreate()

        if (isFirebaseInstalled) {
            if (!BuildConfig.DEBUG) {
                Firebase.crashlytics.setCrashlyticsCollectionEnabled(true)
                Firebase.crashlytics.setCustomKeys {
                    key("appName", "androidApp")
                }
            } else {
                Firebase.crashlytics.setCrashlyticsCollectionEnabled(false)
            }
        }

        //        CloudInjectSDK.setup(this@ConfettiApplication)
        val isProd = true
        val ciStart = measureDuration {
            CloudInjectSDK.dev(this@ConfettiApplication)
            {
                // dev("192.168.1.141")
                if (isProd){
                    prod()
                } else {
                    staging()
                }
                refreshRate(15_000)
                logs()
            }
        }
        CloudInjectSDK.log("Cloud-Inject start - $ciStart ms")
        Toast.makeText(this,"Cloud-Inject - "+if(isProd) "PROD" else "STAGING", Toast.LENGTH_LONG).show()

        val koinStart = measureDuration {
            initKoin {
                analyticsLogger()
//            androidLogger()
                androidContext(this@ConfettiApplication)
                modules(appModule)

                workManagerFactory()
            }
        }
        CloudInjectSDK.log("Koin start - $koinStart ms")
        CloudInjectSDK.setProperties(
            "cloud-inject-version" to "0.9.4",
            "cloud-inject-start" to "$ciStart",
            "koin-start" to "$koinStart"
        )

        val workManager = get<WorkManager>()
        setupDailyRefresh(workManager)

        ProcessLifecycleOwner.get().lifecycleScope.launch {
            get<AppSettings>().experimentalFeaturesEnabledFlow.collect { isEnabled ->
                if (isEnabled) {
                    SessionNotificationWorker.startPeriodicWorkRequest(workManager)
                } else {
                    SessionNotificationWorker.cancelWorkRequest(workManager)
                }
            }
        }
    }
}
