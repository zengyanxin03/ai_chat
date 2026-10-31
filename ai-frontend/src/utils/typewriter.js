export function createTypewriter({ append, onTick, interval = 18 }) {
  let queue = ''
  let timer = null
  let finished = false
  let resolveIdle

  function notifyIdle() {
    if (resolveIdle) {
      resolveIdle()
      resolveIdle = null
    }
  }

  function takeSize() {
    if (queue.length > 24) return Math.ceil(queue.length / 3)
    if (queue.length > 8) return 2
    return 1
  }

  function pump() {
    timer = null
    if (!queue) {
      notifyIdle()
      return
    }

    const size = takeSize()
    const piece = queue.slice(0, size)
    queue = queue.slice(size)
    append(piece)
    onTick?.()
    timer = window.setTimeout(pump, interval)
  }

  function start() {
    if (!timer) {
      timer = window.setTimeout(pump, interval)
    }
  }

  return {
    push(text) {
      if (!text) return
      queue += text
      start()
    },
    flush() {
      finished = true
      if (!queue && !timer) {
        return Promise.resolve()
      }
      return new Promise((resolve) => {
        resolveIdle = resolve
        start()
      }).finally(() => {
        finished = true
      })
    },
    stop() {
      if (timer) {
        window.clearTimeout(timer)
        timer = null
      }
      if (queue) {
        append(queue)
        queue = ''
        onTick?.()
      }
      notifyIdle()
      return finished
    },
  }
}
