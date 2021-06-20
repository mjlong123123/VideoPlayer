package com.dragon.renderlib.net

import java.nio.ByteBuffer
import kotlin.experimental.and
import kotlin.experimental.or

class NaluData {

    private val F_MASK = 0b10000000.toByte()
    private val NRI_MASK = 0b01100000.toByte()
    private val TYPE_MASK = 0b00011111.toByte()
    private val START_MASK = 0b10000000.toByte()
    private val END_MASK = 0b01000000.toByte()
    private val RESERVE_MASK = 0b00100000.toByte()
    private val maxFragmentSize = 65535 - 1000
    private val data = ByteArray(1000 * 100)
    private var position = 0;

    fun appended(buffer: ByteArray, len: Int, sender: (ByteArray, Int, Int) -> Unit) {
        val type = buffer[0] and TYPE_MASK
        if (type == 0b11100.toByte()) {
            val isStartFlag = (buffer[1] and START_MASK) == START_MASK
            val isEndFlag = (buffer[1] and END_MASK) == END_MASK
            when {
                isStartFlag -> {
                    data[0] = 0
                    data[1] = 0
                    data[2] = 0
                    data[3] = 1
                    position = 4
                    data[position] = (buffer[0] and (F_MASK or NRI_MASK)) or (buffer[1] and TYPE_MASK)
                    position++
                    System.arraycopy(buffer, 2, data, position, len - 2)
                    position += (len - 2)
                }
                isEndFlag -> {
                    System.arraycopy(buffer, 2, data, position, len - 2)
                    position += (len - 2)
                    sender.invoke(data, 0, position)
                }
                else -> {
                    System.arraycopy(buffer, 2, data, position, len - 2)
                    position += (len - 2)
                }
            }
        } else {
            data[0] = 0
            data[1] = 0
            data[2] = 0
            data[3] = 1
            position = 4
            System.arraycopy(buffer, 0, data, position, len)
            position += len
            sender.invoke(data, 0, position)
        }
    }

    fun split2FU(byteBuffer: ByteBuffer, offset: Int, size: Int, sender: (ByteArray, Int, Int, Boolean, Boolean) -> Unit) {
        if (size < maxFragmentSize) {
            byteBuffer.position(offset + 4)//skip 0001
            byteBuffer.get(data, 0, size - 4)
            sender.invoke(data, 0, size - 4, true, true)
            return
        }
        val naluHeader = byteBuffer.get(4)//read nalu header
        byteBuffer.position(offset + 5)//skip 0001 and nalu header
        var leftSize = size - 5
        var started = false
        var fuIndicator: Byte
        var fuHeader: Byte
        while (leftSize > 0) {
            val readSize = if (leftSize > maxFragmentSize) maxFragmentSize else leftSize;
            byteBuffer.get(data, 2, readSize)
            leftSize -= readSize
            fuIndicator = naluHeader and 0b11100000.toByte() or 0b00011100.toByte()
            fuHeader = when {
                !started -> {
                    0b10000000.toByte()
                }
                leftSize <= 0 -> {
                    0b01000000.toByte()
                }
                else -> {
                    0b00000000.toByte()
                }
            }
            data[0] = fuIndicator
            data[1] = fuHeader or (naluHeader and 0b00011111.toByte())
            sender.invoke(data, 0, readSize + 2, leftSize <= 0, !started)
            started = true
        }
    }
}