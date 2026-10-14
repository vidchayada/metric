<script setup lang="ts">
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useCartStore } from '@/stores/cart'
import { statusOf } from '@/api/client'

const auth = useAuthStore()
const cart = useCartStore()
const route = useRoute()
const router = useRouter()

const username = ref('')
const password = ref('')
const error = ref<string | null>(null)
const submitting = ref(false)

/** Only follow same-site paths, never an absolute URL. */
function safeReturnTo(): string {
  const target = route.query.returnTo
  return typeof target === 'string' && target.startsWith('/') && !target.startsWith('//') ? target : '/'
}

async function submit() {
  submitting.value = true
  error.value = null
  try {
    const user = await auth.login(username.value.trim(), password.value)

    const role = (user.role ?? '').toUpperCase()
    if (role === 'ADMIN' || auth.isAdmin) {
      await router.replace('/admin/dashboard')
      return
    }

    cart.loadCount()
    await router.replace(safeReturnTo())
  } catch (e) {
    error.value = statusOf(e) === 401 ? 'Incorrect username or password.' : 'Something went wrong.'
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <form class="space-y-4" @submit.prevent="submit">
    <p v-if="route.query.registered" class="rounded-lg bg-emerald-50 px-3 py-2 text-sm text-emerald-800" role="status">Account created! Please log in.</p>
    <p v-if="error" class="rounded-lg bg-red-50 px-3 py-2 text-sm text-red-700" role="alert">{{ error }}</p>

    <div>
      <label for="username" class="field-label">Username</label>
      <input id="username" v-model="username" type="text" required autocomplete="username" class="field" />
    </div>
    <div>
      <label for="password" class="field-label">Password</label>
      <input id="password" v-model="password" type="password" required autocomplete="current-password" class="field" />
    </div>

    <div class="flex gap-3 pt-2">
      <RouterLink :to="{ name: 'register' }" class="btn-outline flex-1">Sign up</RouterLink>
      <button type="submit" class="btn flex-1" :disabled="submitting">Log in</button>
    </div>
  </form>
</template>
