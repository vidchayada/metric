<script setup lang="ts">
import { onMounted, reactive, ref, watch } from 'vue'
import { adminApi } from '@/api/admin'
import { apiError } from '@/api/client'
import { useToastStore } from '@/stores/toast'
import AppModal from '@/components/common/AppModal.vue'
import SafeImage from '@/components/common/SafeImage.vue'
import { formatPrice } from '@/utils/format'
import type { AdminAttribute, AdminCategory, AdminProduct } from '@/types'

const toast = useToastStore()

const products = ref<AdminProduct[]>([])
const categories = ref<AdminCategory[]>([])
const categoryFilter = ref<number | ''>('')
const search = ref('')
const loading = ref(false)

const modalOpen = ref(false)
const editingId = ref<number | null>(null)
const saving = ref(false)
const images = ref<File[]>([])
const form = reactive({ categoryId: '' as number | '', name: '', detail: '', price: 0 })
const attributes = ref<AdminAttribute[]>([])

const emptyRow = (): AdminAttribute => ({ size: '', color: '', stockQty: 0 })

async function load() {
  loading.value = true
  try {
    products.value = await adminApi.products(categoryFilter.value, search.value.trim())
  } catch (e) {
    toast.error(apiError(e, 'Could not load products.'))
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  try {
    categories.value = await adminApi.categories()
  } catch (e) {
    toast.error(apiError(e))
  }
  load()
})
watch(categoryFilter, load)

function openCreate() {
  editingId.value = null
  Object.assign(form, { categoryId: '', name: '', detail: '', price: 0 })
  attributes.value = [emptyRow()]
  images.value = []
  modalOpen.value = true
}

function openEdit(p: AdminProduct) {
  editingId.value = p.id
  Object.assign(form, { categoryId: p.categoryId ?? '', name: p.name, detail: p.detail ?? '', price: p.price })
  attributes.value = p.attributes.length ? p.attributes.map((a) => ({ ...a })) : [emptyRow()]
  images.value = []
  modalOpen.value = true
}

const addRow = () => attributes.value.push(emptyRow())
function removeRow(i: number) {
  attributes.value.splice(i, 1)
  if (!attributes.value.length) attributes.value.push(emptyRow())
}
const onFiles = (e: Event) => (images.value = Array.from((e.target as HTMLInputElement).files ?? []))

async function save() {
  if (form.categoryId === '') return toast.error('Please select a category.')
  saving.value = true
  const payload = {
    name: form.name.trim(),
    detail: form.detail.trim(),
    price: Number(form.price) || 0,
    categoryId: form.categoryId,
    attributes: attributes.value.filter((a) => a.size.trim() || a.color.trim()).map((a) => ({ ...a, stockQty: Number(a.stockQty) || 0 })),
  }
  try {
    if (editingId.value === null) await adminApi.createProduct(payload, images.value)
    else await adminApi.updateProduct(editingId.value, payload, images.value)
    toast.success(editingId.value === null ? 'Product created' : 'Product updated')
    modalOpen.value = false
    await load()
  } catch (e) {
    toast.error(apiError(e))
  } finally {
    saving.value = false
  }
}

async function remove(p: AdminProduct) {
  if (!confirm(`Delete "${p.name}"?`)) return
  try {
    await adminApi.deleteProduct(p.id)
    toast.success('Product deleted')
    await load()
  } catch (e) {
    toast.error(apiError(e))
  }
}
</script>

<template>
  <div>
    <div class="mb-6 flex flex-wrap items-end justify-between gap-4">
      <div>
        <h1 class="page-title">Product</h1>
        <p class="text-sm text-muted">Manage clothing inventory</p>
      </div>
      <div class="flex flex-wrap items-center gap-2">
        <select v-model="categoryFilter" class="field w-auto" aria-label="Filter by category">
          <option value="">All Categories</option>
          <option v-for="c in categories" :key="c.id" :value="c.id">{{ c.name }}</option>
        </select>
        <input v-model="search" placeholder="Search product name" aria-label="Search products" class="field w-56" @keyup.enter="load" />
        <button class="btn !h-10 !w-10 !px-0 text-lg" aria-label="Add product" @click="openCreate">+</button>
      </div>
    </div>

    <p v-if="!loading && !products.length" class="py-16 text-center text-muted">No products found. Add your first product!</p>

    <ul v-else class="space-y-3">
      <li v-for="p in products" :key="p.id" class="grid gap-4 rounded-2xl bg-white p-4 ring-1 ring-line md:grid-cols-[88px_1fr_1fr_auto] md:items-center">
        <div class="h-28 w-22 overflow-hidden rounded-lg bg-blush-50 md:h-28">
          <SafeImage :src="p.mainImage" :alt="p.name" class="h-full w-full object-cover" />
        </div>
        <div class="min-w-0">
          <p class="font-medium">{{ p.name }}</p>
          <p class="line-clamp-2 text-sm text-muted">{{ p.detail || 'No detail' }}</p>
        </div>
        <dl class="grid grid-cols-[auto_1fr] gap-x-4 gap-y-1 text-sm">
          <dt class="text-xs tracking-wider text-muted uppercase">Category</dt><dd>{{ p.categoryName ?? '-' }}</dd>
          <dt class="text-xs tracking-wider text-muted uppercase">Price</dt><dd>{{ formatPrice(p.price) }}</dd>
          <dt class="text-xs tracking-wider text-muted uppercase">Stock</dt>
          <dd :class="p.totalStock === 0 ? 'text-red-600' : ''">{{ p.totalStock }} items</dd>
          <dt class="text-xs tracking-wider text-muted uppercase">Variants</dt>
          <dd>{{ p.attributes.map((a) => [a.size, a.color].filter(Boolean).join(' / ')).join(', ') || '—' }}</dd>
        </dl>
        <div class="flex gap-2 md:flex-col">
          <button class="btn !py-1.5" @click="openEdit(p)">Edit</button>
          <button class="btn-outline !border-red-300 !py-1.5 !text-red-700 hover:!bg-red-600 hover:!text-white" @click="remove(p)">Delete</button>
        </div>
      </li>
    </ul>

    <AppModal :open="modalOpen" :title="editingId === null ? 'New Product' : 'Edit Product'" @close="modalOpen = false">
      <form id="product-form" class="space-y-4" @submit.prevent="save">
        <div>
          <label for="p-cat" class="field-label">Category</label>
          <select id="p-cat" v-model="form.categoryId" required class="field">
            <option value="" disabled>Select Category</option>
            <option v-for="c in categories" :key="c.id" :value="c.id">{{ c.name }}</option>
          </select>
        </div>
        <div><label for="p-name" class="field-label">Product name</label><input id="p-name" v-model="form.name" required placeholder="Cotton Shirt" class="field" /></div>
        <div><label for="p-detail" class="field-label">Detail</label><textarea id="p-detail" v-model="form.detail" rows="3" placeholder="About your product..." class="field" /></div>
        <div class="grid grid-cols-2 gap-4">
          <div><label for="p-price" class="field-label">Price (฿)</label><input id="p-price" v-model.number="form.price" type="number" min="0" required class="field" /></div>
          <div>
            <label for="p-images" class="field-label">Images</label>
            <input id="p-images" type="file" multiple accept="image/*" class="field text-xs" @change="onFiles" />
          </div>
        </div>

        <fieldset>
          <legend class="field-label">Inventory variations</legend>
          <div v-for="(row, i) in attributes" :key="i" class="mb-2 grid grid-cols-[1fr_1fr_80px_32px] gap-2">
            <input v-model="row.size" placeholder="Size" aria-label="Size" class="field" />
            <input v-model="row.color" placeholder="Color" aria-label="Color" class="field" />
            <input v-model.number="row.stockQty" type="number" min="0" placeholder="Qty" aria-label="Quantity" class="field" />
            <button type="button" class="text-muted hover:text-red-600" aria-label="Remove variation" @click="removeRow(i)">−</button>
          </div>
          <button type="button" class="text-sm underline" @click="addRow">+ Add variation</button>
        </fieldset>
      </form>
      <template #actions>
        <button type="button" class="btn-outline" @click="modalOpen = false">Cancel</button>
        <button type="submit" form="product-form" class="btn" :disabled="saving">Confirm</button>
      </template>
    </AppModal>
  </div>
</template>
