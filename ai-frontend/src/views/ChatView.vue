<script setup>
import { nextTick, onMounted, onUnmounted, ref } from 'vue'
import { doChatWithLawAppSse } from '../api/lawChat'
import { createChatId } from '../utils/chatId'

const chatId = ref('')
const inputText = ref('')
const sending = ref(false)
const messages = ref([])
const listRef = ref(null)

let abortController = null
let typeQueue = ''
let typingTimer = null
let streamEnded = false
let activeAiId = ''

onMounted(() => {
  chatId.value = createChatId()
  messages.value.push({
    id: `welcome-${chatId.value}`,
    role: 'ai',
    content: '您好，我是 AI 法务顾问。请描述您的法律问题，我会基于会话为您提供咨询建议。',
    streaming: false,
  })
})

onUnmounted(() => {
  abortController?.abort()
  stopTypewriter()
})

async function scrollToBottom() {
  await nextTick()
  const el = listRef.value
  if (el) {
    el.scrollTop = el.scrollHeight
  }
}

function updateMessage(id, patch) {
  messages.value = messages.value.map((item) => {
    if (item.id !== id) {
      return item
    }
    return { ...item, ...patch }
  })
}

function appendToMessage(id, chunk) {
  messages.value = messages.value.map((item) => {
    if (item.id !== id) {
      return item
    }
    return { ...item, content: `${item.content}${chunk}` }
  })
}

function stopTypewriter() {
  if (typingTimer) {
    clearTimeout(typingTimer)
    typingTimer = null
  }
}

function finishAiMessage() {
  stopTypewriter()
  const current = messages.value.find((item) => item.id === activeAiId)
  if (current && !current.content) {
    updateMessage(activeAiId, {
      content: '未收到模型回复，请稍后重试。',
      streaming: false,
    })
  } else if (activeAiId) {
    updateMessage(activeAiId, { streaming: false })
  }
  sending.value = false
  activeAiId = ''
  typeQueue = ''
  streamEnded = false
  scrollToBottom()
}

function pumpTypewriter() {
  if (typingTimer) {
    return
  }

  const tick = () => {
    typingTimer = null
    if (!typeQueue) {
      if (streamEnded) {
        finishAiMessage()
      }
      return
    }

    const takeSize = Math.min(2, typeQueue.length)
    const piece = typeQueue.slice(0, takeSize)
    typeQueue = typeQueue.slice(takeSize)
    appendToMessage(activeAiId, piece)
    scrollToBottom()
    typingTimer = window.setTimeout(tick, 18)
  }

  tick()
}

function enqueueChunk(chunk) {
  typeQueue += chunk
  pumpTypewriter()
}

async function sendMessage() {
  const message = inputText.value.trim()
  if (!message || sending.value || !chatId.value) {
    return
  }

  sending.value = true
  inputText.value = ''
  streamEnded = false
  typeQueue = ''
  stopTypewriter()

  messages.value.push({
    id: `user-${Date.now()}`,
    role: 'user',
    content: message,
    streaming: false,
  })

  const aiMessageId = `ai-${Date.now()}`
  activeAiId = aiMessageId
  messages.value.push({
    id: aiMessageId,
    role: 'ai',
    content: '',
    streaming: true,
  })
  await scrollToBottom()

  abortController?.abort()
  abortController = new AbortController()

  await doChatWithLawAppSse({
    message,
    chatId: chatId.value,
    signal: abortController.signal,
    onMessage: (chunk) => {
      enqueueChunk(chunk)
    },
    onError: (error) => {
      stopTypewriter()
      const detail = error?.message || '未知错误'
      const current = messages.value.find((item) => item.id === aiMessageId)
      updateMessage(aiMessageId, {
        content: current?.content || `请求失败：${detail}`,
        streaming: false,
      })
      sending.value = false
      activeAiId = ''
      typeQueue = ''
      streamEnded = false
      scrollToBottom()
    },
    onComplete: () => {
      streamEnded = true
      if (!typeQueue) {
        finishAiMessage()
      }
    },
  })
}

function onKeydown(event) {
  if (event.key === 'Enter' && !event.shiftKey) {
    event.preventDefault()
    sendMessage()
  }
}
</script>

<template>
  <div class="page">
    <header class="header">
      <div>
        <h1>AI 法务咨询</h1>
        <p>基于会话的实时法律咨询助手</p>
      </div>
      <div class="chat-id">会话 ID：{{ chatId || '生成中...' }}</div>
    </header>

    <main ref="listRef" class="chat-list">
      <div
        v-for="item in messages"
        :key="item.id"
        class="row"
        :class="item.role === 'user' ? 'row-user' : 'row-ai'"
      >
        <div class="avatar">{{ item.role === 'user' ? '我' : 'AI' }}</div>
        <div class="bubble" :class="item.role">
          <span>{{ item.content }}</span>
          <span v-if="item.streaming" class="cursor">|</span>
        </div>
      </div>
    </main>

    <footer class="composer">
      <textarea
        v-model="inputText"
        :disabled="sending"
        rows="3"
        placeholder="请输入法律问题，Enter 发送，Shift + Enter 换行"
        @keydown="onKeydown"
      />
      <button :disabled="sending || !inputText.trim()" @click="sendMessage">
        {{ sending ? '回复中...' : '发送' }}
      </button>
    </footer>
  </div>
</template>

<style scoped>
.page {
  height: 100%;
  display: flex;
  flex-direction: column;
  background:
    linear-gradient(180deg, rgba(15, 46, 89, 0.08), transparent 180px),
    #eef3f8;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 18px 24px;
  border-bottom: 1px solid #d7e1ec;
  background: #0f2e59;
  color: #fff;
}

.header h1 {
  margin: 0;
  font-size: 22px;
}

.header p {
  margin: 4px 0 0;
  font-size: 13px;
  color: #c9d7ea;
}

.chat-id {
  font-size: 12px;
  background: rgba(255, 255, 255, 0.12);
  padding: 8px 12px;
  border-radius: 999px;
}

.chat-list {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.row {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  max-width: 78%;
}

.row-ai {
  align-self: flex-start;
}

.row-user {
  align-self: flex-end;
  flex-direction: row-reverse;
}

.avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: grid;
  place-items: center;
  font-size: 12px;
  font-weight: 700;
  flex-shrink: 0;
}

.row-ai .avatar {
  background: #163a6b;
  color: #fff;
}

.row-user .avatar {
  background: #c9a227;
  color: #1c2430;
}

.bubble {
  padding: 12px 14px;
  border-radius: 14px;
  white-space: pre-wrap;
  word-break: break-word;
  line-height: 1.6;
  box-shadow: 0 6px 18px rgba(16, 36, 64, 0.06);
}

.bubble.ai {
  background: #fff;
  color: #223044;
  border-top-left-radius: 4px;
  min-height: 24px;
}

.bubble.user {
  background: #1d4f91;
  color: #fff;
  border-top-right-radius: 4px;
}

.cursor {
  display: inline-block;
  margin-left: 2px;
  animation: blink 1s step-end infinite;
}

.composer {
  display: flex;
  gap: 12px;
  padding: 16px 24px 20px;
  background: #fff;
  border-top: 1px solid #d7e1ec;
}

textarea {
  flex: 1;
  resize: none;
  border: 1px solid #cdd8e6;
  border-radius: 12px;
  padding: 12px;
  font-size: 14px;
  outline: none;
}

textarea:focus {
  border-color: #1d4f91;
}

button {
  width: 108px;
  border: 0;
  border-radius: 12px;
  background: #0f2e59;
  color: #fff;
  font-size: 15px;
  cursor: pointer;
}

button:disabled {
  opacity: 0.55;
  cursor: not-allowed;
}

@keyframes blink {
  50% {
    opacity: 0;
  }
}
</style>
