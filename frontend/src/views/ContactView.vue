<script setup lang="ts">
import NewsletterForm from '@/components/common/NewsletterForm.vue'

const map =
    'https://maps.google.com/maps?q=Chiang+Mai+University,239+Huay+Kaew+Road+Suthep+Muang+Chiang+Mai+50200&output=embed'
const company = ['About Us', 'Sustainability', 'Careers', 'Privacy Policy', 'Terms']
const customers = ['Start a Return', 'Return Policy', 'FAQ', 'Catalogs & Mailers', 'About Group Gifting']
</script>

<template>
  <div class="mx-auto max-w-7xl px-4 py-12 lg:px-8">
    <div class="grid gap-6 md:grid-cols-[2fr_1fr]">
      <iframe :src="map" title="Girlette store location" loading="lazy" class="h-80 w-full rounded-2xl border-0" allowfullscreen referrerpolicy="no-referrer-when-downgrade" />
      <div class="flex flex-col justify-center rounded-2xl bg-blush-50 p-8">
        <p class="field-label">Visit Us</p>
        <address class="leading-relaxed not-italic">239 Huay Kaew Road, Suthep,<br />Mueang Chiang Mai, Chiang Mai, 50200</address>
      </div>
    </div>

    <div class="mt-14 grid gap-10 md:grid-cols-3">
      <div>
        <p class="field-label">Contact Us</p>
        <p class="text-lg">+66 82-146-1435</p>
        <a href="mailto:hello@girlette.com" class="mt-1 block underline">Email Us</a>
        <p class="mt-2 text-sm text-muted">Mon – Fri 9am – 3pm (UTC+7)</p>
      </div>
      <div class="grid grid-cols-2 gap-6">
        <div>
          <p class="field-label">Company</p>
          <ul class="space-y-2 text-sm text-muted"><li v-for="l in company" :key="l"><a href="#" class="hover:text-ink">{{ l }}</a></li></ul>
        </div>
        <div>
          <p class="field-label">Customers</p>
          <ul class="space-y-2 text-sm text-muted"><li v-for="l in customers" :key="l"><a href="#" class="hover:text-ink">{{ l }}</a></li></ul>
        </div>
      </div>
      <div>
        <p class="field-label">Get the latest from us</p>
        <NewsletterForm />
      </div>
    </div>
  </div>
</template>
