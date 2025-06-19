package ke.droidcon.composeApp

import android.os.Build

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.RELEASE}"
}

actual fun getPlatform(): Platform = AndroidPlatform()