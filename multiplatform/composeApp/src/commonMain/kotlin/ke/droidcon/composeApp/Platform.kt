package ke.droidcon.composeApp

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform