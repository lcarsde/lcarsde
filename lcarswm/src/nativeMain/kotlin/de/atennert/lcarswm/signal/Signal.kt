package de.atennert.lcarswm.signal

import platform.posix.*

/**
 * Signals that we handle
 */
enum class Signal(val signalValue: Int) {
    ABRT(SIGABRT),
    SEGV(SIGSEGV),
    FPE(SIGFPE),
    ILL(SIGILL),
    QUIT(SIGQUIT),
    TRAP(SIGTRAP),
    SYS(SIGSYS),
    BUS(SIGBUS),
    XCPU(SIGXCPU),
    XFSZ(SIGXFSZ),
    USR1(SIGUSR1),
    USR2(SIGUSR2),
    TERM(SIGTERM),
    INT(SIGINT),
    HUP(SIGHUP),
    PIPE(SIGPIPE),
    CHLD(SIGCHLD),
    TTIN(SIGTTIN),
    TTOU(SIGTTOU);

    companion object {
        /**
         * Signals that trigger us to core-dump
         */
        val CORE_DUMP_SIGNALS = setOf(ABRT, SEGV, FPE, ILL, QUIT, TRAP, SYS, BUS, XCPU, XFSZ)
    }
}