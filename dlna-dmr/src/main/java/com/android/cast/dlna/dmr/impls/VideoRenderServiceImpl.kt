package com.android.cast.dlna.dmr.impls

import com.android.cast.dlna.dmr.commons.IVideoRenderControl
import org.fourthline.cling.model.types.UnsignedIntegerFourBytes
import org.fourthline.cling.support.avtransport.AbstractAVTransportService
import org.fourthline.cling.support.model.DeviceCapabilities
import org.fourthline.cling.support.model.MediaInfo
import org.fourthline.cling.support.model.PositionInfo
import org.fourthline.cling.support.model.TransportAction
import org.fourthline.cling.support.model.TransportInfo
import org.fourthline.cling.support.model.TransportSettings

class VideoRenderServiceImpl(private val _videoRenderControl: IVideoRenderControl) : AbstractAVTransportService() {
    override fun getCurrentInstanceIds(): Array<UnsignedIntegerFourBytes> =
        arrayOf(UnsignedIntegerFourBytes(0))

    override fun getCurrentTransportActions(instanceId: UnsignedIntegerFourBytes): Array<TransportAction> =
        _videoRenderControl.currentTransportActions

    override fun getDeviceCapabilities(instanceId: UnsignedIntegerFourBytes): DeviceCapabilities =
        _videoRenderControl.deviceCapabilities

    override fun getMediaInfo(instanceId: UnsignedIntegerFourBytes): MediaInfo =
        _videoRenderControl.mediaInfo

    override fun getPositionInfo(instanceId: UnsignedIntegerFourBytes): PositionInfo =
        _videoRenderControl.positionInfo

    override fun getTransportInfo(instanceId: UnsignedIntegerFourBytes): TransportInfo =
        _videoRenderControl.transportInfo

    override fun getTransportSettings(instanceId: UnsignedIntegerFourBytes): TransportSettings =
        _videoRenderControl.transportSettings

    override fun next(instanceId: UnsignedIntegerFourBytes) =
        _videoRenderControl.next()

    override fun pause(instanceId: UnsignedIntegerFourBytes) =
        _videoRenderControl.pause()

    override fun play(instanceId: UnsignedIntegerFourBytes, speed: String) =
        _videoRenderControl.play(speed)

    override fun previous(instanceId: UnsignedIntegerFourBytes) =
        _videoRenderControl.previous()

    override fun seek(instanceId: UnsignedIntegerFourBytes, unit: String, target: String) =
        _videoRenderControl.seek(unit, target)

    override fun setAVTransportURI(instanceId: UnsignedIntegerFourBytes, currentURI: String, currentURIMetaData: String) =
        _videoRenderControl.setAVTransportURI(currentURI, currentURIMetaData)

    override fun setNextAVTransportURI(instanceId: UnsignedIntegerFourBytes, nextURI: String, nextURIMetaData: String) =
        _videoRenderControl.setNextAVTransportURI(nextURI, nextURIMetaData)

    override fun setPlayMode(instanceId: UnsignedIntegerFourBytes, newPlayMode: String) =
        _videoRenderControl.setPlayMode(newPlayMode)

    override fun stop(instanceId: UnsignedIntegerFourBytes) =
        _videoRenderControl.stop()

    override fun record(instanceId: UnsignedIntegerFourBytes) {} // ignore

    override fun setRecordQualityMode(instanceId: UnsignedIntegerFourBytes, newRecordQualityMode: String) {} // ignore
}
