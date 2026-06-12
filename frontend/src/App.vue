<script setup>
import PasskeyAuth from './components/PasskeyAuth.vue'
</script>

<template>
  <main class="main-layout">
    <PasskeyAuth />
  </main>
</template>

<style>
body {
  margin: 0;
  padding: 0;
  background-color: #0b0c0f;
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
}
.main-layout {
  width: 100%;
}
</style>

