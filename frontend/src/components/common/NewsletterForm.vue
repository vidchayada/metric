<script setup lang="ts">
import { ref } from 'vue'
import { contactApi } from '@/api/shop'
import { apiError } from '@/api/client'
import { useToastStore } from '@/stores/toast'

const email = ref('')
const submitting = ref(false)
const toast = useToastStore()

async function subscribe() {
  submitting.value = true
  try {
    const res = await contactApi.subscribe(email.value.trim())
    toast.success(res.message || 'Thank you for subscribing!')
    email.value = ''
  } catch (e) {
    toast.error(apiError(e))
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <form class="space-y-3" @submit.prevent="subscribe">
    <input v-model="email" type="email" required placeholder="Enter your email address" class="field" aria-label="Email address" />
    <p class="text-xs text-muted">
      By signing up, you agree to our <a href="#" class="underline">Privacy Policy</a> and
      <a href="#" class="underline">Terms of Service</a>.
    </p>
    <button type="submit" class="btn" :disabled="submitting">Subscribe</button>
  </form>
</template>
