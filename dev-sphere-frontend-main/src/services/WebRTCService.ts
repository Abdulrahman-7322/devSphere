import { useCallStore, type CallType } from '../stores/callStore'
import wsService, { WSReqType } from './WebSocketService'
import { shallowRef } from 'vue'

// Configuration for STUN servers
const rtcConfig = {
    iceServers: [
        { urls: 'stun:stun.l.google.com:19302' },
        { urls: 'stun:stun1.l.google.com:19302' },
        { urls: 'stun:stun2.l.google.com:19302' },
    ]
}

class WebRTCService {
    private peerConnection: RTCPeerConnection | null = null
    private localMediaStream: MediaStream | null = null // Internal non-reactive stream
    public localStream = shallowRef<MediaStream | null>(null) // Reactive ref for UI binding
    public remoteStream = shallowRef<MediaStream | null>(null) // Reactive ref for UI binding
    private targetId: string = ''

    // Optimization: Queue for candidates and pending offer
    private pendingOffer: RTCSessionDescriptionInit | null = null
    private candidateQueue: RTCIceCandidateInit[] = []

    constructor() {
        // Register handler to avoid circular dependency import in WebSocketService
        wsService.registerRtcHandler(this.handleSignal.bind(this))
    }

    // Initialize call (Caller)
    async startCall(targetId: string, targetUserInfo: any, type: CallType = 'audio') {
        if (wsService.status.value !== 1) { // WSStatus.OPEN is 1
            wsService.ensureConnected()
            alert('网络连接未就绪，正在尝试重连，请稍后再试')
            return
        }

        const callStore = useCallStore()
        this.targetId = targetId
        callStore.startCall(targetUserInfo, type)

        await this.initPeerConnection()

        // Create Offer
        const offer = await this.peerConnection!.createOffer()
        await this.peerConnection!.setLocalDescription(offer)

        // Send Offer
        this.sendSignal('offer', offer)
    }

    // Handle incoming call (Callee)
    async handleIncomingCall(senderId: string, senderUserInfo: any, offer: RTCSessionDescriptionInit) {
        const callStore = useCallStore()
        // If already in a call, reject (busy)
        if (callStore.callState !== 'idle') {
            this.sendSignal('busy', null, senderId)
            return
        }

        this.targetId = senderId
        // Extract call type from senderUserInfo if available, default to audio
        const type: CallType = senderUserInfo.callType || 'audio'
        callStore.incomingCall(senderUserInfo, type)

        // Optimization: Store offer, do NOT init peer connection yet
        // Wait for user to accept to avoid occupying microphone
        this.pendingOffer = offer
    }

    // Accept Call
    async acceptCall() {
        const callStore = useCallStore()

        if (!this.pendingOffer) {
            console.error('[WebRTC] No pending offer to accept')
            return
        }

        // 1. Init PeerConnection now
        await this.initPeerConnection()

        // 2. Set Remote Description
        await this.peerConnection!.setRemoteDescription(new RTCSessionDescription(this.pendingOffer))
        this.pendingOffer = null

        // 3. Process Queued Candidates
        while (this.candidateQueue.length > 0) {
            const candidate = this.candidateQueue.shift()
            if (candidate) {
                await this.peerConnection!.addIceCandidate(new RTCIceCandidate(candidate))
            }
        }

        // 4. Create Answer
        const answer = await this.peerConnection!.createAnswer()
        await this.peerConnection!.setLocalDescription(answer)

        // 5. Send Answer
        this.sendSignal('answer', answer)
        callStore.connectCall()
    }

    // Reject Call
    rejectCall() {
        const callStore = useCallStore()
        this.sendSignal('reject', null)
        this.cleanup()
        callStore.reset()
    }

    // Hangup
    hangup() {
        const callStore = useCallStore()
        this.sendSignal('hangup', null)
        this.cleanup()
        callStore.endCall()
    }

    // Handle incoming signals
    async handleSignal(data: any) {
        const callStore = useCallStore()
        const { type, payload, senderId, senderUserInfo } = data

        switch (type) {
            case 'offer':
                // Handle incoming offer
                if (senderId && senderUserInfo) {
                    await this.handleIncomingCall(senderId, senderUserInfo, payload)
                } else {
                    console.error('[WebRTC] Received offer without sender info')
                }
                break;

            case 'answer':
                if (this.peerConnection && callStore.callState === 'calling') {
                    await this.peerConnection.setRemoteDescription(new RTCSessionDescription(payload))
                    callStore.connectCall()
                }
                break

            case 'candidate':
                if (payload) {
                    // Optimization: Queue candidate if remote description is not set yet
                    if (this.peerConnection && this.peerConnection.remoteDescription) {
                        await this.peerConnection.addIceCandidate(new RTCIceCandidate(payload))
                    } else {
                        // Queue it (either we haven't init PC yet, or haven't set remote desc)
                        console.log('[WebRTC] Queuing candidate')
                        this.candidateQueue.push(payload)
                    }
                }
                break

            case 'hangup':
                this.cleanup()
                callStore.endCall()
                break

            case 'busy':
                alert('对方正忙')
                this.cleanup()
                callStore.reset()
                break

            case 'reject':
                alert('对方已拒绝')
                this.cleanup()
                callStore.reset()
                break
        }
    }

    // Toggle Mute
    toggleMute() {
        const callStore = useCallStore()
        if (this.localMediaStream) {
            this.localMediaStream.getAudioTracks().forEach(track => {
                track.enabled = !track.enabled
            })
            callStore.isMuted = !callStore.isMuted
        }
    }

    // Toggle Video
    toggleVideo() {
        const callStore = useCallStore()
        if (this.localMediaStream) {
            this.localMediaStream.getVideoTracks().forEach(track => {
                track.enabled = !track.enabled
            })
            callStore.isCameraOn = !callStore.isCameraOn
        }
    }

    // Switch Camera (Mobile)
    async switchCamera() {
        const callStore = useCallStore()
        if (callStore.callType !== 'video') return

        // Stop current video track
        if (this.localMediaStream) {
            this.localMediaStream.getVideoTracks().forEach(track => track.stop())
        }

        callStore.isFrontCamera = !callStore.isFrontCamera
        const constraints = {
            audio: true,
            video: {
                facingMode: callStore.isFrontCamera ? 'user' : 'environment',
                width: { ideal: 1280 },
                height: { ideal: 720 }
            }
        }

        try {
            const newStream = await navigator.mediaDevices.getUserMedia(constraints)

            // Replace video track in PeerConnection
            const videoTrack = newStream.getVideoTracks()[0]
            if (this.peerConnection) {
                const sender = this.peerConnection.getSenders().find(s => s.track?.kind === 'video')
                if (sender && videoTrack) {
                    sender.replaceTrack(videoTrack)
                }
            }

            // Update local stream
            // We need to keep audio track from old stream if we only got video? 
            // Actually getUserMedia with audio:true returns both.
            // Let's just replace the whole stream ref
            this.localMediaStream = newStream
            this.localStream.value = newStream

            // Sync mute state
            this.localMediaStream.getAudioTracks().forEach(track => {
                track.enabled = !callStore.isMuted
            })

        } catch (err) {
            console.error('Failed to switch camera', err)
            alert('切换摄像头失败')
        }
    }

    // Private helpers
    private async initPeerConnection() {
        const callStore = useCallStore()

        // Constraints based on call type
        const constraints = {
            audio: true,
            video: callStore.callType === 'video' ? {
                facingMode: 'user',
                width: { ideal: 1280 },
                height: { ideal: 720 }
            } : false
        }

        // Get Local Stream
        try {
            this.localMediaStream = await navigator.mediaDevices.getUserMedia(constraints)
            this.localStream.value = this.localMediaStream
        } catch (err) {
            console.error('Failed to get user media', err)
            alert('无法获取媒体权限')
            throw err
        }

        this.peerConnection = new RTCPeerConnection(rtcConfig)

        // Add Tracks
        if (this.localMediaStream) {
            this.localMediaStream.getTracks().forEach(track => {
                this.peerConnection!.addTrack(track, this.localMediaStream!)
            })
        }

        // Handle Remote Stream
        this.peerConnection.ontrack = (event) => {
            console.log('[WebRTC] Received remote track')
            // Optimization: Update reactive ref instead of direct DOM manipulation
            this.remoteStream.value = event.streams[0]
        }

        // Handle ICE Candidates
        this.peerConnection.onicecandidate = (event) => {
            if (event.candidate) {
                this.sendSignal('candidate', event.candidate)
            }
        }
    }

    private sendSignal(type: string, payload: any, targetId?: string) {
        console.log('[WebRTC] Sending signal:', type, 'to', targetId || this.targetId)
        const callStore = useCallStore()

        // Get current user info from localStorage
        const userStr = localStorage.getItem('userInfo')
        let rawUserInfo: any = {}
        if (userStr) {
            try {
                rawUserInfo = JSON.parse(userStr)
            } catch (e) { }
        }

        // Normalize user info for the call store
        const senderUserInfo = {
            id: rawUserInfo.id || rawUserInfo.uid,
            name: rawUserInfo.realName || rawUserInfo.username || 'Unknown',
            avatar: rawUserInfo.headUrl || rawUserInfo.avatar || `https://api.dicebear.com/7.x/avataaars/svg?seed=${rawUserInfo.username || 'default'}`,
            callType: callStore.callType // Include call type
        }

        // Also get senderId from user object
        const senderId = senderUserInfo.id

        wsService.send({
            type: WSReqType.RTC_SIGNAL,
            userId: targetId || this.targetId,
            data: JSON.stringify({
                type,
                payload,
                senderId,
                senderUserInfo
            })
        })
    }

    private cleanup() {
        if (this.localMediaStream) {
            this.localMediaStream.getTracks().forEach(track => track.stop())
            this.localMediaStream = null
        }
        if (this.peerConnection) {
            this.peerConnection.close()
            this.peerConnection = null
        }
        this.localStream.value = null
        this.remoteStream.value = null
        this.targetId = ''
        this.pendingOffer = null
        this.candidateQueue = []
    }
}

export default new WebRTCService()
