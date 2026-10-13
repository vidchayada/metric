<script setup lang="ts">
import { useToastStore } from '@/stores/toast'

const toast = useToastStore()
const tone = { success: 'bg-ink text-white', error: 'bg-red-600 text-white', info: 'bg-white text-ink ring-1 ring-line' }
</script>

<template>
  <div class="pointer-events-none fixed inset-x-0 bottom-6 z-[60] flex flex-col items-center gap-2 px-4" aria-live="polite">
    <div
        v-for="t in toast.toasts"
        :key="t.id"
        :class="['pointer-events-auto rounded-full px-5 py-2.5 text-sm shadow-lg', tone[t.kind]]"
        role="status"
        @click="toast.dismiss(t.id)"
    >
      {{ t.message }}
    </div>
  </div>
</template>
