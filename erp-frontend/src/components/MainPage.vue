<template>
	<div class="main-page d-flex flex-grow-1">
		<SideMenu :items="sidebarItems" />
		<MainContent />
	</div>
</template>

<script setup lang="ts">
import SideMenu from './SideMenu.vue'
import MainContent from '@/views/MainContent.vue'

defineProps({
	sidebarItems: Array,
})
</script>
