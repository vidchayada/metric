import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { authApi } from '@/api/shop'
import { TOKEN_KEY, setUnauthorizedHandler } from '@/api/client'
import type { User } from '@/types'

export const useAuthStore = defineStore('auth', () => {
    const token = ref<string | null>(localStorage.getItem(TOKEN_KEY))
    const user = ref<User | null>(null)

    const isLoggedIn = computed(() => !!token.value)
    const isAdmin = computed(() => user.value?.role === 'ADMIN')

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
        const res = await authApi.login(username, password)
        setSession(res.token, res.user)
        return res.user
    }

    /** Restore the user after a page refresh (only the token is persisted). */
    async function fetchMe() {
        if (!token.value) return
        try {
            user.value = await authApi.me()
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
