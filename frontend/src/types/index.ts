export type Role = 'USER' | 'ADMIN'
export type Visibility = 'show' | 'hidden'
export type OrderStatus = 'pending' | 'processing' | 'shipped' | 'completed' | 'cancelled'

export const ORDER_STATUSES: OrderStatus[] = [
    'pending',
    'processing',
    'shipped',
    'completed',
    'cancelled',
]

export interface User {
    id: number
    username: string
    fullName: string | null
    email: string
    phone: string | null
    dateOfBirth: string | null
    address: string | null
    profileImage: string | null
    role: Role
}

export interface AuthResponse {
    token: string
    user: User
}

export interface Page<T> {
    items: T[]
    page: number
    totalPages: number
    totalItems: number
}

export interface ProductSummary {
    id: number
    name: string
    price: number
    image: string | null
}

export interface CategorySummary {
    id: number
    name: string
    slug: string
    image: string | null
}

export interface HomeData {
    bestSellers: ProductSummary[]
    categories: CategorySummary[]
    newestImage: string | null
}

export interface ProductImage {
    id: number
    url: string
    primary: boolean
    sortOrder: number
}

export interface ProductAttribute {
    id: number
    size: string | null
    color: string | null
    stockQty: number
}

export interface ProductDetail {
    id: number
    name: string
    description: string | null
    price: number
    categoryName: string | null
    images: ProductImage[]
    attributes: ProductAttribute[]
}

export interface CartItem {
    id: number
    productId: number
    attributeId: number | null
    name: string
    price: number
    quantity: number
    size: string | null
    color: string | null
    image: string | null
}

export interface ProfileForm {
    fullName: string
    username: string
    email: string
    phone: string
    dateOfBirth: string
    address: string
}

export interface RegisterForm {
    username: string
    email: string
    password: string
    fullName?: string
}

export type FieldErrors = Partial<Record<'username' | 'email' | 'password' | 'general', string>>

/* ---------- admin ---------- */
export interface DashboardData {
    totalMonth: number
    totalWeek: number
    totalCategory: number
    totalProduct: number
    itemSold: { name: string; qty: number }[]
    categories: { id: number; name: string; products: string[] }[]
    recentOrders: { number: string; date: string; customer: string | null }[]
    outOfStock: string[]
}

export interface AdminOrder {
    id: number
    orderNumber: string
    orderDate: string
    orderName: string | null
    customer: string | null
    status: OrderStatus
}

export interface OrderQuery {
    search?: string
    month?: number | ''
    status?: OrderStatus | ''
    page?: number
}

export interface AdminCategory {
    id: number
    name: string
    slug: string
    visibility: Visibility
    productCount: number
}

export interface AdminAttribute {
    size: string
    color: string
    stockQty: number
}

export interface AdminProduct {
    id: number
    name: string
    detail: string | null
    price: number
    categoryId: number | null
    categoryName: string | null
    mainImage: string | null
    totalStock: number
    attributes: AdminAttribute[]
}

export interface ProductPayload {
    name: string
    detail: string
    price: number
    categoryId: number
    attributes: AdminAttribute[]
}

export interface SalesRow {
    label: string
    totalQty: number
    totalSales: number
}

export interface SalesHistory {
    topProducts: { name: string; imageUrl: string | null; totalOrders: number }[]
    daily: SalesRow[]
    monthly: SalesRow[]
}
