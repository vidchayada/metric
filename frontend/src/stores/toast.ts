import { ref } from 'vue'
import { defineStore } from 'pinia'

export type ToastKind = 'success' | 'error' | 'info'
export interface Toast {
    id: number
    message: string
    kind: ToastKind
}

export const useToastStore = defineStore('toast', () => {
    const toasts = ref<Toast[]>([])
    let nextId = 1

    function dismiss(id: number) {
        toasts.value = toasts.value.filter((t) => t.id !== id)
    }

    function push(message: string, kind: ToastKind = 'info', duration = 3000) {
        const id = nextId++
        toasts.value.push({ id, message, kind })
        if (duration > 0) setTimeout(() => dismiss(id), duration)
        return id
    }

    return {
        toasts,
        push,
        dismiss,
        success: (m: string) => push(m, 'success'),
        error: (m: string) => push(m, 'error', 4000),
    }
})
