package app.pinya.lime

import android.graphics.drawable.Drawable

class ItemApp(name: String, packageName: String, icon: Drawable) {
    private var name: String
    private var packageName: String
    private var icon: Drawable

    var home: Boolean = false
    var hidden: Boolean = false

    init {
        this.name = name
        this.packageName = packageName
        this.icon = icon
    }

    fun getPackageName(): String {
        return this.packageName
    }

    fun getName(): String {
        return this.name
    }

    fun getIcon(): Drawable {
        return this.icon
    }
}