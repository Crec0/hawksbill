package club.mindtech.mindbot.helpers.image

import java.awt.Color
import java.awt.Graphics2D
import java.awt.image.BufferedImage

fun image(width: Int, height: Int, components: Graphics2D.() -> Unit): BufferedImage {
    val img = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
    val g2d = img.createGraphics()
    g2d.apply(components)
    g2d.dispose()
    return img
}

fun Graphics2D.rect(
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    color: Int = 0xFFFFFF,
    fill: Boolean = false
) {
    this.color = Color(color)
    if (fill) {
        this.fillRect(x, y, width, height)
    } else {
        this.drawRect(x, y, width, height)
    }
}

fun Graphics2D.text(x: Int, y: Int, text: String) {
    this.drawString(text, x, y)
}

fun Graphics2D.ellipse(x: Int, y: Int, width: Int, height: Int) {
    this.drawOval(x, y, width, height)
}
