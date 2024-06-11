package com.android.cast.dlna.dmr.commons

import com.android.cast.dlna.core.Logger
import org.fourthline.cling.model.types.UnsignedIntegerTwoBytes

/**
 * @ClassName AudioControl
 * @Description TODO
 * @Author Mozhimen / Kolin Zhao
 * @Date 2024/6/10 19:34
 * @Version 1.0
 */
// -------------------------------------------------------------------------------------------
// - Audio
// -------------------------------------------------------------------------------------------
interface IAudioRenderControl : IRenderControl {
    val logger: Logger
    fun setMute(channelName: String, desiredMute: Boolean) {
        logger.i("setMute: $desiredMute")
    }

    fun getMute(channelName: String): Boolean
    fun setVolume(channelName: String, desiredVolume: UnsignedIntegerTwoBytes) {
        logger.i("setVolume: ${desiredVolume.value}")
    }

    fun getVolume(channelName: String): UnsignedIntegerTwoBytes
}