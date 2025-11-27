import { defineStore } from 'pinia'
import { ref } from 'vue'

export type CallState = 'idle' | 'calling' | 'incoming' | 'connected' | 'ended'
export type CallType = 'audio' | 'video'

export interface RemoteUserInfo {
    id: string
    name: string
    avatar: string
}

export const useCallStore = defineStore('call', () => {
    const callState = ref<CallState>('idle')
    const callType = ref<CallType>('audio')
    const remoteUserInfo = ref<RemoteUserInfo | null>(null)
    const startTime = ref<Date | null>(null)
    const isMuted = ref(false)
    const isCameraOn = ref(true)
    const isFrontCamera = ref(true)
    const isSpeakerOn = ref(false) // For UI toggle state (though browser handles output)
    let callingTimeout: any = null

    // Actions
    function startCall(userInfo: RemoteUserInfo, type: CallType = 'audio') {
        callState.value = 'calling'
        callType.value = type
        remoteUserInfo.value = userInfo
        isMuted.value = false
        isCameraOn.value = type === 'video'
        isSpeakerOn.value = type === 'video' // Default to speaker for video calls

        // 60s timeout for no answer
        if (callingTimeout) clearTimeout(callingTimeout)
        callingTimeout = setTimeout(() => {
            if (callState.value === 'calling') {
                // Timeout - cancel call
                endCall()
                // Ideally we should send a "cancel" signal here too, but for now just end local state
                // In a real app, WebRTCService should handle the signaling part of "cancel"
            }
        }, 60000)
    }

    function incomingCall(userInfo: RemoteUserInfo, type: CallType = 'audio') {
        callState.value = 'incoming'
        callType.value = type
        remoteUserInfo.value = userInfo
        isMuted.value = false
        isCameraOn.value = type === 'video'
        isSpeakerOn.value = type === 'video'
    }

    function connectCall() {
        if (callingTimeout) clearTimeout(callingTimeout)
        callState.value = 'connected'
        startTime.value = new Date()
    }

    function endCall() {
        if (callingTimeout) clearTimeout(callingTimeout)
        callState.value = 'ended'
        startTime.value = null
        // Don't nullify remoteUserInfo immediately so we can show "Ended" screen with avatar
        setTimeout(() => {
            if (callState.value === 'ended') {
                reset()
            }
        }, 2000)
    }

    function reset() {
        if (callingTimeout) clearTimeout(callingTimeout)
        callState.value = 'idle'
        callType.value = 'audio'
        remoteUserInfo.value = null
        startTime.value = null
        isMuted.value = false
        isCameraOn.value = true
        isFrontCamera.value = true
        isSpeakerOn.value = false
    }

    return {
        callState,
        callType,
        remoteUserInfo,
        startTime,
        isMuted,
        isCameraOn,
        isFrontCamera,
        isSpeakerOn,
        startCall,
        incomingCall,
        connectCall,
        endCall,
        reset
    }
})
