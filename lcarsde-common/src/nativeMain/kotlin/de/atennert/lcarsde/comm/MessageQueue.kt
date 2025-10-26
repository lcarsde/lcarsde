package de.atennert.lcarsde.comm

import de.atennert.lcarsde.lifecycle.closeWith
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.convert
import kotlinx.cinterop.nativeHeap
import kotlinx.cinterop.usePinned
import platform.linux.mq_attr
import platform.linux.mq_close
import platform.linux.mq_open
import platform.linux.mq_receive
import platform.linux.mq_send
import platform.linux.mq_unlink
import platform.linux.mqd_t
import platform.posix.O_CREAT
import platform.posix.O_NONBLOCK
import platform.posix.O_RDONLY
import platform.posix.O_RDWR
import platform.posix.O_WRONLY
import platform.posix.mode_t
import kotlin.experimental.ExperimentalNativeApi

/**
 * Adapter class for Posix message queue. The message queue will be created and destroyed
 * by this implementation. Other end points must connect later to the queue and disconnect
 * before it is destroyed.
 *
 * @param name The name of the message queue, unique identifier for each queue
 * @param mode The usage mode for this queue in this app: READ, WRITE or READ_WRITE
 */
@ExperimentalForeignApi
class MessageQueue(private val name: String, private val mode: Mode, private val isManaging: Boolean = false) {
    enum class Mode (val flag: Int) {
        READ(O_RDONLY),
        WRITE(O_WRONLY),
        READ_WRITE(O_RDWR)
    }

    private val mqDes: mqd_t

    init {
        if (isManaging) {
            val oFlags = mode.flag or O_NONBLOCK or O_CREAT
            val mqAttributes = nativeHeap.alloc<mq_attr>()
            mqAttributes.mq_flags = O_NONBLOCK.convert()
            mqAttributes.mq_maxmsg = MAX_MESSAGE_COUNT
            mqAttributes.mq_msgsize = MAX_MESSAGE_SIZE.convert()
            mqAttributes.mq_curmsgs = 0

            mqDes = mq_open(name, oFlags, QUEUE_PERMISSIONS, mqAttributes)
        } else {
            mqDes = mq_open(name, mode.flag)
        }

        closeWith(MessageQueue::close)
    }

    @ExperimentalNativeApi
    fun send(message: String) {
        check(mode != Mode.READ) { "Can not use MQ send in read mode" }

        mq_send(mqDes, message, message.length.convert(), 0.convert())
    }

    @ExperimentalNativeApi
    fun receive(): String? {
        check(mode != Mode.WRITE) { "Can not use MQ receive in write mode" }

        val msgBuffer = ByteArray(MESSAGE_BUFFER_SIZE)
        var msgSize: Long = -1
        msgBuffer.usePinned {
            msgSize = mq_receive(mqDes, it.addressOf(0), MESSAGE_BUFFER_SIZE.convert(), null)
        }
        if (msgSize > 0) {
            return msgBuffer.decodeToString(0, msgSize.convert())
        }
        return null
    }

    private fun close() {
        if (mq_close(mqDes) == -1) {
            return
        }

        if (isManaging) {
            mq_unlink(name)
        }
    }

    companion object {
        private val QUEUE_PERMISSIONS: mode_t = 432.convert() // 0660
        private const val MAX_MESSAGE_SIZE = 1024
        private const val MAX_MESSAGE_COUNT = 10L
        private const val MESSAGE_BUFFER_SIZE = MAX_MESSAGE_SIZE + 10
    }
}