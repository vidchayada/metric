<script setup lang="ts">
import { onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import { adminApi } from '@/api/admin'
import { apiError } from '@/api/client'
import { useToastStore } from '@/stores/toast'
import { useCatalogStore } from '@/stores/catalog'
import AppModal from '@/components/common/AppModal.vue'
import type { AdminCategory, Visibility } from '@/types'

const toast = useToastStore()
const catalog = useCatalogStore()

const categories = ref<AdminCategory[]>([])
const filter = ref<Visibility | ''>('')
const search = ref('')
const loading = ref(false)

const modalOpen = ref(false)
const editingId = ref<number | null>(null)
const form = reactive<{ name: string; visibility: Visibility }>({ name: '', visibility: 'show' })

async function load() {
  loading.value = true
  try {
    categories.value = await adminApi.categories(filter.value, search.value.trim())
  } catch (e) {
    toast.error(apiError(e, 'Could not load categories.'))
  } finally {
    loading.value = false
  }
}

let timer: ReturnType<typeof setTimeout> | undefined
watch(search, () => {
  clearTimeout(timer)
  timer = setTimeout(load, 400)
})
watch(filter, load)
onBeforeUnmount(() => clearTimeout(timer))
onMounted(load)

function openCreate() {
  editingId.value = null
  Object.assign(form, { name: '', visibility: 'show' })
  modalOpen.value = true
}
function openEdit(c: AdminCategory) {
  editingId.value = c.id
  Object.assign(form, { name: c.name, visibility: c.visibility })
  modalOpen.value = true
}

async function run(action: () => Promise<unknown>, success: string) {
  try {
    await action()
    toast.success(success)
    await load()
    catalog.loadCategories(true) // keep the storefront menu in sync
  } catch (e) {
    toast.error(apiError(e))
  }
}

async function save() {
  const name = form.name.trim()
  if (!name) return
  const id = editingId.value
  await run(
      () => (id === null ? adminApi.createCategory(name, form.visibility) : adminApi.updateCategory(id, name, form.visibility)),
      id === null ? 'Category created' : 'Category updated',
  )
  modalOpen.value = false
}

const toggleVisibility = (c: AdminCategory) =>
    run(() => adminApi.setCategoryVisibility(c.id, c.visibility === 'show' ? 'hidden' : 'show'), 'Visibility updated')

function remove(c: AdminCategory) {
  if (confirm(`Delete category "${c.name}"?`)) run(() => adminApi.deleteCategory(c.id), 'Category deleted')
}

const btn = 'rounded-full px-3 py-1 text-xs font-medium tracking-wider uppercase transition'
</script>

<template>
  <div>
    <div class="mb-6 flex flex-wrap items-end justify-between gap-4">
      <div>
        <h1 class="page-title">Categories</h1>
        <p class="text-sm text-muted">Manage your categories</p>
      </div>
      <div class="flex flex-wrap items-center gap-2">
        <select v-model="filter" class="field w-auto" aria-label="Filter by visibility">
          <option value="">All Categories</option>
          <option value="show">Show</option>
          <option value="hidden">Hidden</option>
        </select>
        <input v-model="search" placeholder="Search category name" aria-label="Search categories" class="field w-56" />
        <button class="btn !h-10 !w-10 !px-0 text-lg" aria-label="Add category" @click="openCreate">+</button>
      </div>
    </div>

    <p v-if="!loading && !categories.length" class="py-16 text-center text-muted">No categories found.</p>

    <div v-else class="overflow-x-auto rounded-2xl bg-white ring-1 ring-line">
      <table class="w-full min-w-[560px] text-sm">
        <thead>
        <tr class="border-b border-line text-left text-xs tracking-wider text-muted uppercase">
          <th class="px-4 py-3">Category name</th><th>Product</th><th>Visibility</th><th class="pr-4 text-right">Action</th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="c in categories" :key="c.id" class="border-b border-line last:border-0">
          <td class="px-4 py-3">#{{ c.name.toLowerCase() }}</td>
          <td>{{ c.productCount }}</td>
          <td><span :class="['rounded-full px-2 py-0.5 text-xs', c.visibility === 'show' ? 'bg-emerald-50 text-emerald-700' : 'bg-line text-muted']">{{ c.visibility }}</span></td>
          <td class="space-x-2 pr-4 text-right">
            <button :class="[btn, 'bg-blush-100 hover:bg-blush-300']" @click="toggleVisibility(c)">{{ c.visibility === 'show' ? 'Hide' : 'Show' }}</button>
            <button :class="[btn, 'bg-ink text-white hover:bg-blush-700']" @click="openEdit(c)">Edit</button>
            <button :class="[btn, 'bg-red-50 text-red-700 hover:bg-red-100']" @click="remove(c)">Delete</button>
          </td>
        </tr>
        </tbody>
      </table>
    </div>

    <AppModal :open="modalOpen" :title="editingId === null ? 'New Category' : 'Edit Category'" @close="modalOpen = false">
      <form id="category-form" class="space-y-4" @submit.prevent="save">
        <div><label for="cat-name" class="field-label">Category name</label><input id="cat-name" v-model="form.name" required class="field" /></div>
        <div>
          <label for="cat-vis" class="field-label">Visibility</label>
          <select id="cat-vis" v-model="form.visibility" class="field"><option value="show">Show</option><option value="hidden">Hidden</option></select>
        </div>
      </form>
      <template #actions>
        <button type="button" class="btn-outline" @click="modalOpen = false">Cancel</button>
        <button type="submit" form="category-form" class="btn">Confirm</button>
      </template>
    </AppModal>
  </div>
</template>
