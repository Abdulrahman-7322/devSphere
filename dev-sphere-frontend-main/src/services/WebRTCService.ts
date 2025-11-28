import { useCallStore, type CallType } from '../stores/callStore'
import { useChatStore } from '../stores/chatStore'
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
    // Multi-peer support: Map<targetUserId, RTCPeerConnection>
    private peerConnections: Map<string, RTCPeerConnection> = new Map()

    // Local stream is shared across all connections
    private localMediaStream: MediaStream | null = null
    public localStream = shallowRef<MediaStream | null>(null)

    // Remote streams: Map<targetUserId, MediaStream>
    public remoteStreams = shallowRef<Map<string, MediaStream>>(new Map())

    // Legacy support for single P2P UI (points to the first remote stream)
    public remoteStream = shallowRef<MediaStream | null>(null)

    private targetId: string = '' // Primary target (for P2P or Host)

    // Queue for candidates and pending offers: Map<targetUserId, ...>
    private pendingOffers: Map<string, RTCSessionDescriptionInit> = new Map()
    private candidateQueues: Map<string, RTCIceCandidateInit[]> = new Map()

    constructor() {
        wsService.registerRtcHandler(this.handleSignal.bind(this))
    }

    // Initialize call (Caller)
    async startCall(targetId: string, targetUserInfo: any, type: CallType = 'audio', mode: 'p2p' | 'group' = 'p2p') {
        const callStore = useCallStore()
        const chatStore = useChatStore()

        if (callStore.callState !== 'idle') {
            console.warn('[WebRTC] Call already in progress')
            return
        }

        if (wsService.status.value !== 1) {
            wsService.ensureConnected()
            alert('网络连接未就绪，正在尝试重连，请稍后再试')
            return
        }

        this.targetId = targetId
        callStore.startCall(targetUserInfo, type, mode)

        // Get local media first
        await this.initLocalMedia()

        // Send Call Message to Chat
        const messageType = type === 'video' ? 6 : 5
        const content = `发起${type === 'video' ? '视频' : '语音'}通话`
        // Note: sendCallMessage is async but we don't need to await it to block the call start
        chatStore.sendCallMessage(targetId, content, messageType)

        if (mode === 'p2p') {
            // Create PC for the target
            await this.createPeerConnection(targetId)

            // Create & Send Offer
            const pc = this.peerConnections.get(targetId)
            if (pc) {
                const offer = await pc.createOffer()
                await pc.setLocalDescription(offer)
                this.sendSignal('offer', offer, targetId)
            }
        } else {
            // Group mode: We are the first one, so we are "connected"
            callStore.connectCall()
        }
    }

    // Invite users to existing call (Group Mode)
    async inviteUsers(users: any[]) {
        const callStore = useCallStore()
        if (callStore.callMode !== 'group') return

        for (const user of users) {
            if (this.peerConnections.has(user.id)) continue; // Already connected

            callStore.addParticipant(user)

            // Create PC
            await this.createPeerConnection(user.id)

            // Create & Send Offer
            const pc = this.peerConnections.get(user.id)
            if (pc) {
                const offer = await pc.createOffer()
                await pc.setLocalDescription(offer)
                this.sendSignal('offer', offer, user.id)
            }
        }
    }

    // Handle incoming call (Callee)
    async handleIncomingCall(senderId: string, senderUserInfo: any, offer: RTCSessionDescriptionInit, mode: 'p2p' | 'group' = 'p2p') {
        const callStore = useCallStore()

        // If already in a call, reject (busy) - UNLESS it's a group call invite for the SAME group?
        // For now, simple busy logic.
        if (callStore.callState !== 'idle') {
            this.sendSignal('busy', null, senderId)
            return
        }

        this.targetId = senderUserInfo.groupId || senderId
        const type: CallType = senderUserInfo.callType || 'audio'

        // For group call, we want to store the group info as remoteUserInfo if possible
        // But callStore.incomingCall expects sender info. 
        // We might need to adjust callStore or just accept that remoteUserInfo is the sender for now.
        // However, for ContactPicker to work, we need groupId.
        // Let's ensure callStore.remoteUserInfo has the ID we need.

        callStore.incomingCall(senderUserInfo, type, mode)

        // Store offer
        this.pendingOffers.set(senderId, offer)
    }

    // Accept Call (P2P or Group Join)
    async acceptCall() {
        const callStore = useCallStore()
        let targetId = this.targetId // The caller

        // In group mode, targetId is groupId, but offer is from a user (inviter)
        if (callStore.callMode === 'group') {
            const inviterId = this.pendingOffers.keys().next().value
            if (inviterId) {
                targetId = inviterId
            }
        }

        const offer = this.pendingOffers.get(targetId)
        if (!offer) {
            console.error('[WebRTC] No pending offer to accept from', targetId)
            return
        }

        await this.initLocalMedia()

        // Init PC for caller
        await this.createPeerConnection(targetId)
        const pc = this.peerConnections.get(targetId)

        if (pc) {
            await pc.setRemoteDescription(new RTCSessionDescription(offer))
            this.pendingOffers.delete(targetId)

            // Process Queued Candidates
            const queue = this.candidateQueues.get(targetId) || []
            while (queue.length > 0) {
                const candidate = queue.shift()
                if (candidate) {
                    await pc.addIceCandidate(new RTCIceCandidate(candidate))
                }
            }

            const answer = await pc.createAnswer()
            await pc.setLocalDescription(answer)
            this.sendSignal('answer', answer, targetId)

            callStore.connectCall()
        }
    }

    // Reject Call
    rejectCall() {
        const callStore = useCallStore()
        this.sendSignal('reject', null, this.targetId)
        this.cleanup()
        callStore.reset()
    }

    // Hangup
    hangup() {
        const callStore = useCallStore()

        // Notify ALL connected peers
        this.peerConnections.forEach((_, peerId) => {
            this.sendSignal('hangup', null, peerId)
        })

        this.cleanup()
        callStore.endCall()
    }

    // Handle incoming signals
    async handleSignal(data: any) {
        const callStore = useCallStore()
        const { type, payload, senderId, senderUserInfo } = data

        switch (type) {
            case 'offer':
                // If we are already in a call (Group Mode), and this is a new participant
                if (callStore.callState === 'connected' && callStore.callMode === 'group') {
                    // Auto-accept logic for Mesh topology (if we are in the same group context)
                    // For now, assume if we receive an offer while in group call, it's a new peer.
                    console.log('[WebRTC] Received offer from new peer in group call:', senderId)

                    callStore.addParticipant(senderUserInfo, 'connected')

                    await this.createPeerConnection(senderId)
                    const pc = this.peerConnections.get(senderId)
                    if (pc) {
                        await pc.setRemoteDescription(new RTCSessionDescription(payload))
                        const answer = await pc.createAnswer()
                        await pc.setLocalDescription(answer)
                        this.sendSignal('answer', answer, senderId)
                    }
                } else {
                    // Normal incoming call
                    if (senderId && senderUserInfo) {
                        await this.handleIncomingCall(senderId, senderUserInfo, payload, senderUserInfo.callMode || 'p2p')
                    }
                }
                break;

            case 'answer':
                const pc = this.peerConnections.get(senderId)
                if (pc) {
                    await pc.setRemoteDescription(new RTCSessionDescription(payload))
                    // If this was the primary target, set state to connected
                    if (senderId === this.targetId && callStore.callState === 'calling') {
                        callStore.connectCall()
                    }
                    // Update participant status
                    callStore.updateParticipantStatus(senderId, 'connected')
                }
                break

            case 'candidate':
                if (payload) {
                    const pc = this.peerConnections.get(senderId)
                    if (pc && pc.remoteDescription) {
                        await pc.addIceCandidate(new RTCIceCandidate(payload))
                    } else {
                        // Queue it
                        if (!this.candidateQueues.has(senderId)) {
                            this.candidateQueues.set(senderId, [])
                        }
                        this.candidateQueues.get(senderId)?.push(payload)
                    }
                }
                break

            case 'hangup':
                // If P2P, end call. If Group, just remove that peer.
                if (callStore.callMode === 'p2p') {
                    this.cleanup()
                    callStore.endCall()
                } else {
                    this.closePeerConnection(senderId)
                    callStore.removeParticipant(senderId)
                    // If no participants left? End call?
                    if (this.peerConnections.size === 0) {
                        this.cleanup()
                        callStore.endCall()
                    }
                }
                break

            case 'busy':
            case 'reject':
                if (callStore.callMode === 'p2p') {
                    alert('对方忙或拒绝')
                    this.cleanup()
                    callStore.reset()
                } else {
                    // Group mode: just mark as failed or remove
                    callStore.removeParticipant(senderId)
                }
                break
        }
    }

    // --- Internal Helpers ---

    private async initLocalMedia() {
        const callStore = useCallStore()
        if (this.localMediaStream) return

        const constraints = {
            audio: true,
            video: callStore.callType === 'video' ? {
                facingMode: 'user',
                width: { ideal: 640 }, // Lower resolution for group calls
                height: { ideal: 480 }
            } : false
        }

        try {
            this.localMediaStream = await navigator.mediaDevices.getUserMedia(constraints)
            this.localStream.value = this.localMediaStream

            if (callStore.callType === 'video') {
                callStore.isCameraOn = true
            }
            this.updateMediaState()
        } catch (err: any) {
            console.error('Failed to get user media', err)
            throw err
        }
    }

    private async createPeerConnection(targetId: string) {
        if (this.peerConnections.has(targetId)) return

        console.log('[WebRTC] Creating PC for', targetId)
        const pc = new RTCPeerConnection(rtcConfig)

        // Add local tracks
        if (this.localMediaStream) {
            this.localMediaStream.getTracks().forEach(track => {
                pc.addTrack(track, this.localMediaStream!)
            })
        }

        // ICE Candidates
        pc.onicecandidate = (event) => {
            if (event.candidate) {
                this.sendSignal('candidate', event.candidate, targetId)
            }
        }

        // Connection State
        pc.onconnectionstatechange = () => {
            console.log(`[WebRTC] Connection state with ${targetId}:`, pc.connectionState)
            if (pc.connectionState === 'failed' || pc.connectionState === 'disconnected') {
                // Handle disconnection
                // this.closePeerConnection(targetId) // Optional: auto-reconnect logic?
            }
        }

        // Remote Track
        pc.ontrack = (event) => {
            console.log(`[WebRTC] Received remote track from ${targetId}`, event.streams[0])
            const newMap = new Map(this.remoteStreams.value)
            newMap.set(targetId, event.streams[0])
            this.remoteStreams.value = newMap

            // Legacy support
            if (!this.remoteStream.value) {
                this.remoteStream.value = event.streams[0] || null
            }
        }

        this.peerConnections.set(targetId, pc)
    }

    private closePeerConnection(targetId: string) {
        const pc = this.peerConnections.get(targetId)
        if (pc) {
            pc.close()
            this.peerConnections.delete(targetId)
        }

        const newMap = new Map(this.remoteStreams.value)
        newMap.delete(targetId)
        this.remoteStreams.value = newMap

        if (this.remoteStream.value && !newMap.size) {
            this.remoteStream.value = null
        }
    }

    private sendSignal(type: string, payload: any, targetId: string) {
        const callStore = useCallStore()

        // Get current user info
        const userStr = localStorage.getItem('userInfo')
        let rawUserInfo: any = {}
        try { rawUserInfo = JSON.parse(userStr || '{}') } catch (e) { }

        const senderUserInfo = {
            id: rawUserInfo.id || rawUserInfo.uid,
            name: rawUserInfo.realName || rawUserInfo.username || 'Unknown',
            avatar: rawUserInfo.headUrl || rawUserInfo.avatar || 'default',
            callType: callStore.callType,
            callMode: callStore.callMode,
            groupId: callStore.callMode === 'group' ? this.targetId : undefined
        }

        wsService.send({
            type: WSReqType.RTC_SIGNAL,
            userId: targetId,
            data: JSON.stringify({
                type,
                payload,
                senderId: senderUserInfo.id,
                senderUserInfo
            })
        })
    }

    updateMediaState() {
        const callStore = useCallStore()
        if (this.localMediaStream) {
            this.localMediaStream.getAudioTracks().forEach(t => t.enabled = !callStore.isMuted)
            this.localMediaStream.getVideoTracks().forEach(t => t.enabled = callStore.isCameraOn)
        }
    }

    toggleMute() {
        const callStore = useCallStore()
        callStore.isMuted = !callStore.isMuted
        this.updateMediaState()
    }

    toggleVideo() {
        const callStore = useCallStore()
        callStore.isCameraOn = !callStore.isCameraOn
        this.updateMediaState()
    }

    async switchCamera() {
        // ... (Keep existing switchCamera logic, but apply to ALL peer connections)
        // For simplicity, just replacing track in local stream and then replacing sender track in all PCs
        const callStore = useCallStore()
        if (callStore.callType !== 'video' || !this.localMediaStream) return

        this.localMediaStream.getVideoTracks().forEach(t => t.stop())
        callStore.isFrontCamera = !callStore.isFrontCamera

        try {
            const newStream = await navigator.mediaDevices.getUserMedia({
                audio: true,
                video: { facingMode: callStore.isFrontCamera ? 'user' : 'environment' }
            })

            const videoTrack = newStream.getVideoTracks()[0]
            this.localMediaStream = newStream
            this.localStream.value = newStream

            // Replace track in all PCs
            this.peerConnections.forEach(pc => {
                const sender = pc.getSenders().find(s => s.track?.kind === 'video')
                if (sender && videoTrack) sender.replaceTrack(videoTrack)
            })

            this.updateMediaState()
        } catch (e) {
            console.error('Switch camera failed', e)
        }
    }

    public connectionState = shallowRef<string>('connected') // Simplified for group

    private cleanup() {
        if (this.localMediaStream) {
            this.localMediaStream.getTracks().forEach(t => t.stop())
            this.localMediaStream = null
        }
        this.peerConnections.forEach(pc => pc.close())
        this.peerConnections.clear()

        this.remoteStreams.value = new Map()
        this.remoteStream.value = null
        this.localStream.value = null
        this.pendingOffers.clear()
        this.candidateQueues.clear()
    }
}

export default new WebRTCService()
