// src/utils/validation.ts
const EMAIL_PATTERN = /^[^\s@]+@[^\s@.]+(?:\.[^\s@.]+)+$/

export function isValidEmail(value: string): boolean {
    return EMAIL_PATTERN.test(value.trim())
}