<script setup lang="ts">
import NewsletterForm from '@/components/common/NewsletterForm.vue'

const customer = ['Start a Return', 'Return Policy', 'FAQ', 'Catalogs and Mailers', 'About Group Gifting']
const company = ['About Us', 'Sustainability', 'Careers', 'Privacy Policy', 'Terms']
const heading = 'mb-4 text-xs font-medium tracking-widest uppercase'
</script>

<template>
  <footer class="mt-24 border-t border-line bg-blush-50">
    <div class="mx-auto grid max-w-7xl grid-cols-2 gap-8 px-4 py-14 lg:grid-cols-4 lg:px-8">
      <div>
        <h6 :class="heading">Contact us</h6>
        <ul class="space-y-2 text-sm text-muted">
          <li>+66 82-146-1435</li>
          <li><a href="mailto:hello@girlette.com" class="hover:text-ink">Email Us</a></li>
          <li>Mon–Fri 9am–3pm (UTC+7)</li>
        </ul>
      </div>
      <div>
        <h6 :class="heading">Customer</h6>
        <ul class="space-y-2 text-sm text-muted"><li v-for="l in customer" :key="l"><a href="#" class="hover:text-ink">{{ l }}</a></li></ul>
      </div>
      <div>
        <h6 :class="heading">Company</h6>
        <ul class="space-y-2 text-sm text-muted"><li v-for="l in company" :key="l"><a href="#" class="hover:text-ink">{{ l }}</a></li></ul>
      </div>
      <div class="col-span-2 lg:col-span-1">
        <h6 :class="heading">Get the latest news from us</h6>
        <NewsletterForm />
      </div>
    </div>
    <div class="border-t border-line py-4 text-center text-xs text-muted">©2025 Girlette</div>
  </footer>
</template>
