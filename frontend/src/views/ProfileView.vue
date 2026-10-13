<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { userApi } from '@/api/shop'
import { apiError } from '@/api/client'
import { useAuthStore } from '@/stores/auth'
import { useCartStore } from '@/stores/cart'
import { useToastStore } from '@/stores/toast'
import SafeImage from '@/components/common/SafeImage.vue'
import { formatLongDate } from '@/utils/format'
import type { ProfileForm, User } from '@/types'

const MAX_AVATAR_BYTES = 5 * 1024 * 1024
const ALLOWED_TYPES = ['image/jpeg', 'image/png', 'image/webp']

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const cart = useCartStore()
const toast = useToastStore()

const editing = computed(() => route.query.edit === '1')
const error = ref<string | null>(null)
const saving = ref(false)
const form = reactive<ProfileForm>({ fullName: '', username: '', email: '', phone: '', dateOfBirth: '', address: '' })

function fill(user: User | null) {
  if (!user) return
  Object.assign(form, {
    fullName: user.fullName ?? '',
    username: user.username,
    email: user.email,
    phone: user.phone ?? '',
    dateOfBirth: user.dateOfBirth?.slice(0, 10) ?? '',
    address: user.address ?? '',
  })
}
watch(() => auth.user, fill, { immediate: true })

const startEdit = () => router.push({ query: { edit: '1' } })
function cancel() {
  fill(auth.user)
  error.value = null
  router.push({ query: {} })
}

async function save() {
  saving.value = true
  error.value = null
  try {
    auth.setUser(await userApi.update({ ...form }))
    toast.success('Profile updated')
    router.push({ query: {} })
  } catch (e) {
    error.value = apiError(e)
  } finally {
    saving.value = false
  }
}

async function onAvatar(event: Event) {
  const file = (event.target as HTMLInputElement).files?.[0]
  if (!file) return
  if (!ALLOWED_TYPES.includes(file.type)) return toast.error('Please choose a JPEG, PNG or WebP image.')
  if (file.size > MAX_AVATAR_BYTES) return toast.error('Image must be 5 MB or smaller.')
  try {
    auth.setUser(await userApi.uploadAvatar(file))
    toast.success('Photo updated')
  } catch (e) {
    toast.error(apiError(e))
  }
}

function logout() {
  auth.logout()
  cart.reset()
  router.push('/login')
}
</script>

<template>
  <div v-if="auth.user" class="mx-auto max-w-4xl px-4 py-12 lg:px-8">
    <div class="mb-6 flex items-start justify-between">
      <div>
        <h1 class="page-title">Welcome, {{ auth.user.username }}</h1>
        <p class="mt-1 text-sm text-muted">{{ formatLongDate() }}</p>
      </div>
      <button class="btn-outline" @click="logout">Log out</button>
    </div>

    <div class="overflow-hidden rounded-2xl ring-1 ring-line">
      <div class="h-28 bg-gradient-to-r from-blush-100 to-blush-300" />

      <div class="flex items-center justify-between px-6">
        <div class="-mt-10 flex items-center gap-4">
          <div class="relative">
            <SafeImage :src="auth.user.profileImage" alt="Avatar" class="h-24 w-24 rounded-full border-4 border-white object-cover" />
            <label v-if="editing" for="avatar" class="absolute right-0 bottom-0 flex h-8 w-8 cursor-pointer items-center justify-center rounded-full bg-ink text-xs text-white" title="Change photo">✎</label>
            <input id="avatar" type="file" accept="image/jpeg,image/png,image/webp" class="hidden" @change="onAvatar" />
          </div>
          <div class="pt-10">
            <p class="font-medium">{{ auth.user.fullName || auth.user.username }}</p>
            <p class="text-sm text-muted">{{ auth.user.email }}</p>
          </div>
        </div>
        <button v-if="!editing" class="btn-outline mt-4" @click="startEdit">Edit</button>
      </div>

      <form class="grid gap-5 p-6 md:grid-cols-2" @submit.prevent="save">
        <p v-if="error" class="rounded-lg bg-red-50 px-3 py-2 text-sm text-red-700 md:col-span-2" role="alert">{{ error }}</p>

        <div><label for="fullName" class="field-label">Full name</label><input id="fullName" v-model="form.fullName" :readonly="!editing" class="field" /></div>
        <div><label for="dob" class="field-label">Date of birth</label><input id="dob" v-model="form.dateOfBirth" type="date" :readonly="!editing" class="field" /></div>
        <div class="space-y-5">
          <div><label for="uname" class="field-label">Username</label><input id="uname" v-model="form.username" required :readonly="!editing" class="field" /></div>
          <div><label for="phone" class="field-label">Phone</label><input id="phone" v-model="form.phone" type="tel" :readonly="!editing" class="field" /></div>
          <div><label for="mail" class="field-label">Email</label><input id="mail" v-model="form.email" type="email" required :readonly="!editing" class="field" /></div>
        </div>
        <div><label for="address" class="field-label">Address</label><textarea id="address" v-model="form.address" :readonly="!editing" rows="8" class="field h-[calc(100%-1.5rem)] resize-none" /></div>

        <div v-if="editing" class="flex gap-3 md:col-span-2">
          <button type="submit" class="btn" :disabled="saving">Save</button>
          <button type="button" class="btn-outline" @click="cancel">Cancel</button>
        </div>
      </form>
    </div>
  </div>
</template>
