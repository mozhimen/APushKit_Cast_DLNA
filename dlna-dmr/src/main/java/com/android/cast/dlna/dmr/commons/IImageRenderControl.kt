package com.android.cast.dlna.dmr.commons

import com.android.cast.dlna.core.Logger

/**
 * @ClassName IImageRenderControl
 * @Description TODO
 * @Author Mozhimen / Kolin Zhao
 * @Date 2024/6/10 20:06
 * @Version 1.0
 */
interface IImageRenderControl {
    val logger: Logger
    fun setImage()
}