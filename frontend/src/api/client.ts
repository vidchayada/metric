import axios, { AxiosError } from 'axios'

export const TOKEN_KEY = 'girlette_token'

export const http = axios.create({ baseURL: '/api' })

let onUnauthorized: (() => void) | null = null

/** The auth store registers a handler so an expired token logs the user out. */
export function setUnauthorizedHandler(handler: () => void) {
    onUnauthorized = handler
}

http.interceptors.request.use((config) => {
    const token = localStorage.getItem(TOKEN_KEY)
    if (token) config.headers.Authorization = `Bearer ${token}`
    return config
})

http.interceptors.response.use(
    (res) => res,
    (error: AxiosError) => {
        if (error.response?.status === 401 && localStorage.getItem(TOKEN_KEY)) {
            onUnauthorized?.()
        }
        return Promise.reject(error)
    },
)

interface ApiErrorBody {
    message?: string
    errors?: Record<string, string>
}

export function apiError(err: unknown, fallback = 'Something went wrong. Please try again.'): string {
    if (axios.isAxiosError<ApiErrorBody>(err)) return err.response?.data?.message ?? fallback
    return fallback
}

export function fieldErrors(err: unknown): Record<string, string> {
    if (axios.isAxiosError<ApiErrorBody>(err)) return err.response?.data?.errors ?? {}
    return {}
}

export function statusOf(err: unknown): number | undefined {
    return axios.isAxiosError(err) ? err.response?.status : undefined
}
