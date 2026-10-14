import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { authApi } from '@/api/shop'
import { TOKEN_KEY, setUnauthorizedHandler } from '@/api/client'
import type { User } from '@/types'

export const useAuthStore = defineStore('auth', () => {
    const token = ref<string | null>(localStorage.getItem(TOKEN_KEY))
    const user = ref<User | null>(null)

    const isLoggedIn = computed(() => !!token.value)
    const isAdmin = computed(() => user.value?.role?.toUpperCase() === 'ADMIN')

    function setSession(newToken: string, newUser: User) {
        token.value = newToken
        user.value = newUser
        localStorage.setItem(TOKEN_KEY, newToken)
    }

    function logout() {
        token.value = null
        user.value = null
        localStorage.removeItem(TOKEN_KEY)
    }

    async function login(username: string, password: string): Promise<User> {
        const res: any = await authApi.login(username, password)
        const userData: User = res.user ?? {
            id: res.userId ?? res.id,
            userId: res.userId ?? res.id,
            username: res.username,
            email: res.email,
            role: res.role,
        }
        setSession(res.token, userData)
        return userData
    }

    /** Restore the user after a page refresh (only the token is persisted). */
    async function fetchMe() {
        if (!token.value) return
        try {
            const data: any = await authApi.me()
            const rawUser = data.user ?? data
            const img = rawUser.profileImage ?? rawUser.avatar ?? rawUser.avatarUrl ?? ''

            user.value = {
                ...rawUser,
                profileImage: img,
                avatar: img,
                avatarUrl: img
            }
        } catch {
            logout()
        }
    }

    function setUser(updated: User) {
        user.value = updated
    }

    setUnauthorizedHandler(logout)

    return { token, user, isLoggedIn, isAdmin, login, logout, fetchMe, setUser }
})