package com.dragon.renderlib.player

import android.media.*
import android.util.Log
import android.view.Surface
import com.dragon.renderlib.codec.SurfaceDecodeCodec
import com.dragon.renderlib.net.NaluData
import com.dragon.rtplib.RtpWrapper
import java.util.concurrent.ArrayBlockingQueue

class PlayerRtp(val outputSurface: Surface) {
    val videoRtpWrapper = RtpWrapper();

    var videoDecodeCodec: SurfaceDecodeCodec;

    val videoBufferQueue = ArrayBlockingQueue<ByteArray>(10);
    val videoBufferSizeQueue = ArrayBlockingQueue<Int>(10);

    val nalu = NaluData()

    init {
        val videoFormat = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, 480, 800)
        videoFormat.setInteger(MediaFormat.KEY_BIT_RATE, 750000)
        videoFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 30)
        videoFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
        videoFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 5)
        var currentTime = 0L
        videoDecodeCodec = object : SurfaceDecodeCodec(videoFormat, outputSurface) {
            override fun onInputBufferAvailable(codec: MediaCodec, index: Int) {
                val buffer = codec.getInputBuffer(index) ?: return;
                val data = videoBufferQueue.take()
                val size = videoBufferSizeQueue.take()
                val time = (System.currentTimeMillis() - currentTime) * 1000
                buffer.position(0)
                buffer.put(data, 0, size)
                codec.queueInputBuffer(index, 0, size, time, 0)
            }

            override fun onOutputFormatChanged(codec: MediaCodec, format: MediaFormat) {

            }
        }

        videoRtpWrapper.open(40018, 96, 90000);
        videoRtpWrapper.setCallback { data, len ->
            Log.d("dragon_video", "received video data $len")
            nalu.appended(data, len) { buffer, offset, size ->
                videoBufferQueue.put(buffer);
                videoBufferSizeQueue.put(size);
            }
        }
    }

    fun release() {
        videoDecodeCodec.release {
            videoRtpWrapper.close();
        }
    }
}