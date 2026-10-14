<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref } from 'vue'

const visible = ref(false)
function toTop() {
  window.scrollTo({ top: 0, behavior: 'smooth' })
}
const onScroll = () => (visible.value = window.scrollY > 400)

onMounted(() => window.addEventListener('scroll', onScroll, { passive: true }))
onBeforeUnmount(() => window.removeEventListener('scroll', onScroll))
</script>

<template>
  <button
      v-show="visible"
      class="fixed right-5 bottom-5 z-40 h-11 w-11 rounded-full bg-ink text-white shadow-lg transition hover:bg-blush-700"
      aria-label="Back to top"
      @click="toTop"
  >
    ↑
  </button>
</template>
