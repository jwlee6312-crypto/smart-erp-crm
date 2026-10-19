<template>
	<div class="d-flex align-items-center gap-1">
		<input
			:value="fromdt"
			type="date"
			class="form-control form-control-sm"
			@input="onInputFrom"
		/>
		<span>~</span>
		<input
			:value="todt"
			type="date"
			class="form-control form-control-sm"
			@input="onInputTo"
		/>
	</div>
</template>

<script setup lang="ts">
const props = defineProps<{
	fromdt: string
	todt: string
}>()

const emit = defineEmits(['update:fromdt', 'update:todt'])

const onInputFrom = (e: Event) => {
	emit('update:fromdt', (e.target as HTMLInputElement).value)
}

const onInputTo = (e: Event) => {
	emit('update:todt', (e.target as HTMLInputElement).value)
}
</script>

<style scoped>
.form-control {
	width: 130px;
}
</style>
