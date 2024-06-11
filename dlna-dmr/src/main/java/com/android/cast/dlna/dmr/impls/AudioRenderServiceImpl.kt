package com.android.cast.dlna.dmr.impls

import com.android.cast.dlna.dmr.commons.IAudioRenderControl
import org.fourthline.cling.model.types.UnsignedIntegerFourBytes
import org.fourthline.cling.model.types.UnsignedIntegerTwoBytes
import org.fourthline.cling.support.model.Channel
import org.fourthline.cling.support.renderingcontrol.AbstractAudioRenderingControl

class AudioRenderServiceImpl(private val _audioRenderControl: IAudioRenderControl) : AbstractAudioRenderingControl() {

    override fun setMute(instanceId: UnsignedIntegerFourBytes, channelName: String, desiredMute: Boolean) =
        _audioRenderControl.setMute(channelName, desiredMute)

    override fun getMute(instanceId: UnsignedIntegerFourBytes, channelName: String): Boolean =
        _audioRenderControl.getMute(channelName)

    override fun setVolume(instanceId: UnsignedIntegerFourBytes, channelName: String, desiredVolume: UnsignedIntegerTwoBytes) =
        _audioRenderControl.setVolume(channelName, desiredVolume)

    override fun getVolume(instanceId: UnsignedIntegerFourBytes, channelName: String): UnsignedIntegerTwoBytes =
        _audioRenderControl.getVolume(channelName)

    override fun getCurrentChannels(): Array<Channel> =
        arrayOf(Channel.Master)

    override fun getCurrentInstanceIds(): Array<UnsignedIntegerFourBytes> =
        arrayOf(UnsignedIntegerFourBytes(0))
}