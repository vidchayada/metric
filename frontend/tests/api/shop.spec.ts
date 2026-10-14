import { afterEach, describe, expect, it, vi } from 'vitest'
import { http } from '@/api/client'
import { authApi, cartApi, catalogApi, contactApi, userApi } from '@/api/shop'

const reply = (data: unknown) => ({ data }) as never

describe('shop api', () => {
  afterEach(() => vi.restoreAllMocks())

  it('login posts credentials and returns the body', async () => {
    const post = vi.spyOn(http, 'post').mockResolvedValue(reply({ token: 't', user: { id: 1 } }))
    const res = await authApi.login('mina', 'pw')
    expect(post).toHaveBeenCalledWith('/auth/login', { username: 'mina', password: 'pw' })
    expect(res.token).toBe('t')
  })

  it('register and me', async () => {
    const post = vi.spyOn(http, 'post').mockResolvedValue(reply(null))
    await authApi.register({ username: 'a', email: 'a@b.co', password: 'password1' })
    expect(post).toHaveBeenCalledWith('/auth/register', { username: 'a', email: 'a@b.co', password: 'password1' })

    const get = vi.spyOn(http, 'get').mockResolvedValue(reply({ id: 2 }))
    expect(await authApi.me()).toEqual({ id: 2 })
    expect(get).toHaveBeenCalledWith('/auth/me')
  })

  it('updates the profile and uploads an avatar as multipart', async () => {
    const put = vi.spyOn(http, 'put').mockResolvedValue(reply({ id: 1 }))
    const form = { fullName: 'A', username: 'a', email: 'a@b.co', phone: '', dateOfBirth: '', address: '' }
    await userApi.update(form)
    expect(put).toHaveBeenCalledWith('/users/me', form)

    const post = vi.spyOn(http, 'post').mockResolvedValue(reply({ id: 1 }))
    const file = new File(['x'], 'a.png', { type: 'image/png' })
    await userApi.uploadAvatar(file)
    const [url, body] = post.mock.calls[0] as [string, FormData]
    expect(url).toBe('/users/me/avatar')
    expect(body.get('avatar')).toBe(file)
  })

  it('catalog endpoints', async () => {
    const get = vi.spyOn(http, 'get').mockResolvedValue(reply('data'))
    await catalogApi.home()
    await catalogApi.categories()
    await catalogApi.products({ page: 2, size: 15, category: 'tops' })
    await catalogApi.product(7)
    expect(get.mock.calls).toEqual([
      ['/home'],
      ['/categories'],
      ['/products', { params: { page: 2, size: 15, category: 'tops' } }],
      ['/products/7'],
    ])
  })

  it('cart endpoints', async () => {
    vi.spyOn(http, 'get').mockResolvedValueOnce(reply([{ id: 1 }])).mockResolvedValueOnce(reply({ count: 4 }))
    const post = vi.spyOn(http, 'post').mockResolvedValue(reply({ orderNumber: '#1' }))
    const patch = vi.spyOn(http, 'patch').mockResolvedValue(reply(null))
    const del = vi.spyOn(http, 'delete').mockResolvedValue(reply(null))

    expect(await cartApi.list()).toEqual([{ id: 1 }])
    expect(await cartApi.count()).toBe(4)
    await cartApi.add(1, 2, 3)
    await cartApi.setQuantity(9, 5)
    await cartApi.remove(9)
    expect(await cartApi.checkout()).toEqual({ orderNumber: '#1' })

    expect(post).toHaveBeenCalledWith('/cart/items', { productId: 1, attributeId: 2, quantity: 3 })
    expect(patch).toHaveBeenCalledWith('/cart/items/9', { quantity: 5 })
    expect(del).toHaveBeenCalledWith('/cart/items/9')
    expect(post).toHaveBeenCalledWith('/orders/checkout', {
      shippingAddress: 'Default Address',
    })
  })

  it('subscribes an email', async () => {
    const post = vi.spyOn(http, 'post').mockResolvedValue(reply({ message: 'ok' }))
    expect(await contactApi.subscribe('a@b.co')).toEqual({ message: 'ok' })
    expect(post).toHaveBeenCalledWith('/subscribers', { email: 'a@b.co' })
  })
})
