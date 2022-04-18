package club.mindtech.mindbot.util

import java.lang.NullPointerException

fun <T> notNull(obj: T?): T {
    if (obj == null) {
        val caller = findCaller()
        throw NullPointerException("Object is null at $caller")
    }
    return obj
}

fun findCaller(): String {
    return StackWalker.getInstance().walk {
            frame -> frame.skip(2).findFirst().map {
                "${it.className}::${it.methodName}#${it.lineNumber}"
            }.orElse("Unknown")
        }
}
