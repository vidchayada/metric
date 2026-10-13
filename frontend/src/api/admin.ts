import { http } from './client'
import type {
    AdminCategory,
    AdminOrder,
    AdminProduct,
    DashboardData,
    OrderQuery,
    OrderStatus,
    Page,
    ProductPayload,
    SalesHistory,
    Visibility,
} from '@/types'

function productForm(payload: ProductPayload, images: File[]): FormData {
    const body = new FormData()
    body.append('data', new Blob([JSON.stringify(payload)], { type: 'application/json' }))
    images.forEach((file) => body.append('images', file))
    return body
}

export const adminApi = {
    dashboard: () => http.get<DashboardData>('/admin/dashboard').then((r) => r.data),

    orders: (q: OrderQuery) =>
        http.get<Page<AdminOrder>>('/admin/orders', { params: { ...q, size: 8 } }).then((r) => r.data),
    updateOrderStatus: (id: number, status: OrderStatus) =>
        http.patch(`/admin/orders/${id}/status`, { status }),

    categories: (filter = '', search = '') =>
        http.get<AdminCategory[]>('/admin/categories', { params: { filter, search } }).then((r) => r.data),
    createCategory: (name: string, visibility: Visibility) =>
        http.post('/admin/categories', { name, visibility }),
    updateCategory: (id: number, name: string, visibility: Visibility) =>
        http.put(`/admin/categories/${id}`, { name, visibility }),
    setCategoryVisibility: (id: number, visibility: Visibility) =>
        http.patch(`/admin/categories/${id}/visibility`, { visibility }),
    deleteCategory: (id: number) => http.delete(`/admin/categories/${id}`),

    products: (categoryId: number | '' = '', search = '') =>
        http
            .get<AdminProduct[]>('/admin/products', { params: { category: categoryId || undefined, search } })
            .then((r) => r.data),
    createProduct: (payload: ProductPayload, images: File[]) =>
        http.post('/admin/products', productForm(payload, images)),
    updateProduct: (id: number, payload: ProductPayload, images: File[]) =>
        http.put(`/admin/products/${id}`, productForm(payload, images)),
    deleteProduct: (id: number) => http.delete(`/admin/products/${id}`),

    sales: (dailyMonth: number, dailyYear: number, monthlyYear: number) =>
        http
            .get<SalesHistory>('/admin/sales', { params: { dailyMonth, dailyYear, monthlyYear } })
            .then((r) => r.data),
}
