import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import MainLayout from '@/layouts/MainLayout.vue'
import AuthLayout from '@/layouts/AuthLayout.vue'
import AdminLayout from '@/layouts/AdminLayout.vue'

export const routes = [
    {
        path: '/',
        component: MainLayout,
        children: [
            { path: '', name: 'home', component: () => import('@/views/HomeView.vue') },
            { path: 'products', name: 'products', component: () => import('@/views/AllProductsView.vue') },
            { path: 'category/:slug', name: 'category', component: () => import('@/views/CategoryView.vue') },
            { path: 'search', name: 'search', component: () => import('@/views/SearchView.vue') },
            { path: 'product/:id(\\d+)', name: 'product', component: () => import('@/views/ProductDetailView.vue') },
            { path: 'cart', name: 'cart', component: () => import('@/views/CartView.vue'), meta: { requiresAuth: true } },
            { path: 'contact', name: 'contact', component: () => import('@/views/ContactView.vue') },
            { path: 'profile', name: 'profile', component: () => import('@/views/ProfileView.vue'), meta: { requiresAuth: true } },
            { path: ':pathMatch(.*)*', name: 'not-found', component: () => import('@/views/NotFoundView.vue') },
        ],
    },
    {
        path: '/',
        component: AuthLayout,
        children: [
            { path: 'login', name: 'login', component: () => import('@/views/auth/LoginView.vue') },
            { path: 'register', name: 'register', component: () => import('@/views/auth/RegisterView.vue') },
        ],
    },
    {
        path: '/admin',
        component: AdminLayout,
        meta: { requiresAuth: true, requiresAdmin: true },
        children: [
            { path: '', redirect: { name: 'admin-dashboard' } },
            { path: 'dashboard', name: 'admin-dashboard', component: () => import('@/views/admin/DashboardView.vue') },
            { path: 'orders', name: 'admin-orders', component: () => import('@/views/admin/OrdersView.vue') },
            { path: 'products', name: 'admin-products', component: () => import('@/views/admin/ProductsView.vue') },
            { path: 'categories', name: 'admin-categories', component: () => import('@/views/admin/CategoriesView.vue') },
            { path: 'sales-history', name: 'admin-sales', component: () => import('@/views/admin/SalesHistoryView.vue') },
        ],
    },
]

export const router = createRouter({
    history: createWebHistory(),
    routes,
    scrollBehavior: () => ({ top: 0 }),
})

router.beforeEach(async (to) => {
    const auth = useAuthStore()
    const needsAuth = to.matched.some((r) => r.meta.requiresAuth)
    const needsAdmin = to.matched.some((r) => r.meta.requiresAdmin)

    if (auth.isLoggedIn && !auth.user) await auth.fetchMe()

    if (needsAuth && !auth.isLoggedIn) {
        return { name: 'login', query: { returnTo: to.fullPath } }
    }
    if (needsAdmin && !auth.isAdmin) return { name: 'home' }
    if ((to.name === 'login' || to.name === 'register') && auth.isLoggedIn) {
        return auth.isAdmin ? { name: 'admin-dashboard' } : { name: 'home' }
    }
})
