import request from './request'

/**
 * SSE 流式调用 AI 法务咨询应用
 * 对应后端：GET /ai/law_app/chat/sse
 *
 * 使用 Fetch 读取 ReadableStream，在每个 SSE event 到达时立刻回调，
 * 这样前端可以边收边在同一个气泡里拼接内容。
 * Axios 仍用于拼接带 baseURL / query 的请求地址。
 */
export async function doChatWithLawAppSse({
  message,
  chatId,
  onMessage,
  onError,
  onComplete,
  signal,
}) {
  try {
    const url = request.getUri({
      method: 'get',
      url: '/ai/law_app/chat/sse',
      params: { message, chatId },
    })

    const response = await fetch(url, {
      method: 'GET',
      headers: {
        Accept: 'text/event-stream',
        'Cache-Control': 'no-cache',
      },
      signal,
    })

    if (!response.ok) {
      throw new Error(`SSE 请求失败：${response.status}`)
    }
    if (!response.body) {
      throw new Error('当前环境不支持流式读取响应')
    }

    const reader = response.body.getReader()
    const decoder = new TextDecoder('utf-8')
    let buffer = ''

    const emitLine = (rawLine) => {
      const line = rawLine.replace(/\r$/, '')
      if (!line || line.startsWith(':') || line.startsWith('event:') || line.startsWith('id:') || line.startsWith('retry:')) {
        return
      }

      let chunk = line
      if (line.startsWith('data:')) {
        chunk = line.slice(5)
        if (chunk.startsWith(' ')) {
          chunk = chunk.slice(1)
        }
      }

      if (chunk) {
        onMessage?.(chunk)
      }
    }

    const consume = (flushAll = false) => {
      const parts = buffer.split('\n')
      if (!flushAll) {
        buffer = parts.pop() ?? ''
      } else {
        buffer = ''
      }

      for (const line of parts) {
        emitLine(line)
      }
    }

    while (true) {
      const { done, value } = await reader.read()
      if (done) {
        buffer += decoder.decode()
        consume(true)
        if (buffer) {
          emitLine(buffer)
        }
        onComplete?.()
        break
      }

      buffer += decoder.decode(value, { stream: true })
      consume(false)
    }
  } catch (error) {
    if (error?.name === 'AbortError') {
      return
    }
    onError?.(error)
  }
}
