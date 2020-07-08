package com.wfq.badgeoverlay

import android.view.View

/**
 * @author  wfq
 * @date    2020/6/11
 */
fun View.badge(block: (BadgeDrawable.BadgeState.() -> Unit)? = null): BadgeDrawable {
    val badgeDrawable = getTag(R.id.wfq_overlay_badge) as? BadgeDrawable ?: BadgeDrawable(context)
    if (block != null) {
        badgeDrawable.badgeState.apply(block)
    }
    setTag(R.id.wfq_overlay_badge, badgeDrawable)
    post { // 保证UI测量绘制完成后，再刷新BadgeDrawable
        badgeDrawable.updateBadgeCoordinates(this)
        overlay.add(badgeDrawable) // overlay add方法内部已做了判重处理，不必担心重复添加相同的BadgeDrawable
    }
    return badgeDrawable
}

/**
 * 移除角标
 */
fun View.removeBadge() {
    (getTag(R.id.wfq_overlay_badge) as? BadgeDrawable)?.let {
        overlay.remove(it)
        setTag(R.id.wfq_overlay_badge, null)
    }
}