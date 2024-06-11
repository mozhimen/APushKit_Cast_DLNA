package com.android.cast.dlna.dmr.bases

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import androidx.appcompat.app.AppCompatActivity
import com.android.cast.dlna.dmr.DLNARendererService
import com.android.cast.dlna.dmr.commons.IRenderServiceBinder
import com.android.cast.dlna.dmr.commons.IVideoRenderControl
import com.android.cast.dlna.dmr.cons.CDlnaDmrParams

abstract class BaseRendererActivity : AppCompatActivity() {
    protected var dlnaRendererService: DLNARendererService? = null
        private set

    protected val videoAction: IVideoRenderControl.VideoAction?
        get() = intent.getParcelableExtra(CDlnaDmrParams.Extra_CastAction)

    ///////////////////////////////////////////////////////////////////////////////////////////

    open fun onServiceConnected() {}

    ///////////////////////////////////////////////////////////////////////////////////////////

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!videoAction?.stop.isNullOrBlank()) {
            finish()
            return
        }
        bindService(Intent(this, DLNARendererService::class.java), serviceConnection, Context.BIND_AUTO_CREATE)
    }

    override fun onNewIntent(newIntent: Intent) {
        super.onNewIntent(newIntent)
        intent = newIntent
        if (!videoAction?.stop.isNullOrBlank()) {
            finish()
        }
    }

    override fun onDestroy() {
        unbindService(serviceConnection)
        dlnaRendererService?.bindVideoControl(null)
        super.onDestroy()
    }

    ///////////////////////////////////////////////////////////////////////////////////////////

    private val serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            dlnaRendererService = (service as IRenderServiceBinder).service
            onServiceConnected()
        }

        override fun onServiceDisconnected(name: ComponentName) {
            dlnaRendererService = null
        }
    }
}