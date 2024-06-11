package com.android.cast.dlna.dmr.impls

import android.content.Context
import com.android.cast.dlna.core.Logger
import com.android.cast.dlna.dmr.commons.IVideoRenderControl
import org.fourthline.cling.model.ModelUtil
import org.fourthline.cling.support.avtransport.AVTransportException
import org.fourthline.cling.support.model.MediaInfo
import org.fourthline.cling.support.model.PositionInfo
import org.fourthline.cling.support.model.TransportSettings
import org.fourthline.cling.support.model.DeviceCapabilities
import org.fourthline.cling.support.model.StorageMedium
import org.fourthline.cling.support.model.TransportAction
import org.fourthline.cling.support.model.TransportInfo
import org.fourthline.cling.support.model.TransportState
import org.fourthline.cling.support.model.TransportStatus

class VideoRenderControlImpl(override val applicationContext: Context) : IVideoRenderControl {
    companion object {
        private val TRANSPORT_ACTION_STOPPED = arrayOf(TransportAction.Play)
        private val TRANSPORT_ACTION_PLAYING = arrayOf(TransportAction.Stop, TransportAction.Pause, TransportAction.Seek)
        private val TRANSPORT_ACTION_PAUSE_PLAYBACK = arrayOf(TransportAction.Play, TransportAction.Seek, TransportAction.Stop)
    }

    //////////////////////////////////////////////////////////////////////////////////////

    var videoControl: IVideoRenderControl.IVideoControl? = null
        set(value) {
            if (value != null) {
                _mediaInfo = MediaInfo(_currentURI, _currentURIMetaData)
                _positionInfo = PositionInfo(0, _currentURIMetaData, _currentURI)
            } else {
                videoControl?.destroy()
                _mediaInfo = MediaInfo()
                _positionInfo = PositionInfo()
            }
            field = value
        }
    private var _positionInfo = PositionInfo()
    private var _mediaInfo = MediaInfo()
    private var _currentURI: String? = null
    private var _currentURIMetaData: String? = null
    private var _nextURI: String? = null
    private var _nextURIMetaData: String? = null
    private var _previousURI: String? = null
    private var _previousURIMetaData: String? = null

    //////////////////////////////////////////////////////////////////////////////////////

    override val logger = Logger.create("AVTransportController")
    override val transportSettings = TransportSettings()
    override val deviceCapabilities: DeviceCapabilities = DeviceCapabilities(arrayOf(StorageMedium.UNKNOWN))
    override val transportInfo
        get() = videoControl?.let { ctrl ->
            TransportInfo(ctrl.getState().toTransportState(), TransportStatus.OK, "1")
        } ?: TransportInfo()
    override val mediaInfo
        get() = _mediaInfo
    override val positionInfo: PositionInfo
        get() = videoControl?.let { ctrl ->
            val duration = ModelUtil.toTimeString(ctrl.duration / 1000)
            val realTime = ModelUtil.toTimeString(ctrl.currentPosition / 1000)
            PositionInfo(0, duration, _currentURI, realTime, realTime)
        } ?: PositionInfo()
    override val currentTransportActions: Array<TransportAction>
        get() = when (transportInfo.currentTransportState) {
            TransportState.PLAYING -> TRANSPORT_ACTION_PLAYING
            TransportState.PAUSED_PLAYBACK -> TRANSPORT_ACTION_PAUSE_PLAYBACK
            else -> TRANSPORT_ACTION_STOPPED
        }

    //////////////////////////////////////////////////////////////////////////////////////

    @Throws(AVTransportException::class)
    override fun setAVTransportURI(currentURI: String, currentURIMetaData: String?) {
        super.setAVTransportURI(currentURI, currentURIMetaData)
        _currentURI = currentURI
        _currentURIMetaData = currentURIMetaData
    }

    override fun setNextAVTransportURI(nextURI: String, nextURIMetaData: String?) {
        super.setNextAVTransportURI(nextURI, nextURIMetaData)
        _nextURI = nextURI
        _nextURIMetaData = nextURIMetaData
    }

    override fun play(speed: String?) {
        super.play(speed)
        videoControl?.play()
    }

    override fun pause() {
        super.pause()
        videoControl?.pause()
    }

    override fun seek(unit: String?, target: String?) {
        super.seek(unit, target)
        try {
            videoControl?.seek(ModelUtil.fromTimeString(target) * 1000)
        } catch (e: Exception) {
            logger.w("seek failed: $e")
        }
    }

    override fun next() {
        super.next()
        if (_nextURI != null && _nextURIMetaData != null) {
            _previousURI = _currentURI
            _previousURIMetaData = _currentURIMetaData
            setAVTransportURI(_nextURI!!, _nextURIMetaData)
        }
        _nextURI = null
        _nextURIMetaData = null
    }

    override fun previous() {
        super.previous()
        if (_previousURI != null && _previousURIMetaData != null) {
            _nextURI = _currentURI
            _nextURIMetaData = _currentURIMetaData
            setAVTransportURI(_previousURI!!, _previousURIMetaData)
        }
        _previousURI = null
        _previousURIMetaData = null
    }

    override fun stop() {
        super.stop()
        videoControl?.destroy()
        _mediaInfo = MediaInfo()
        _positionInfo = PositionInfo()
    }
}
