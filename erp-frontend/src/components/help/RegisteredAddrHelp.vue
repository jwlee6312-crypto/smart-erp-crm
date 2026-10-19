<template>
  <HelpBase
    ref="baseRef"
    v-model:visible="visible_val"
    title="기존 배송지 선택"
    :columns="columns"
    :large="true"
    :loading="loading"
    @search="fetchData"
    @confirm="(d) => $emit('confirm', d)"
    @close="$emit('close')"
  />
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { api } from '@/utils/axios'
import { useAuthStore } from '@/stores/authStore'
import HelpBase from './HelpBase.vue'

const props = defineProps<{ visible: boolean, custcd?: string }>()
const emit = defineEmits(['update:visible', 'confirm', 'close'])
const authStore = useAuthStore()

const baseRef = ref<any>(null)
const loading = ref(false)
const visible_val = computed({ get: () => props.visible, set: (v) => emit('update:visible', v) })

const columns = [
  { title: "코드", field: "trancd", width: 80, hozAlign: "center" },
  { title: "배송처명", field: "custnm", width: 150 },
  { title: "우편번호", field: "postno", width: 80, hozAlign: "center" },
  { title: "주소", field: "address", minWidth: 250, widthGrow: 1 },
  { title: "상세주소", field: "d_address", width: 150 }
]

const fetchData = async (searchWord: string) => {
  loading.value = true;
  try {
    // 🚀 T0 구분과 거래처코드(custcd) 필터를 결합하여 해당 업체 전용 주소록 조회
    const res = await api.post('/hs00/HS00_000S_STR', {
      gubun: 'T0',
      cmpycd: authStore.cmpycd,
      code: props.custcd || '',
      codenm: searchWord
    })
    baseRef.value?.setData(res.data || [])
  } finally { loading.value = false }
}
</script>
