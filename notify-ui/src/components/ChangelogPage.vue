<template>
  <div class="nu-cl">
    <div class="nu-cl-hero">
      <h1>{{ title }}</h1>
      <p>了解每个版本带来的新功能、优化与修复</p>
    </div>

    <div class="nu-filters">
      <input class="nu-search" v-model="keyword" placeholder="🔍 搜索更新内容" />
      <span class="nu-spacer"></span>
      <button v-for="c in cats" :key="c.v" class="nu-chip" :class="{ on: category === c.v }" @click="selectCat(c.v)">
        {{ c.label }}
      </button>
    </div>

    <div class="nu-timeline">
      <div v-for="(v, i) in filteredList" :key="i" class="nu-tl-item">
        <div class="nu-tl-head">
          <span class="nu-ver">{{ v.versionNo }}</span>
          <span class="nu-date">{{ formatTime(v.planReleaseTime) }}</span>
          <span v-if="i === 0 && page === 1" class="nu-latest">最新</span>
        </div>
        <div v-if="v.description" class="nu-desc">{{ v.description }}</div>
        <div v-for="(it, j) in v.items" :key="j" class="nu-item">
          <span class="nu-tag" :class="tagClass(it.category)">{{ catDesc(it.category) }}</span>
          <span>{{ it.title }}</span>
        </div>
      </div>
      <div v-if="!filteredList.length" class="nu-empty">暂无更新记录</div>
    </div>

    <div class="nu-pager">
      <button class="nu-btn ghost" :disabled="page <= 1" @click="go(page - 1)">上一页</button>
      <span>第 {{ page }} 页</span>
      <button class="nu-btn ghost" :disabled="page * size >= total" @click="go(page + 1)">下一页</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import type { ChangeCategory, NotifierOptions } from '../types'
import { fetchChangelog } from '../api'

const props = withDefaults(defineProps<{
  options: NotifierOptions
  title?: string
  size?: number
}>(), { title: '更新日志', size: 10 })

const list = ref<any[]>([])
const total = ref(0)
const page = ref(1)
const category = ref<ChangeCategory | ''>('')
const keyword = ref('')

const cats = [
  { v: '', label: '全部' },
  { v: 'ADD', label: '新增' },
  { v: 'OPTIMIZE', label: '优化' },
  { v: 'FIX', label: '修复' },
] as const

const filteredList = computed(() => {
  if (!keyword.value) return list.value
  const k = keyword.value.toLowerCase()
  return list.value
    .map((v) => ({ ...v, items: (v.items || []).filter((it: any) => it.title.toLowerCase().includes(k)) }))
    .filter((v) => v.items.length || (v.description || '').toLowerCase().includes(k))
})

function catDesc(c: ChangeCategory) {
  return c === 'ADD' ? '＋ 新增' : c === 'OPTIMIZE' ? '⤴ 优化' : '🔧 修复'
}
function tagClass(c: ChangeCategory) {
  return c === 'ADD' ? 'add' : c === 'OPTIMIZE' ? 'opt' : 'fix'
}
function formatTime(t?: string) {
  return (t || '').replace('T', ' ').slice(0, 10)
}

async function load() {
  const data: any = await fetchChangelog(props.options, page.value, props.size, category.value || undefined)
  list.value = data?.records || []
  total.value = data?.total || 0
}
function selectCat(v: ChangeCategory | '') {
  category.value = v
  page.value = 1
  load()
}
function go(p: number) {
  page.value = p
  load()
}

onMounted(load)
</script>

<style scoped>
.nu-cl { max-width: 820px; margin: 0 auto; font-family: -apple-system, BlinkMacSystemFont, "PingFang SC", sans-serif; }
.nu-cl-hero { background: linear-gradient(135deg,#3b5bfd,#7b5bff); color: #fff; border-radius: 16px; padding: 24px 26px; margin-bottom: 18px; }
.nu-cl-hero h1 { margin: 0 0 6px; font-size: 22px; }
.nu-cl-hero p { margin: 0; opacity: .9; font-size: 13px; }
.nu-filters { display: flex; align-items: center; gap: 8px; margin-bottom: 16px; }
.nu-search { padding: 8px 12px; border: 1px solid #e9ecf2; border-radius: 9px; width: 260px; outline: none; }
.nu-spacer { flex: 1; }
.nu-chip { border: 1px solid #e9ecf2; background: #fff; border-radius: 20px; padding: 5px 12px; cursor: pointer; font-size: 12.5px; }
.nu-chip.on { background: #eef1ff; border-color: #c2cdff; color: #2c45d6; }
.nu-timeline { border-left: 2px solid #e9ecf2; padding-left: 20px; }
.nu-tl-item { margin-bottom: 22px; }
.nu-tl-head { display: flex; align-items: center; gap: 8px; }
.nu-ver { font-weight: 700; font-size: 16px; }
.nu-date { color: #99a0b0; font-size: 12.5px; }
.nu-latest { background: #eef1ff; color: #2c45d6; border-radius: 20px; padding: 0 8px; font-size: 11px; }
.nu-desc { color: #5a6273; font-size: 13px; margin: 8px 0; }
.nu-item { display: flex; gap: 10px; padding: 6px 0; align-items: center; }
.nu-tag { padding: 2px 9px; border-radius: 20px; font-size: 12px; }
.nu-tag.add { background: #e6f8f0; color: #138a59; }
.nu-tag.opt { background: #eef1ff; color: #2c45d6; }
.nu-tag.fix { background: #fff4e3; color: #b9710b; }
.nu-empty { color: #99a0b0; text-align: center; padding: 30px 0; }
.nu-pager { display: flex; align-items: center; justify-content: center; gap: 12px; margin-top: 16px; }
.nu-btn { background: #3b5bfd; color: #fff; border: none; padding: 7px 14px; border-radius: 9px; cursor: pointer; }
.nu-btn.ghost { background: #fff; color: #5a6273; border: 1px solid #e9ecf2; }
.nu-btn:disabled { opacity: .5; cursor: not-allowed; }
</style>
