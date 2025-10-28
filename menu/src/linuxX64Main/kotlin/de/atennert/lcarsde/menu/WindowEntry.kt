package de.atennert.lcarsde.menu

data class WindowEntry(val id: String, var name: String, var isActive: Boolean) {
    override fun equals(other: Any?): Boolean {
        if (other !is WindowEntry) {
            return false
        }
        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}