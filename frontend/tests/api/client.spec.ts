import { afterEach, describe, expect, it, vi } from 'vitest'
import type { AxiosResponse, InternalAxiosRequestConfig } from 'axios'
import { apiError, fieldErrors, http, setUnauthorizedHandler, statusOf, TOKEN_KEY } from '@/api/client'
import { axiosErr } from '../helpers'

type Adapter = (config: InternalAxiosRequestConfig) => Promise<AxiosResponse>
const original = http.defaults.adapter

function useAdapter(fn: Adapter) {
  http.defaults.adapter = fn as never
}

const ok = (config: InternalAxiosRequestConfig, data: unknown = {}): AxiosResponse =>
  ({ data, status: 200, statusText: 'OK', headers: {}, config }) as AxiosResponse

describe('http client', () => {
  afterEach(() => {
    http.defaults.adapter = original
    localStorage.clear()
    setUnauthorizedHandler(() => {})
  })

  it('uses the /api base path', () => {
    expect(http.defaults.baseURL).toBe('/api')
  })

  it('adds the Bearer token when one is stored', async () => {
    localStorage.setItem(TOKEN_KEY, 'abc')
    let header: unknown
    useAdapter(async (config) => {
      header = config.headers.Authorization
      return ok(config)
    })
    await http.get('/x')
    expect(header).toBe('Bearer abc')
  })

  it('sends no Authorization header without a token', async () => {
    let header: unknown = 'unset'
    useAdapter(async (config) => {
      header = config.headers.Authorization
      return ok(config)
    })
    await http.get('/x')
    expect(header).toBeUndefined()
  })

  it('calls the unauthorized handler on 401 when a token exists', async () => {
    localStorage.setItem(TOKEN_KEY, 'abc')
    const handler = vi.fn()
    setUnauthorizedHandler(handler)
    useAdapter(async () => {
      throw axiosErr(401)
    })
    await expect(http.get('/x')).rejects.toBeDefined()
    expect(handler).toHaveBeenCalledOnce()
  })

  it('ignores 401 when there is no token (e.g. failed login)', async () => {
    const handler = vi.fn()
    setUnauthorizedHandler(handler)
    useAdapter(async () => {
      throw axiosErr(401)
    })
    await expect(http.get('/x')).rejects.toBeDefined()
    expect(handler).not.toHaveBeenCalled()
  })

  it('does not treat other errors as unauthorized', async () => {
    localStorage.setItem(TOKEN_KEY, 'abc')
    const handler = vi.fn()
    setUnauthorizedHandler(handler)
    useAdapter(async () => {
      throw axiosErr(500)
    })
    await expect(http.get('/x')).rejects.toBeDefined()
    expect(handler).not.toHaveBeenCalled()
  })
})

describe('error helpers', () => {
  it('apiError prefers the server message, then the fallback', () => {
    expect(apiError(axiosErr(400, { message: 'Bad thing' }))).toBe('Bad thing')
    expect(apiError(axiosErr(400, {}), 'custom')).toBe('custom')
    expect(apiError(new Error('x'))).toBe('Something went wrong. Please try again.')
  })

  it('fieldErrors returns the errors map or an empty object', () => {
    expect(fieldErrors(axiosErr(409, { errors: { email: 'taken' } }))).toEqual({ email: 'taken' })
    expect(fieldErrors(axiosErr(409, {}))).toEqual({})
    expect(fieldErrors('nope')).toEqual({})
  })

  it('statusOf reads the HTTP status of axios errors only', () => {
    expect(statusOf(axiosErr(404))).toBe(404)
    expect(statusOf(new Error('x'))).toBeUndefined()
  })
})
