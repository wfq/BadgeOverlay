package com.wfq.badge.simple

import android.graphics.Rect
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.updateLayoutParams
import com.wfq.badgeoverlay.BadgeDrawable
import com.wfq.badgeoverlay.badge
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val groupOverlay = root.overlay
        ContextCompat.getDrawable(this, R.mipmap.ic_launcher)?.let {
            it.bounds = Rect(100, 100, 300, 300)
            groupOverlay.add(it)
        }

        // Avoid clipping a badge if it's displayed.

        root.clipChildren = false
//        root.clipToPadding = false
        content.post {
            val badgeDrawable = BadgeDrawable(this)
            badgeDrawable.setText("王方琪")
            badgeDrawable.updateBadgeCoordinates(content)
            content.overlay.add(badgeDrawable)
        }
        content.badge()

        var badge1 = bottom_navigation.getOrCreateBadge(R.id.page_1)
        badge1.isVisible = true
// An icon only badge will be displayed unless a number is set:

        var badge = bottom_navigation.getOrCreateBadge(R.id.page_2)
        badge.isVisible = true
// An icon only badge will be displayed unless a number is set:
        badge.number = 9


        content.setOnClickListener {
            it.updateLayoutParams<ConstraintLayout.LayoutParams> {
                setMargins(100, 0, 0, 0)
            }
        }
    }
}
