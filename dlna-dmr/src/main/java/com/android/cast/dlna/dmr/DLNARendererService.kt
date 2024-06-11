package com.android.cast.dlna.dmr

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import com.android.cast.dlna.core.Utils
import com.android.cast.dlna.core.getLogger
import com.android.cast.dlna.dmr.commons.IAudioRenderControl
import com.android.cast.dlna.dmr.commons.IRenderServiceBinder
import com.android.cast.dlna.dmr.commons.IVideoRenderControl
import com.android.cast.dlna.dmr.impls.VideoRenderControlImpl
import com.android.cast.dlna.dmr.impls.VideoRenderServiceImpl
import com.android.cast.dlna.dmr.impls.AudioRenderControlImpl
import com.android.cast.dlna.dmr.impls.AudioRenderServiceImpl
import org.fourthline.cling.UpnpServiceConfiguration
import org.fourthline.cling.android.AndroidUpnpServiceConfiguration
import org.fourthline.cling.android.AndroidUpnpServiceImpl
import org.fourthline.cling.binding.annotations.AnnotationLocalServiceBinder
import org.fourthline.cling.model.ValidationException
import org.fourthline.cling.model.meta.DeviceDetails
import org.fourthline.cling.model.meta.DeviceIdentity
import org.fourthline.cling.model.meta.LocalDevice
import org.fourthline.cling.model.meta.LocalService
import org.fourthline.cling.model.meta.ManufacturerDetails
import org.fourthline.cling.model.meta.ModelDetails
import org.fourthline.cling.model.types.UDADeviceType
import org.fourthline.cling.model.types.UDAServiceId
import org.fourthline.cling.model.types.UDN
import org.fourthline.cling.support.avtransport.lastchange.AVTransportLastChangeParser
import org.fourthline.cling.support.avtransport.lastchange.AVTransportVariable
import org.fourthline.cling.support.lastchange.EventedValue
import org.fourthline.cling.support.lastchange.LastChangeAwareServiceManager
import org.fourthline.cling.support.model.Channel
import org.fourthline.cling.support.renderingcontrol.lastchange.ChannelVolume
import org.fourthline.cling.support.renderingcontrol.lastchange.RenderingControlLastChangeParser
import org.fourthline.cling.support.renderingcontrol.lastchange.RenderingControlVariable.Volume
import java.io.IOException
import java.util.UUID

open class DLNARendererService : AndroidUpnpServiceImpl() {
    companion object {
        fun startService(context: Context) {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                context.applicationContext.startForegroundService(Intent(context, DLNARendererService::class.java))
//            } else {
            context.applicationContext.startService(Intent(context, DLNARendererService::class.java))
//            }
        }
    }

    ///////////////////////////////////////////////////////////////

    private val logger = getLogger("RendererService>>>>>")
    private val serviceBinder = DLNARendererServiceBinder()
    private var localDevice: LocalDevice? = null
    private lateinit var videoRenderControl: IVideoRenderControl
    private lateinit var audioRenderControl: IAudioRenderControl

    ///////////////////////////////////////////////////////////////

    override fun onCreate() {
        logger.i("DLNARendererService create.")

        super.onCreate()

        videoRenderControl = VideoRenderControlImpl(applicationContext)
        audioRenderControl = AudioRenderControlImpl(applicationContext)

        try {
            localDevice = createRendererDevice(Utils.getHttpBaseUrl(applicationContext))
            upnpService.registry.addDevice(localDevice)
        } catch (e: Exception) {
            e.printStackTrace()
            stopSelf()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_STICKY

    override fun onBind(intent: Intent): IBinder? = serviceBinder

    override fun onDestroy() {
        logger.w("DLNARendererService destroy.")

        localDevice?.also { device ->
            upnpService.registry.removeDevice(device)
        }
        (videoRenderControl as? VideoRenderControlImpl)?.videoControl = null

        super.onDestroy()
    }

    override fun createConfiguration(): UpnpServiceConfiguration =
        object : AndroidUpnpServiceConfiguration() {
            override fun getAliveIntervalMillis(): Int = 5 * 1000
        }

    ///////////////////////////////////////////////////////////////

    @Throws(ValidationException::class, IOException::class)
    protected fun createRendererDevice(baseUrl: String): LocalDevice {
        val info = "DLNA_MediaPlayer-$baseUrl-${Build.MODEL}-${Build.MANUFACTURER}"
        val udn = try {
            UDN(UUID.nameUUIDFromBytes(info.toByteArray()))
        } catch (ex: Exception) {
            UDN(UUID.randomUUID())
        }

        logger.i("create local device: [MediaRenderer][${udn.identifierString.split("-").last()}]($baseUrl)")

        return LocalDevice(
            DeviceIdentity(udn),
            UDADeviceType("MediaRenderer", 1),
            DeviceDetails(
                "DMR (${Build.MODEL})",
                ManufacturerDetails(Build.MANUFACTURER),
                ModelDetails(Build.MODEL, "MPI MediaPlayer", "v1", baseUrl)
            ),
            emptyArray(),
            generateLocalServices()
        )
    }

    @Suppress("UNCHECKED_CAST")
    protected open fun generateLocalServices(): Array<LocalService<*>> {
        val serviceBinder = AnnotationLocalServiceBinder()

        // av transport service
        val videoRenderService = serviceBinder.read(VideoRenderServiceImpl::class.java) as LocalService<VideoRenderServiceImpl>
        videoRenderService.manager = object : LastChangeAwareServiceManager<VideoRenderServiceImpl>(videoRenderService, AVTransportLastChangeParser()) {
            override fun createServiceInstance(): VideoRenderServiceImpl =
                VideoRenderServiceImpl(videoRenderControl)
        }
        // render service
        val audioRenderService = serviceBinder.read(AudioRenderServiceImpl::class.java) as LocalService<AudioRenderServiceImpl>
        audioRenderService.manager = object : LastChangeAwareServiceManager<AudioRenderServiceImpl>(audioRenderService, RenderingControlLastChangeParser()) {
            override fun createServiceInstance(): AudioRenderServiceImpl =
                AudioRenderServiceImpl(audioRenderControl)
        }

        return arrayOf(videoRenderService, audioRenderService)
    }

    ///////////////////////////////////////////////////////////////

    // ---- BinderWrapper
    protected inner class DLNARendererServiceBinder : AndroidUpnpServiceImpl.Binder(), IRenderServiceBinder {
        override val service: DLNARendererService
            get() = this@DLNARendererService
    }

    ///////////////////////////////////////////////////////////////

//    fun updateDevice() {
//        localDevice?.run {
//            upnpService.registry.addDevice(this)
//        }
//    }

    fun bindVideoControl(videoControl: IVideoRenderControl.IVideoControl?) {
        (videoRenderControl as? VideoRenderControlImpl)?.videoControl = videoControl
    }

    fun notifyAvTransportLastChange(state: IVideoRenderControl.EVideoState) {
        val event : EventedValue<*> = AVTransportVariable.TransportState(state.toTransportState())
        val manager = localDevice?.findService(UDAServiceId("AVTransport"))?.manager
        (manager?.implementation as? VideoRenderServiceImpl)?.lastChange?.setEventedValue(0, event)
        (manager as? LastChangeAwareServiceManager)?.fireLastChange()
    }

    fun notifyRenderControlLastChange(volume: Int) {
        val manager = localDevice?.findService(UDAServiceId("RenderingControl"))?.manager
        (manager?.implementation as? AudioRenderServiceImpl)?.lastChange?.setEventedValue(0, Volume(ChannelVolume(Channel.Master, volume)))
        (manager as? LastChangeAwareServiceManager)?.fireLastChange()
    }
}
