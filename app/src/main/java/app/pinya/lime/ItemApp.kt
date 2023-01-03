package app.pinya.lime

import android.graphics.drawable.Drawable

class ItemApp(name: String, packageName: String, icon: Drawable) {
    var originalName: String
    var packageName: String
    var icon: Drawable

    var name: String
    var home: Boolean = false
    var hidden: Boolean = false
    var system: Boolean = false
    var homeOrderIndex: Int = 0

    init {
        this.originalName = name
        this.name = name
        this.packageName = packageName
        this.icon = icon
    }
}