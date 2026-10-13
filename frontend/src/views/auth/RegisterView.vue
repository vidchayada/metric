<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { authApi } from '@/api/shop'
import { fieldErrors, statusOf } from '@/api/client'
import type { FieldErrors } from '@/types'

const router = useRouter()
const form = reactive({ username: '', email: '', password: '', terms: false })
const errors = ref<FieldErrors>({})
const showPassword = ref(false)
const submitting = ref(false)

function validate(): boolean {
  const e: FieldErrors = {}
  if (!form.username.trim()) e.username = 'Username is required'
  if (!form.email.trim()) e.email = 'Email is required'
  else if (!/^\S+@\S+\.\S+$/.test(form.email)) e.email = 'Enter a valid email'
  if (!form.password) e.password = 'Password is required'
  else if (form.password.length < 8) e.password = 'Password must be at least 8 characters'
  if (!form.terms) e.general = 'Please accept the terms and privacy policy'
  errors.value = e
  return Object.keys(e).length === 0
}

async function submit() {
  if (!validate()) return
  submitting.value = true
  try {
    await authApi.register({ username: form.username.trim(), email: form.email.trim(), password: form.password })
    router.push({ name: 'login', query: { registered: '1' } })
  } catch (e) {
    errors.value =
        statusOf(e) === 409 ? (fieldErrors(e) as FieldErrors) : { general: 'Something went wrong. Please try again.' }
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <form class="space-y-4" novalidate @submit.prevent="submit">
    <h1 class="text-center font-serif text-2xl">Create account</h1>
    <p v-if="errors.general" class="rounded-lg bg-red-50 px-3 py-2 text-sm text-red-700" role="alert">{{ errors.general }}</p>

    <div>
      <label for="username" class="field-label">Username</label>
      <input id="username" v-model="form.username" type="text" placeholder="Your username" autocomplete="username" class="field" />
      <p v-if="errors.username" class="field-error">{{ errors.username }}</p>
    </div>
    <div>
      <label for="email" class="field-label">Email</label>
      <input id="email" v-model="form.email" type="email" placeholder="Your email" autocomplete="email" class="field" />
      <p v-if="errors.email" class="field-error">{{ errors.email }}</p>
    </div>
    <div>
      <label for="password" class="field-label">Password</label>
      <div class="relative">
        <input id="password" v-model="form.password" :type="showPassword ? 'text' : 'password'" placeholder="At least 8 characters" autocomplete="new-password" class="field pr-12" />
        <button type="button" class="absolute inset-y-0 right-3 text-xs text-muted" :aria-label="showPassword ? 'Hide password' : 'Show password'" @click="showPassword = !showPassword">
          {{ showPassword ? 'Hide' : 'Show' }}
        </button>
      </div>
      <p v-if="errors.password" class="field-error">{{ errors.password }}</p>
    </div>

    <label class="flex items-center gap-2 text-sm text-muted">
      <input v-model="form.terms" type="checkbox" class="h-4 w-4 accent-blush-700" />
      I accept the terms and privacy policy
    </label>

    <button type="submit" class="btn w-full" :disabled="submitting">Sign up</button>
    <p class="text-center text-sm text-muted">Already have an account? <RouterLink :to="{ name: 'login' }" class="text-ink underline">Log in</RouterLink></p>
  </form>
</template>
