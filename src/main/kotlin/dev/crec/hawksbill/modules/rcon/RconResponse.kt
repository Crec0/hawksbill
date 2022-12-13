package dev.crec.hawksbill.modules.rcon

enum class RconResponse(val value: Int) {
    UNKNOWN(-1),
    COMMAND_RESPONSE(0),
    COMMAND(2),
    LOGIN_SUCCESS(2),
    LOGIN(3)
}
