<template>
	<div class="d-flex gap-1 w-100 align-items-center">
		<!-- 📦 배송처 코드 및 우편번호 그룹 (너비 최적화) -->
		<div class="d-flex gap-1 flex-shrink-0" style="width: 185px;">
			<!-- 배송처 코드 (trancd) -->
			<input
				v-model="trancd_val"
				type="text"
				class="form-control form-control-sm text-center bg-light fw-bold text-primary"
				style="width: 45px;"
				placeholder="코드"
				readonly
				tabindex="-1"
			/>
			<!-- 우편번호 (postno) -->
			<input
				v-model="postno_val"
				type="text"
				class="form-control form-control-sm text-center bg-light px-1"
				style="width: 65px;"
				placeholder="우편번호"
				maxlength="6"
				readonly
				tabindex="-1"
			/>
			<!-- 1️⃣ 신규 주소 검색 (돋보기) -->
			<button
				type="button"
				class="btn btn-sm btn-dark px-2"
				title="다음 주소 검색"
				@click="openPostcode"
				:tabindex="tabindex"
			>
				<i class="bi bi-search"></i>
			</button>
			<!-- 2️⃣ 기존 배송지 팝업 (리스트) -->
			<button
				type="button"
				class="btn btn-sm btn-outline-secondary px-2"
				title="기존 배송지 선택"
				@click="emit('open-address')"
				:tabindex="tabindex ? Number(tabindex) + 1 : -1"
			>
				<i class="bi bi-list-stars"></i>
			</button>
		</div>

		<!-- 🏠 기본주소 (자동 확장) -->
		<input
			v-model="address_val"
			type="text"
			class="form-control form-control-sm flex-grow-1 bg-light"
			style="min-width: 150px;"
			placeholder="기본주소"
			readonly
			tabindex="-1"
		/>

		<!-- ✏️ 상세주소 (고정 비율 확보) -->
		<input
			v-model="d_address_val"
			type="text"
			class="form-control form-control-sm"
			style="width: 25%; min-width: 120px;"
			placeholder="상세주소 입력"
			:tabindex="tabindex ? Number(tabindex) + 2 : -1"
		/>
	</div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps({
	trancd: [String, Number],
	postno: String,
	address: String,
	d_address: String,
	tabindex: [String, Number] // 🚀 부모로부터 전달받은 시작 탭 순서
})

const emit = defineEmits(['update:trancd', 'update:postno', 'update:address', 'update:d_address', 'open-address'])

const trancd_val = computed({ get: () => props.trancd, set: (val) => emit('update:trancd', val) })
const postno_val = computed({ get: () => props.postno, set: (val) => emit('update:postno', val) })
const address_val = computed({ get: () => props.address, set: (val) => emit('update:address', val) })
const d_address_val = computed({ get: () => props.d_address, set: (val) => emit('update:d_address', val) })

const openPostcode = () => {
	if (!(window as any).daum) { alert('주소 서비스를 로드할 수 없습니다.'); return }
	new (window as any).daum.Postcode({
		oncomplete: (data: any) => {
			trancd_val.value = ''
			postno_val.value = data.zonecode
			address_val.value = data.address
			d_address_val.value = ''
		},
	}).open()
}
</script>
