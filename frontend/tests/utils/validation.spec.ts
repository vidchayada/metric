import { describe, expect, it } from 'vitest'
import { isValidEmail } from '@/utils/validation'

describe('isValidEmail', () => {
  it.each(['a@b.co', 'first.last@mail.example.co.th', 'x+tag@sub.domain.org', '  a@b.co  '])('accepts %s', (v) => {
    expect(isValidEmail(v)).toBe(true)
  })

  it.each(['', 'plain', 'a@b', 'a@@b.co', '@b.co', 'a@.co', 'a@b..co', 'a b@c.co', 'a@b.c o'])('rejects %j', (v) => {
    expect(isValidEmail(v)).toBe(false)
  })

  it('stays fast on a pathological input', () => {
    const start = performance.now()
    isValidEmail(`a@${'b.'.repeat(50_000)}!`)
    expect(performance.now() - start).toBeLessThan(500)
  })
})
