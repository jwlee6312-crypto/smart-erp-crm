<template>
	<div class="d-flex gap-1">
		<!-- 우편번호 -->
		<div class="d-flex gap-1">
			<input
				v-model="postnoValue"
				type="text"
				class="form-control form-control-sm w-75"
				placeholder="우편번호"
				maxlength="6"
				readonly
			/>
			<button type="button" class="btn btn-sm btn-dark" :disabled="!active" @click="openPostcode">
				<i class="bi bi-search"></i>
			</button>
		</div>
		<!-- 기본주소 -->
		<input
			v-model="addressValue"
			type="text"
			class="form-control form-control-sm w-75"
			placeholder="기본주소"
			maxlength="100"
			readonly
		/>

		<!-- 상세주소 -->
		<input
			v-model="dAddressValue"
			type="text"
			class="form-control form-control-sm w-75"
			placeholder="상세주소"
			maxlength="100"
			:readonly="!active"
		/>
	</div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

// 🔄 Props 받기
const props = defineProps({
	postno: String,
	address: String,
	d_address: String,
	active: Boolean || null,
})

// 🔁 Emits
const emit = defineEmits(['update:postno', 'update:address', 'update:d_address'])

// 🔁 양방향 바인딩 computed
const postnoValue = computed({
	get: () => props.postno,
	set: (val) => emit('update:postno', val),
})

const addressValue = computed({
	get: () => props.address,
	set: (val) => emit('update:address', val),
})

const dAddressValue = computed({
	get: () => props.d_address,
	set: (val) => emit('update:d_address', val),
})

// 🔍 다음 주소 API
const openPostcode = () => {
	new daum.Postcode({
		oncomplete: function (data) {
			const fullAddress = data.address
			const zonecode = data.zonecode

			postnoValue.value = zonecode
			addressValue.value = fullAddress
			dAddressValue.value = '' // 상세주소 초기화
		},
	}).open()
}
</script>
