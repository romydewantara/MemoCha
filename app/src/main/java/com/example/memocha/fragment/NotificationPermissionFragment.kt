package com.example.memocha.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.memocha.R
import kotlinx.android.synthetic.main.fragment_notification_permission.*

class NotificationPermissionFragment(
    private val notificationState: Boolean,
    private val badgeState: Boolean
) : Fragment(R.layout.fragment_notification_permission) {

    var isNotificationAllowed = false
    var isBadgeAllowed = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init()
        initListener()
    }

    private fun init() {
        isNotificationAllowed = notificationState
        isBadgeAllowed = badgeState

        notificationOnOff.isChecked = isNotificationAllowed
        badgeOnOff.isChecked = isBadgeAllowed
        if (isNotificationAllowed) cardViewBadge.visibility = View.VISIBLE
    }

    private fun initListener() {
        notificationOnOff.setOnCheckedChangeListener { _, isNotificationChecked ->
            isNotificationAllowed = isNotificationChecked
            if (isNotificationAllowed) {
                cardViewBadge.visibility = View.VISIBLE
            } else {
                cardViewBadge.visibility = View.GONE
                badgeOnOff.isChecked = false
            }
        }
        badgeOnOff.setOnCheckedChangeListener { _, isChecked ->
            isBadgeAllowed = isChecked
        }
    }
}