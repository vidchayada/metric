<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { adminApi } from '@/api/admin'
import { apiError } from '@/api/client'
import { useToastStore } from '@/stores/toast'
import AppPagination from '@/components/common/AppPagination.vue'
import { MONTHS } from '@/utils/format'
import { ORDER_STATUSES, type AdminOrder, type OrderStatus } from '@/types'

const route = useRoute()
const router = useRouter()
const toast = useToastStore()

const orders = ref<AdminOrder[]>([])
const page = ref(1)
const totalPages = ref(0)
const loading = ref(false)

const search = ref(String(route.query.search ?? ''))
const month = computed(() => (route.query.month ? Number(route.query.month) : ''))
const status = computed(() => (route.query.status as OrderStatus | undefined) ?? '')
const currentPage = computed(() => Math.max(1, Number(route.query.page) || 1))

async function load() {
  loading.value = true
  try {
    const res = await adminApi.orders({
      search: String(route.query.search ?? ''),
      month: month.value,
      status: status.value,
      page: currentPage.value,
    })
    orders.value = res.items
    page.value = res.page
    totalPages.value = res.totalPages
  } catch (e) {
    toast.error(apiError(e, 'Could not load orders.'))
  } finally {
    loading.value = false
  }
}

function setFilter(key: 'search' | 'month' | 'status', value: string) {
  router.push({ query: { ...route.query, [key]: value || undefined, page: undefined } })
}
const goTo = (p: number) => router.push({ query: { ...route.query, page: p } })

async function changeStatus(order: AdminOrder, next: OrderStatus) {
  const targetId = (order as any).id ?? (order as any).orderId
  if (!targetId) {
    toast.error('Order ID not found')
    return
  }

  const previous = order.status
  order.status = next
  try {
    await adminApi.updateOrderStatus(targetId, next)
    toast.success('Status updated successfully')
  } catch (e) {
    order.status = previous
    toast.error(apiError(e, 'Could not update status.'))
  }
}

watch(() => route.query, load)
onMounted(load)
</script>

<template>
  <div>
    <div class="mb-6 flex flex-wrap items-end justify-between gap-4">
      <h1 class="page-title">Orders</h1>
      <form class="flex gap-2" role="search" @submit.prevent="setFilter('search', search.trim())">
        <input v-model="search" placeholder="Search order name" aria-label="Search orders" class="field w-64" />
        <button class="btn" type="submit">Search</button>
      </form>
    </div>

    <div class="mb-6 flex flex-wrap gap-4">
      <div>
        <label for="month" class="field-label">Month</label>
        <select id="month" class="field" :value="month" @change="setFilter('month', ($event.target as HTMLSelectElement).value)">
          <option value="">All</option>
          <option v-for="(m, i) in MONTHS" :key="m" :value="i + 1">{{ m }}</option>
        </select>
      </div>
      <div>
        <label for="status" class="field-label">Status</label>
        <select id="status" class="field" :value="status" @change="setFilter('status', ($event.target as HTMLSelectElement).value)">
          <option value="">All</option>
          <option v-for="s in ORDER_STATUSES" :key="s" :value="s">{{ s }}</option>
        </select>
      </div>
    </div>

    <p v-if="!loading && !orders.length" class="py-16 text-center text-muted">No orders found.</p>

    <div v-else class="overflow-x-auto rounded-2xl bg-white ring-1 ring-line">
      <table class="w-full min-w-[640px] text-sm">
        <thead>
        <tr class="border-b border-line text-left text-xs tracking-wider text-muted uppercase">
          <th class="px-4 py-3">Order number</th><th>Order date</th><th>Order name</th><th>Customer</th><th class="pr-4">Status</th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="o in orders" :key="o.id" class="border-b border-line last:border-0">
          <td class="px-4 py-3">{{ o.orderNumber }}</td>
          <td>{{ o.orderDate }}</td>
          <td>{{ o.orderName ?? '—' }}</td>
          <td>{{ o.customer ?? '—' }}</td>
          <td class="pr-4">
            <select class="field !py-1.5" :value="o.status" :aria-label="`Status of ${o.orderNumber}`" @change="changeStatus(o, ($event.target as HTMLSelectElement).value as OrderStatus)">
              <option v-for="s in ORDER_STATUSES" :key="s" :value="s">{{ s }}</option>
            </select>
          </td>
        </tr>
        </tbody>
      </table>
    </div>

    <AppPagination :page="page" :total-pages="totalPages" @change="goTo" />
  </div>
</template>
