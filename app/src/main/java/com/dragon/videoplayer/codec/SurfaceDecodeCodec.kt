package com.dragon.renderlib.codec

import android.media.MediaCodec
import android.media.MediaCodecList
import android.media.MediaFormat
import android.view.Surface

/**
 * @author dragon
 */
abstract class SurfaceDecodeCodec(mediaFormat: MediaFormat, val outputSurface: Surface) : BaseCodec("SurfaceDecodeCodec", mediaFormat) {
    override fun onCreateMediaCodec(mediaFormat: MediaFormat): MediaCodec {
        val mediaCodecList = MediaCodecList(MediaCodecList.ALL_CODECS)
        val mediaCodecName = mediaCodecList.findDecoderForFormat(mediaFormat)
        return MediaCodec.createByCodecName(mediaCodecName)
    }

    override fun onConfigMediaCodec(mediaCodec: MediaCodec) {
        mediaCodec.configure(mediaFormat, outputSurface, null, 0)
    }

    override fun onOutputBufferAvailable(codec: MediaCodec, index: Int, info: MediaCodec.BufferInfo) {
        codec.releaseOutputBuffer(index,true);
    }
}