import { beforeEach, describe, expect, it, vi } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'
import { useAuthStore } from '@/stores/auth'
import { authApi } from '@/api/shop'
import { TOKEN_KEY } from '@/api/client'
import type { User } from '@/types'

vi.mock('@/api/shop', () => ({ authApi: { login: vi.fn(), me: vi.fn(), register: vi.fn() } }))

const user = (role: User['role']): User => ({
  id: 1,
  username: 'u',
  fullName: null,
  email: 'u@x.com',
  phone: null,
  dateOfBirth: null,
  address: null,
  profileImage: null,
  role,
})

describe('auth store', () => {
  beforeEach(() => {
    localStorage.clear()
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  it('stores token and user on login', async () => {
    vi.mocked(authApi.login).mockResolvedValue({ token: 'jwt', user: user('USER') })
    const auth = useAuthStore()
    await auth.login('u', 'pw')
    expect(auth.isLoggedIn).toBe(true)
    expect(auth.isAdmin).toBe(false)
    expect(localStorage.getItem(TOKEN_KEY)).toBe('jwt')
  })

  it('recognises admins', async () => {
    vi.mocked(authApi.login).mockResolvedValue({ token: 'jwt', user: user('ADMIN') })
    const auth = useAuthStore()
    await auth.login('admin', 'pw')
    expect(auth.isAdmin).toBe(true)
  })

  it('clears the session on logout', async () => {
    vi.mocked(authApi.login).mockResolvedValue({ token: 'jwt', user: user('USER') })
    const auth = useAuthStore()
    await auth.login('u', 'pw')
    auth.logout()
    expect(auth.isLoggedIn).toBe(false)
    expect(auth.user).toBeNull()
    expect(localStorage.getItem(TOKEN_KEY)).toBeNull()
  })

  it('restores the user from a persisted token', async () => {
    localStorage.setItem(TOKEN_KEY, 'jwt')
    vi.mocked(authApi.me).mockResolvedValue(user('USER'))
    setActivePinia(createPinia())
    const auth = useAuthStore()
    await auth.fetchMe()
    expect(auth.user?.username).toBe('u')
  })

  it('logs out when the stored token is rejected', async () => {
    localStorage.setItem(TOKEN_KEY, 'expired')
    vi.mocked(authApi.me).mockRejectedValue(new Error('401'))
    setActivePinia(createPinia())
    const auth = useAuthStore()
    await auth.fetchMe()
    expect(auth.isLoggedIn).toBe(false)
  })

  it('does nothing when there is no token', async () => {
    const auth = useAuthStore()
    await auth.fetchMe()
    expect(authApi.me).not.toHaveBeenCalled()
  })
})
