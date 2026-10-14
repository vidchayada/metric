<script setup lang="ts">
defineProps<{ open: boolean; title?: string }>()
const emit = defineEmits<{ close: [] }>()
</script>

<template>
  <Teleport to="body">
    <div
        v-if="open"
        class="fixed inset-0 z-50 flex items-center justify-center bg-ink/40 p-4"
        @click.self="emit('close')"
        @keydown.esc="emit('close')"
    >
      <div role="dialog" aria-modal="true" class="max-h-[90vh] w-full max-w-lg overflow-y-auto rounded-2xl bg-white p-6 shadow-xl">
        <h2 v-if="title" class="mb-4 font-serif text-2xl">{{ title }}</h2>
        <slot />
        <div v-if="$slots.actions" class="mt-6 flex justify-end gap-3"><slot name="actions" /></div>
      </div>
    </div>
  </Teleport>
</template>






