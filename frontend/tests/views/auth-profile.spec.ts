import { beforeEach, describe, expect, it, vi } from 'vitest'
import { DOMWrapper } from '@vue/test-utils'
import LoginView from '@/views/auth/LoginView.vue'
import RegisterView from '@/views/auth/RegisterView.vue'
import ProfileView from '@/views/ProfileView.vue'
import { useAuthStore } from '@/stores/auth'
import { useCartStore } from '@/stores/cart'
import { useToastStore } from '@/stores/toast'
import { TOKEN_KEY } from '@/api/client'
import { shopApiMock } from '../mocks/api'
import { axiosErr, mountWith, settle, user } from '../helpers'
import type { Pinia } from 'pinia'

vi.mock('@/api/shop', async () => (await import('../mocks/api')).shopApiMock)

const lastToast = (pinia: Pinia) => useToastStore(pinia).toasts.at(-1)

beforeEach(() => {
  vi.resetAllMocks()
  shopApiMock.cartApi.count.mockResolvedValue(0)
})

describe('LoginView', () => {
  async function submit(wrapper: Awaited<ReturnType<typeof mountWith>>['wrapper']) {
    await wrapper.find('#username').setValue(' mina ')
    await wrapper.find('#password').setValue('secret123')
    await wrapper.find('form').trigger('submit')
    await settle()
  }

  it('logs a customer in and goes to the home page', async () => {
    shopApiMock.authApi.login.mockResolvedValue({ token: 'jwt', user: user() })
    const { wrapper, router } = await mountWith(LoginView, { route: '/login' })
    await submit(wrapper)
    expect(shopApiMock.authApi.login).toHaveBeenCalledWith('mina', 'secret123')
    expect(localStorage.getItem(TOKEN_KEY)).toBe('jwt')
    expect(router.currentRoute.value.path).toBe('/')
  })

  it('sends admins to the dashboard', async () => {
    shopApiMock.authApi.login.mockResolvedValue({ token: 'jwt', user: user({ role: 'ADMIN' }) })
    const { wrapper, router } = await mountWith(LoginView, { route: '/login' })
    await submit(wrapper)
    expect(router.currentRoute.value.path).toBe('/admin/dashboard')
  })

  it('returns to the page the user came from', async () => {
    shopApiMock.authApi.login.mockResolvedValue({ token: 'jwt', user: user() })
    const { wrapper, router } = await mountWith(LoginView, { route: '/login?returnTo=/cart' })
    await submit(wrapper)
    expect(router.currentRoute.value.path).toBe('/cart')
  })

  it.each(['https://evil.example/steal', '//evil.example'])('ignores unsafe returnTo %s', async (target) => {
    shopApiMock.authApi.login.mockResolvedValue({ token: 'jwt', user: user() })
    const { wrapper, router } = await mountWith(LoginView, {
      route: `/login?returnTo=${encodeURIComponent(target)}`,
    })
    await submit(wrapper)
    expect(router.currentRoute.value.path).toBe('/')
  })

  it('shows a clear message for wrong credentials', async () => {
    shopApiMock.authApi.login.mockRejectedValue(axiosErr(401))
    const { wrapper } = await mountWith(LoginView, { route: '/login' })
    await submit(wrapper)
    expect(wrapper.find('[role="alert"]').text()).toBe('Incorrect username or password.')
    expect(localStorage.getItem(TOKEN_KEY)).toBeNull()
  })

  it('shows a generic message for other failures', async () => {
    shopApiMock.authApi.login.mockRejectedValue(axiosErr(500))
    const { wrapper } = await mountWith(LoginView, { route: '/login' })
    await submit(wrapper)
    expect(wrapper.find('[role="alert"]').text()).toBe('Something went wrong.')
  })

  it('confirms a fresh registration', async () => {
    const { wrapper } = await mountWith(LoginView, { route: '/login?registered=1' })
    expect(wrapper.find('[role="status"]').text()).toContain('Account created')
  })
})

describe('RegisterView', () => {
  const fill = async (
    wrapper: Awaited<ReturnType<typeof mountWith>>['wrapper'],
    v: { username?: string; email?: string; password?: string; terms?: boolean },
  ) => {
    await wrapper.find('#username').setValue(v.username ?? '')
    await wrapper.find('#email').setValue(v.email ?? '')
    await wrapper.find('#password').setValue(v.password ?? '')
    await wrapper.find('input[type="checkbox"]').setValue(v.terms ?? false)
    await wrapper.find('form').trigger('submit')
    await settle()
  }

  it('validates every field before calling the API', async () => {
    const { wrapper } = await mountWith(RegisterView, { route: '/register' })
    await fill(wrapper, {})
    const text = wrapper.text()
    expect(text).toContain('Username is required')
    expect(text).toContain('Email is required')
    expect(text).toContain('Password is required')
    expect(text).toContain('Please accept the terms')
    expect(shopApiMock.authApi.register).not.toHaveBeenCalled()
  })

  it('rejects a malformed email and a short password', async () => {
    const { wrapper } = await mountWith(RegisterView, { route: '/register' })
    await fill(wrapper, { username: 'mina', email: 'not-an-email', password: 'short', terms: true })
    expect(wrapper.text()).toContain('Enter a valid email')
    expect(wrapper.text()).toContain('at least 8 characters')
    expect(shopApiMock.authApi.register).not.toHaveBeenCalled()
  })

  it('registers and moves to the login page', async () => {
    shopApiMock.authApi.register.mockResolvedValue({})
    const { wrapper, router } = await mountWith(RegisterView, { route: '/register' })
    await fill(wrapper, { username: ' mina ', email: 'mina@example.com', password: 'password1', terms: true })
    expect(shopApiMock.authApi.register).toHaveBeenCalledWith({
      username: 'mina',
      email: 'mina@example.com',
      password: 'password1',
    })
    expect(router.currentRoute.value.name).toBe('login')
    expect(router.currentRoute.value.query.registered).toBe('1')
  })

  it('shows duplicate-field errors from a 409', async () => {
    shopApiMock.authApi.register.mockRejectedValue(
      axiosErr(409, { errors: { username: 'Username is already taken', email: 'Email is already registered' } }),
    )
    const { wrapper } = await mountWith(RegisterView, { route: '/register' })
    await fill(wrapper, { username: 'mina', email: 'mina@example.com', password: 'password1', terms: true })
    expect(wrapper.text()).toContain('Username is already taken')
    expect(wrapper.text()).toContain('Email is already registered')
  })

  it('shows a general error for other failures', async () => {
    shopApiMock.authApi.register.mockRejectedValue(axiosErr(500))
    const { wrapper } = await mountWith(RegisterView, { route: '/register' })
    await fill(wrapper, { username: 'mina', email: 'mina@example.com', password: 'password1', terms: true })
    expect(wrapper.find('[role="alert"]').text()).toContain('Something went wrong')
  })

  it('toggles password visibility', async () => {
    const { wrapper } = await mountWith(RegisterView, { route: '/register' })
    expect(wrapper.find('#password').attributes('type')).toBe('password')
    await wrapper.find('button[aria-label="Show password"]').trigger('click')
    expect(wrapper.find('#password').attributes('type')).toBe('text')
    await wrapper.find('button[aria-label="Hide password"]').trigger('click')
    expect(wrapper.find('#password').attributes('type')).toBe('password')
  })
})

describe('ProfileView', () => {
  const open = (route = '/profile') => mountWith(ProfileView, { route, loggedIn: true })
  const input = (wrapper: Awaited<ReturnType<typeof open>>['wrapper'], id: string) =>
    wrapper.find(`#${id}`).element as HTMLInputElement

  it('shows the profile read-only with a welcome message', async () => {
    const { wrapper } = await open()
    expect(wrapper.text()).toContain('Welcome, mina')
    expect(input(wrapper, 'uname').readOnly).toBe(true)
    expect(input(wrapper, 'uname').value).toBe('mina')
    expect(input(wrapper, 'dob').value).toBe('2000-01-31')
    expect(wrapper.text()).toContain('mina@example.com')
  })

  it('renders nothing until the user is known', async () => {
    const { wrapper } = await mountWith(ProfileView, { route: '/profile' })
    expect(wrapper.find('form').exists()).toBe(false)
  })

  it('enters edit mode, saves changes and returns to read-only', async () => {
    const updated = user({ fullName: 'Mina C.', phone: '0999999999' })
    shopApiMock.userApi.update.mockResolvedValue(updated)
    const { wrapper, router, pinia } = await open()

    await wrapper.findAll('button').find((b) => b.text() === 'Edit')!.trigger('click')
    await settle()
    expect(router.currentRoute.value.query.edit).toBe('1')
    expect(input(wrapper, 'uname').readOnly).toBe(false)

    await wrapper.find('#fullName').setValue('Mina C.')
    await wrapper.find('#phone').setValue('0999999999')
    await wrapper.find('form').trigger('submit')
    await settle()

    expect(shopApiMock.userApi.update).toHaveBeenCalledWith(
      expect.objectContaining({ fullName: 'Mina C.', phone: '0999999999', username: 'mina' }),
    )
    expect(useAuthStore(pinia).user?.fullName).toBe('Mina C.')
    expect(lastToast(pinia)?.message).toBe('Profile updated')
    expect(router.currentRoute.value.query.edit).toBeUndefined()
  })

  it('shows the server message when saving fails', async () => {
    shopApiMock.userApi.update.mockRejectedValue(axiosErr(409, { message: 'Username is already taken.' }))
    const { wrapper } = await open('/profile?edit=1')
    await wrapper.find('form').trigger('submit')
    await settle()
    expect(wrapper.find('[role="alert"]').text()).toBe('Username is already taken.')
  })

  it('cancel restores the original values', async () => {
    const { wrapper, router } = await open('/profile?edit=1')
    await wrapper.find('#fullName').setValue('Changed')
    await wrapper.findAll('button').find((b) => b.text() === 'Cancel')!.trigger('click')
    await settle()
    expect(input(wrapper, 'fullName').value).toBe('Mina Chan')
    expect(router.currentRoute.value.query.edit).toBeUndefined()
  })

  describe('avatar upload', () => {
    const pick = async (wrapper: Awaited<ReturnType<typeof open>>['wrapper'], file: File) => {
      const el = wrapper.find('#avatar').element as HTMLInputElement
      Object.defineProperty(el, 'files', { value: [file], configurable: true })
      await new DOMWrapper(el).trigger('change')
      await settle()
    }

    it('rejects unsupported file types', async () => {
      const { wrapper, pinia } = await open('/profile?edit=1')
      await pick(wrapper, new File(['x'], 'a.gif', { type: 'image/gif' }))
      expect(lastToast(pinia)?.message).toContain('JPEG, PNG or WebP')
      expect(shopApiMock.userApi.uploadAvatar).not.toHaveBeenCalled()
    })

    it('rejects files over 5 MB', async () => {
      const { wrapper, pinia } = await open('/profile?edit=1')
      const big = new File([new Uint8Array(5 * 1024 * 1024 + 1)], 'big.png', { type: 'image/png' })
      await pick(wrapper, big)
      expect(lastToast(pinia)?.message).toContain('5 MB')
      expect(shopApiMock.userApi.uploadAvatar).not.toHaveBeenCalled()
    })

    it('uploads a valid image and updates the profile', async () => {
      shopApiMock.userApi.uploadAvatar.mockResolvedValue(user({ profileImage: 'data:image/png;base64,AAA' }))
      const { wrapper, pinia } = await open('/profile?edit=1')
      await pick(wrapper, new File(['x'], 'me.png', { type: 'image/png' }))
      expect(useAuthStore(pinia).user?.profileImage).toBe('data:image/png;base64,AAA')
      expect(lastToast(pinia)?.message).toBe('Photo updated')
    })

    it('reports an upload failure', async () => {
      shopApiMock.userApi.uploadAvatar.mockRejectedValue(axiosErr(500, { message: 'Upload failed' }))
      const { wrapper, pinia } = await open('/profile?edit=1')
      await pick(wrapper, new File(['x'], 'me.png', { type: 'image/png' }))
      expect(lastToast(pinia)?.message).toBe('Upload failed')
    })
  })

  it('logs out, clears the cart and returns to the login page', async () => {
    const { wrapper, router, pinia } = await open()
    useCartStore(pinia).count = 4
    await wrapper.findAll('button').find((b) => b.text() === 'Log out')!.trigger('click')
    await settle()
    expect(useAuthStore(pinia).isLoggedIn).toBe(false)
    expect(useCartStore(pinia).count).toBe(0)
    expect(router.currentRoute.value.path).toBe('/login')
  })
})
