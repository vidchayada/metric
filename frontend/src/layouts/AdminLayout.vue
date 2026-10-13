<script setup lang="ts">
import AdminNavbar from '@/components/layout/AdminNavbar.vue'
</script>

<template>
  <div class="min-h-screen bg-blush-50/40">
    <AdminNavbar />
    <main class="mx-auto max-w-7xl px-4 py-8 lg:px-8"><RouterView /></main>
  </div>
</template>
