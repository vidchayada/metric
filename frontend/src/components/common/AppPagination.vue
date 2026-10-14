<script setup lang="ts">
import { computed } from 'vue'
import { getPageNumbers } from '@/utils/pagination'

const props = defineProps<{ page: number; totalPages: number }>()
const emit = defineEmits<{ change: [page: number] }>()

const tokens = computed(() => getPageNumbers(props.page, props.totalPages))
const base = 'h-9 min-w-9 rounded-full px-3 text-sm transition disabled:opacity-30'
</script>

<template>
  <nav v-if="totalPages > 1" class="mt-12 flex flex-wrap items-center justify-center gap-1.5" aria-label="Pagination">
    <button :class="[base, 'hover:bg-blush-100']" :disabled="page === 1" aria-label="Previous page" @click="emit('change', page - 1)">←</button>
    <template v-for="(t, i) in tokens" :key="i">
      <span v-if="t === '...'" class="px-1 text-muted">…</span>
      <button
          v-else
          :class="[base, t === page ? 'bg-ink text-white' : 'hover:bg-blush-100']"
          :aria-current="t === page ? 'page' : undefined"
          @click="emit('change', t)"
      >
        {{ t }}
      </button>
    </template>
    <button :class="[base, 'hover:bg-blush-100']" :disabled="page === totalPages" aria-label="Next page" @click="emit('change', page + 1)">→</button>
  </nav>
</template>
