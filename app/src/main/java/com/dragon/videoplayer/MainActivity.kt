package com.dragon.videoplayer

import android.os.Bundle
import android.view.Surface
import android.view.SurfaceHolder
import androidx.appcompat.app.AppCompatActivity
import com.dragon.renderlib.player.PlayerRtp
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    var rtpPlayer: PlayerRtp? = null;
    var outputSurface: Surface? = null;
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        surfaceView3.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                outputSurface = holder?.surface;
                rtpPlayer = PlayerRtp(outputSurface!!);
            }

            override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {

            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                rtpPlayer?.release();
            }

        })
    }
}