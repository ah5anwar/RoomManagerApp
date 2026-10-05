package com.creativesit.roommanager.ui.dashboard

data class Section(
    val id: String,
    val label: String,
    val icon: String,
    val adminOnly: Boolean = false,   // শুধু এডমিন দেখবে (ম্যানেজার/গ্রাহক না)
    val permissionKey: String? = null // গ্রাহকের ক্ষেত্রে এই পারমিশন থাকলেই দেখাবে
)

object Sections {
    val OVERVIEW = Section("overview", "হোম", "🏠")
    val USERS = Section("users", "ইউজার", "👥")
    val RENT = Section("rent", "ভাড়া", "৳")
    val MARKET = Section("market", "বাজার", "🛒", permissionKey = "market")
    val COOKING = Section("cooking", "রান্না", "🍳", permissionKey = "cooking")
    val TRASH = Section("trash", "ময়লা", "🗑️", permissionKey = "trash")
    val CLEANING = Section("cleaning", "ক্লিন", "🧹", permissionKey = "cleaning")
    val NOTIFICATIONS = Section("notifications", "নোটিফিকেশন", "🔔")
    val NOTIFY = Section("notify", "মেসেজ", "🔔")
    val CHAT = Section("chat", "চ্যাট", "💬", permissionKey = "chat")
    val SETTINGS = Section("settings", "সেটিংস", "⚙️", adminOnly = true)
    val ABOUT = Section("about", "About", "ℹ️")
    val DEVELOPER = Section("developer", "ডেভেলপার তথ্য", "👨‍💻")

    val DUTY_SECTIONS = listOf(MARKET, COOKING, TRASH, CLEANING)

    fun forRole(role: String, permissions: List<String>): List<Section> {
        val all = listOf(OVERVIEW, USERS, RENT, MARKET, COOKING, TRASH, CLEANING, NOTIFY, NOTIFICATIONS, CHAT, SETTINGS, ABOUT, DEVELOPER)
        return when (role) {
            "admin" -> all.filter { it.id != "notifications" } // এডমিনের নিজস্ব 'মেসেজ' ট্যাব আছে, এটা লাগবে না
            // ম্যানেজার এডমিনের প্রায় সব পারমিশন পায়, শুধু "সেটিংস" ও গ্রাহকের জন্য নির্দিষ্ট "নোটিফিকেশন" ছাড়া
            "manager" -> all.filter { !it.adminOnly && it.id != "notifications" }
            else -> all.filter { !it.adminOnly && it.id != "notify" && (it.permissionKey == null || permissions.contains(it.permissionKey)) }
        }
    }
}
