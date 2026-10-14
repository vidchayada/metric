import { http } from './client'
import type {
    AuthResponse,
    CartItem,
    CategorySummary,
    HomeData,
    Page,
    ProductDetail,
    ProductSummary,
    ProfileForm,
    RegisterForm,
    User,
} from '@/types'

export const authApi = {
    login: (username: string, password: string) =>
        http.post<AuthResponse>('/auth/login', { username, password }).then((r) => r.data),
    register: (form: RegisterForm) => http.post<void>('/auth/register', form),
    me: () => http.get<User>('/auth/me').then((r) => r.data),
}

export const userApi = {
    update: (form: ProfileForm) => http.put<User>('/users/me', form).then((r) => r.data),
    uploadAvatar: (file: File) => {
        const body = new FormData()
        body.append('avatar', file)
        return http.post<User>('/users/me/avatar', body).then((r) => r.data)
    },
}

export interface ProductQuery {
    page?: number
    size?: number
    category?: string
    q?: string
}

export const catalogApi = {
    home: () => http.get<HomeData>('/home').then((r) => r.data),
    categories: () => http.get<CategorySummary[]>('/categories').then((r) => r.data),
    products: (params: ProductQuery) =>
        http.get<Page<ProductSummary>>('/products', { params }).then((r) => r.data),
    product: (id: number) => http.get<ProductDetail>(`/products/${id}`).then((r) => r.data),
}

export const cartApi = {
    list: () => http.get<CartItem[]>('/cart').then((r) => r.data),
    count: () => http.get<{ count: number }>('/cart/count').then((r) => r.data.count),
    add: (productId: number, attributeId: number | null, quantity: number) =>
        http.post('/cart/items', { productId, attributeId, quantity }),
    setQuantity: (id: number, quantity: number) => http.patch(`/cart/items/${id}`, { quantity }),
    remove: (id: number) => http.delete(`/cart/items/${id}`),
    checkout: (shippingAddress: string = 'Default Address') =>
        http.post<{ orderNumber: string }>('/orders/checkout', { shippingAddress }).then((r) => r.data),
}

export const contactApi = {
    subscribe: (email: string) =>
        http.post<{ message: string }>('/subscribers', { email }).then((r) => r.data),
}
