<template>
  <div v-if="visible" class="nu-overlay" @click.self="onMaskClick">
    <div class="nu-popup">
      <div class="nu-hd" :class="data.type === 'PRE_NOTICE' ? 'notice' : 'update'">
        <span v-if="!forceRead" class="nu-close" @click="close">✕</span>
        <div class="nu-kicker">{{ data.type === 'PRE_NOTICE' ? '⏰ 升级预告' : '🎉 已升级' }}</div>
        <h3 class="nu-title">{{ data.title }}</h3>
        <div class="nu-sub" v-if="data.versionNo">{{ data.versionNo }}<span v-if="data.releaseTime"> · {{ formatTime(data.releaseTime) }}</span></div>
      </div>
      <div class="nu-bd">
        <div v-if="data.description" class="nu-desc">{{ data.description }}</div>
        <div v-for="(grp, key) in grouped" :key="key" class="nu-group">
          <div v-for="(it, i) in grp" :key="i" class="nu-item">
            <span class="nu-tag" :class="tagClass(it.category)">{{ catDesc(it.category) }}</span>
            <div>
              <div class="nu-it-title">{{ it.title }}</div>
              <div class="nu-it-desc" v-if="it.content">{{ it.content }}</div>
            </div>
          </div>
        </div>
      </div>
      <div class="nu-ft">
        <span v-if="forceRead" class="nu-hint">强提醒 · 阅读后上报已读</span>
        <span class="nu-spacer"></span>
        <a v-if="data.jumpUrl" class="nu-link" :href="data.jumpUrl">查看完整日志</a>
        <button class="nu-btn" @click="confirm">{{ data.type === 'PRE_NOTICE' ? '知道了，已安排' : '我知道了' }}</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import type { ChangeCategory, UpdateData } from '../types'

const props = withDefaults(defineProps<{
  data: UpdateData
  forceRead?: boolean
  modelValue?: boolean
}>(), { forceRead: false, modelValue: true })

const emit = defineEmits<{
  (e: 'read', id?: number): void
  (e: 'close'): void
  (e: 'update:modelValue', v: boolean): void
}>()

const visible = ref(props.modelValue)

const order: ChangeCategory[] = ['ADD', 'OPTIMIZE', 'FIX']
const grouped = computed(() => {
  const map: Record<string, UpdateData['items']> = {}
  for (const c of order) {
    const arr = (props.data.items || []).filter((i) => i.category === c)
    if (arr.length) map[c] = arr
  }
  return map
})

function catDesc(c: ChangeCategory) {
  return c === 'ADD' ? '＋ 新增' : c === 'OPTIMIZE' ? '⤴ 优化' : '🔧 修复'
}
function tagClass(c: ChangeCategory) {
  return c === 'ADD' ? 'add' : c === 'OPTIMIZE' ? 'opt' : 'fix'
}
function formatTime(t: string) {
  return (t || '').replace('T', ' ').slice(0, 16)
}
function onMaskClick() {
  if (!props.forceRead) close()
}
function close() {
  visible.value = false
  emit('update:modelValue', false)
  emit('close')
}
/** 点击确认即视为已读，自动触发已读上报回调。 */
function confirm() {
  emit('read', props.data.announcementId)
  close()
}
</script>

<style scoped>
.nu-overlay { position: fixed; inset: 0; background: rgba(20,26,43,.45); display: grid; place-items: center; z-index: 9999; }
.nu-popup { width: 460px; max-width: 92%; background: #fff; border-radius: 16px; overflow: hidden; box-shadow: 0 18px 50px rgba(31,37,51,.3); font-family: -apple-system, BlinkMacSystemFont, "PingFang SC", "Microsoft YaHei", sans-serif; }
.nu-hd { padding: 20px 22px; color: #fff; position: relative; }
.nu-hd.update { background: linear-gradient(135deg,#3b5bfd,#7b5bff); }
.nu-hd.notice { background: linear-gradient(135deg,#ff9f1c,#ff7a59); }
.nu-kicker { font-size: 12px; opacity: .9; }
.nu-title { margin: 6px 0 4px; font-size: 19px; }
.nu-sub { font-size: 12.5px; opacity: .92; }
.nu-close { position: absolute; right: 16px; top: 16px; cursor: pointer; opacity: .85; }
.nu-bd { padding: 16px 22px; max-height: 320px; overflow: auto; }
.nu-desc { background: #f5f7ff; border-radius: 10px; padding: 10px 12px; font-size: 13px; color: #5a6273; margin-bottom: 8px; }
.nu-item { display: flex; gap: 10px; padding: 8px 0; border-bottom: 1px dashed #e9ecf2; }
.nu-item:last-child { border-bottom: none; }
.nu-tag { flex-shrink: 0; height: fit-content; padding: 2px 9px; border-radius: 20px; font-size: 12px; }
.nu-tag.add { background: #e6f8f0; color: #138a59; }
.nu-tag.opt { background: #eef1ff; color: #2c45d6; }
.nu-tag.fix { background: #fff4e3; color: #b9710b; }
.nu-it-title { font-size: 13.5px; color: #1f2533; }
.nu-it-desc { font-size: 12.5px; color: #99a0b0; }
.nu-ft { padding: 14px 22px; border-top: 1px solid #e9ecf2; display: flex; align-items: center; gap: 10px; }
.nu-hint { font-size: 12px; color: #99a0b0; }
.nu-spacer { flex: 1; }
.nu-link { font-size: 13px; color: #3b5bfd; text-decoration: none; }
.nu-btn { background: #3b5bfd; color: #fff; border: none; padding: 8px 16px; border-radius: 9px; font-size: 13.5px; cursor: pointer; }
.nu-btn:hover { background: #2c45d6; }
</style>
