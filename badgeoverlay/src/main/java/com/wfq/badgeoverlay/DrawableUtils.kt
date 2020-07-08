package com.wfq.badgeoverlay

import android.content.Context
import android.content.res.Resources.NotFoundException
import android.text.TextUtils
import android.util.AttributeSet
import android.util.Xml
import androidx.annotation.XmlRes
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException


/**
 * @author  wfq
 * @date    2020/6/14
 */
object DrawableUtils {

    fun parseDrawableXml(context: Context, @XmlRes id: Int, startTag: CharSequence): AttributeSet {
        return try {
            val parser: XmlPullParser = context.resources.getXml(id)
            var type: Int
            do {
                type = parser.next()
            } while (type != XmlPullParser.START_TAG && type != XmlPullParser.END_DOCUMENT)
            if (type != XmlPullParser.START_TAG) {
                throw XmlPullParserException("No start tag found")
            }
            if (!TextUtils.equals(parser.name, startTag)) {
                throw XmlPullParserException("Must have a <$startTag> start tag")
            }
            Xml.asAttributeSet(parser)
        } catch (e: XmlPullParserException) {
            val exception = NotFoundException(
                "Can't load badge resource ID #0x" + Integer.toHexString(id)
            )
            exception.initCause(e)
            throw exception
        } catch (e: IOException) {
            val exception = NotFoundException(
                "Can't load badge resource ID #0x" + Integer.toHexString(id)
            )
            exception.initCause(e)
            throw exception
        }
    }
}