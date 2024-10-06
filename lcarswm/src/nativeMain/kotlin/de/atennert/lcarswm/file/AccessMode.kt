package de.atennert.lcarswm.file

val AccessMode.posixCode: String
    get() = when(this) {
        AccessMode.READ -> "r"
        AccessMode.WRITE -> "w"
        AccessMode.APPEND -> "a"
        AccessMode.READ_WRITE -> "w+"
        AccessMode.READ_APPEND -> "a+"
    }