package com.android.cast.dlna.dmr.commons

import android.content.Context
import android.content.Intent
import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator
import com.android.cast.dlna.core.Logger
import com.android.cast.dlna.dmr.cons.CDlnaDmrParams
import org.fourthline.cling.model.types.ErrorCode.INVALID_ARGS
import org.fourthline.cling.support.avtransport.AVTransportException
import org.fourthline.cling.support.model.DeviceCapabilities
import org.fourthline.cling.support.model.MediaInfo
import org.fourthline.cling.support.model.PositionInfo
import org.fourthline.cling.support.model.TransportAction
import org.fourthline.cling.support.model.TransportInfo
import org.fourthline.cling.support.model.TransportSettings
import org.fourthline.cling.support.model.TransportState
import java.net.URI

/**
 * @ClassName IVideoRenderControl
 * @Description TODO
 * @Author Mozhimen / Kolin Zhao
 * @Date 2024/6/10 19:41
 * @Version 1.0
 */
// -------------------------------------------------------------------------------------------
// - AvTransport AvTransportControl
// -------------------------------------------------------------------------------------------
interface IVideoRenderControl : IRenderControl {

    interface IVideoControl : IFrameControl {
        val currentPosition: Long
        val duration: Long

        override fun play() {
            play(1.0)
        }

        fun play(speed: Double? /*= 1.0*/)
        fun pause()
        fun seek(millSeconds: Long)
        fun stop()
        fun destroy()
        fun getState(): EVideoState
    }

    interface IFrameControl {
        fun play()
    }

    ///////////////////////////////////////////////////////////////////////////////////

    enum class EVideoState {
        IDLE, PREPARING, PLAYING, PAUSED, STOPPED, ERROR;

        fun toTransportState(): TransportState {
            return when (this) {
                PLAYING, PREPARING -> TransportState.PLAYING
                PAUSED -> TransportState.PAUSED_PLAYBACK
                STOPPED, ERROR -> TransportState.STOPPED
                else -> TransportState.NO_MEDIA_PRESENT
            }
        }
    }

    class VideoAction constructor(
        var currentURI: String? = null,
        var currentURIMetaData: String? = null,
        var nextURI: String? = null,
        var nextURIMetaData: String? = null,
        var stop: String? = null,
    ) : Parcelable {
        constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString()
        )

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeString(currentURI)
            parcel.writeString(currentURIMetaData)
            parcel.writeString(nextURI)
            parcel.writeString(nextURIMetaData)
        }

        override fun describeContents(): Int = 0

        companion object CREATOR : Creator<VideoAction> {
            override fun createFromParcel(parcel: Parcel): VideoAction {
                return VideoAction(parcel)
            }

            override fun newArray(size: Int): Array<VideoAction?> {
                return arrayOfNulls(size)
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////

    val logger: Logger
    val applicationContext: Context
    val currentTransportActions: Array<TransportAction>
    val deviceCapabilities: DeviceCapabilities
    val mediaInfo: MediaInfo
    val positionInfo: PositionInfo
    val transportInfo: TransportInfo
    val transportSettings: TransportSettings

    ///////////////////////////////////////////////////////////////////////////////////

    fun setAVTransportURI(currentURI: String, currentURIMetaData: String?) {
        logger.i("setAVTransportURI: currentURI=$currentURI")
        currentURIMetaData?.let { logger.i("setAVTransportURI: currentURIMetaData=$it") }
        try {
            URI(currentURI)
        } catch (ex: Exception) {
            throw AVTransportException(INVALID_ARGS, "CurrentURI can not be null or malformed")
        }

        startCastActivity {
            this.currentURI = currentURI
            this.currentURIMetaData = currentURIMetaData
        }
    }

    fun setNextAVTransportURI(nextURI: String, nextURIMetaData: String?) {
        logger.i("setNextAVTransportURI: nextURI=$nextURI")
        nextURIMetaData?.let { logger.i("setNextAVTransportURI: nextURIMetaData=$it") }

        startCastActivity {
            this.nextURI = nextURI
            this.nextURIMetaData = nextURIMetaData
        }
    }

    fun setPlayMode(newPlayMode: String?) {
        logger.i("setPlayMode: newPlayMode=$newPlayMode")
    }

    fun play(speed: String?) {
        logger.i("play: speed=$speed")
    }

    fun pause() {
        logger.i("pause")
    }

    fun seek(unit: String?, target: String?) {
        logger.i("seek: unit=$unit, target=$target")
    }

    fun previous() {
        logger.i("previous")
    }

    fun next() {
        logger.i("next")
    }

    fun stop() {
        logger.i("stop")
//        startCastActivity {
//            this.stop = "stop"
//        }
    }

    ///////////////////////////////////////////////////////////////////////////////////

    private fun startCastActivity(function: VideoAction.() -> Unit) {
        applicationContext.startActivity(Intent(CDlnaDmrParams.Action_SetAvTransport).apply {
            val videoAction = VideoAction()
            function(videoAction)
            this.putExtra(CDlnaDmrParams.Extra_CastAction, videoAction)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) // start from service content,should add 'FLAG_ACTIVITY_NEW_TASK' flag.
        })
    }


}