import type { App } from 'vue'
import UpdatePopup from './components/UpdatePopup.vue'
import ChangelogPage from './components/ChangelogPage.vue'
import { initUpdateNotifier } from './notifier'

export { UpdatePopup, ChangelogPage, initUpdateNotifier }
export * from './types'
export { fetchUnread, reportRead, fetchChangelog } from './api'

/** Vue 插件式安装，全局注册组件。 */
export default {
  install(app: App) {
    app.component('UpdatePopup', UpdatePopup)
    app.component('ChangelogPage', ChangelogPage)
  },
}
