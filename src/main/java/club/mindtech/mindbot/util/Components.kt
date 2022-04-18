package club.mindtech.mindbot.util

import club.mindtech.mindbot.events.ComponentCallback
import club.mindtech.mindbot.events.awaitEvent
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu
import net.dv8tion.jda.api.interactions.components.selections.SelectOption
import java.util.*

private val id get() = UUID.randomUUID().toString()

fun button(style: ButtonStyle, label: String, callback: ComponentCallback): Button {
    awaitEvent(id, callback)
    return Button.of(style, id, label)
}

fun menu(options: List<SelectOption>, callback: ComponentCallback): SelectMenu {
    awaitEvent(id, callback)
    return SelectMenu
        .create(id)
        .addOptions(options)
        .build()
}
