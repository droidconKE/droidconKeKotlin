package ke.droidcon.composeApp

class Greeting {
    private val platform = getPlatform()

    fun greet(): String {
        return "Hello DroidconKe World, from : ${platform.name}!"
    }
}