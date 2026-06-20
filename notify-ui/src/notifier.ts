import { createApp, h, ref } from 'vue'
import UpdatePopup from './components/UpdatePopup.vue'
import type { NotifierOptions, UpdateData } from './types'
import { fetchUnread, reportRead } from './api'

/**
 * 一行初始化：启动即拉取未读公告 → 依次弹窗 → 阅读后自动上报已读。
 * 返回控制器，可手动 refresh / destroy。
 */
export function initUpdateNotifier(options: NotifierOptions) {
  const container = document.createElement('div')
  document.body.appendChild(container)

  const queue = ref<UpdateData[]>([])
  const current = ref<UpdateData | null>(null)

  function next() {
    current.value = queue.value.shift() || null
  }

  async function onRead(id?: number) {
    if (id != null) {
      try {
        await reportRead(options, id)
      } catch (e) {
        console.warn('[notify-ui] 已读上报失败', e)
      }
    }
    next()
  }

  const app = createApp({
    setup() {
      return () =>
        current.value
          ? h(UpdatePopup, {
              data: current.value,
              forceRead: current.value.type === 'PRE_NOTICE',
              onRead,
              onClose: next,
            })
          : null
    },
  })
  app.mount(container)

  async function refresh() {
    try {
      const list = await fetchUnread(options)
      queue.value = list
      if (!current.value) next()
    } catch (e) {
      console.warn('[notify-ui] 拉取未读失败', e)
    }
  }

  refresh()

  return {
    refresh,
    destroy() {
      app.unmount()
      container.remove()
    },
  }
}
