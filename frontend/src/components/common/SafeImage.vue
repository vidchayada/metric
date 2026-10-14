<script setup lang="ts">
import { ref, watch } from 'vue'

const FALLBACK =
    "data:image/svg+xml;utf8,<svg xmlns='http://www.w3.org/2000/svg' width='600' height='800' viewBox='0 0 600 800'><rect width='600' height='800' fill='%23f9eaed'/><text x='300' y='410' font-family='Georgia,serif' font-size='56' fill='%23d98c9c' text-anchor='middle'>✿</text></svg>"

const props = defineProps<{ src: string | null | undefined; alt: string }>()
const current = ref(props.src || FALLBACK)

watch(
    () => props.src,
    (value) => (current.value = value || FALLBACK),
)
</script>

<template>
  <img :src="current" :alt="alt" loading="lazy" @error="current = FALLBACK" />
</template>
