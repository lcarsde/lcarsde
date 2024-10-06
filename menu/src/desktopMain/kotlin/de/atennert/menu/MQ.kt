package de.atennert.appSelector

import com.sun.jna.IntegerType
import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.Structure
import com.sun.jna.platform.win32.BaseTSD.SIZE_T
import com.sun.jna.platform.win32.BaseTSD.SSIZE_T
import com.sun.jna.ptr.PointerByReference

class Uint32_t @JvmOverloads constructor(private val value: Int = 0) :
    IntegerType(4, value.toLong(), true) {
    override fun toByte() = value.toByte()

    override fun toShort() = value.toShort()
}

@Structure.FieldOrder("mq_flags", "mq_maxmsg", "mq_msgsize", "mq_curmsgs")
class MqAttributes : Structure() {
    @JvmField
    var mq_flags: Long = 0
    @JvmField
    var mq_maxmsg: Long = 0
    @JvmField
    var mq_msgsize: Long = 0
    @JvmField
    var mq_curmsgs: Long = 0
}

interface MessageQueue : Library {
    fun mq_open(__name: String?, __oflag: Int, permission: Uint32_t, attributes: MqAttributes): Int
    fun mq_open(__name: String?, __oflag: Int): Int

    fun mq_send(__mqdes: Int, __msg_ptr: String, __msg_len: SIZE_T, __msg_prio: Uint32_t): Int

    fun mq_receive(__mqdes: Int, __msg_ptr: PointerByReference, __msg_len: SIZE_T, __msg_prio: Uint32_t?): SSIZE_T

    fun mq_close(__mqdes: Int): Int

    fun mq_unlink(__name: String): Int

    companion object {
        val INSTANCE: MessageQueue = Native.load("rt", MessageQueue::class.java)

        const val QUEUE_PERMISSIONS = 432 // 0660

        const val O_NONBLOCK = 0x800
        const val O_CREAT = 0x40
    }
}

/**
 * Simple Linux message queue implementation.
 */
class MQ(private val name: String, private val mode: Mode, private val isManaging: Boolean = false) {
    enum class Mode(var flag: Int) {
        READ(0),
        WRITE(1),
        READ_WRITE(2)
    }

    private val mq = MessageQueue.INSTANCE
    private val mqDescriptor: Int

    init {
        if (isManaging) {
            val oFlags = mode.flag or MessageQueue.O_NONBLOCK or MessageQueue.O_CREAT
            val attributes = MqAttributes()
            attributes.mq_flags = MessageQueue.O_NONBLOCK.toLong()
            attributes.mq_maxmsg = MAX_MESSAGE_COUNT
            attributes.mq_msgsize = MAX_MESSAGE_SIZE
            attributes.mq_curmsgs = 0

            mqDescriptor = mq.mq_open(name, oFlags, Uint32_t(MessageQueue.QUEUE_PERMISSIONS), attributes)
        } else {
            mqDescriptor = mq.mq_open(name, mode.flag)
        }
    }

    fun send(message: String) {
        check(mode != Mode.READ) { "Can not use MQ send in read mode" }
        mq.mq_send(mqDescriptor, message, SIZE_T(message.length.toLong()), Uint32_t(0))
    }

    fun receive(): String {
        check(mode != Mode.WRITE) { "Can not use MQ receive in write mode" }
        val pref = PointerByReference()
        val msgSize = mq.mq_receive(mqDescriptor, pref, SIZE_T(MESSAGE_BUFFER_SIZE), null)
        return pref.pointer.getByteArray(0, msgSize.toInt()).decodeToString()
    }

    fun close() {
        if (mq.mq_close(mqDescriptor) == -1) {
            return
        }
        if (isManaging) {
            mq.mq_unlink(name)
        }
    }

    private companion object {
        const val MAX_MESSAGE_SIZE = 1024L
        const val MAX_MESSAGE_COUNT = 10L
        const val MESSAGE_BUFFER_SIZE = MAX_MESSAGE_SIZE + 10
    }
}