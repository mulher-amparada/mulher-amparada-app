package com.mulheres

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent

class MyDeviceAdminReceiver : AccessibilityService() {

    companion object {

        private var instancia: MyDeviceAdminReceiver? = null

        fun bloquearTela(): Boolean {

            return instancia?.performGlobalAction(
                GLOBAL_ACTION_LOCK_SCREEN
            ) ?: false
        }

        fun estaAtivo(): Boolean {

            return instancia != null
        }
    }

    override fun onServiceConnected() {

        super.onServiceConnected()

        instancia = this
    }

    override fun onAccessibilityEvent(
        event: AccessibilityEvent?
    ) {
    }

    override fun onInterrupt() {
    }

    override fun onDestroy() {

        if (instancia === this) {
            instancia = null
        }

        super.onDestroy()
    }
}