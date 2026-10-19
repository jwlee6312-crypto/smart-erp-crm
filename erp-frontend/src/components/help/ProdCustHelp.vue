<template>
  <HelpBase
    ref="baseRef"
    v-model:visible="visible_val"
    title="생산거래처 선택"
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

const props = defineProps<{ visible: boolean }>()
const emit = defineEmits(['update:visible', 'confirm', 'close'])
const authStore = useAuthStore()

const baseRef = ref<any>(null)
const loading = ref(false)
const visible_val = computed({ get: () => props.visible, set: (v) => emit('update:visible', v) })

const columns = [
  { title: "거래처코드", field: "custcd", width: 120, hozAlign: "center" },
  { title: "거래처명", field: "custnm", minWidth: 200, widthGrow: 1 }
]

const fetchData = async (searchWord: string) => {
  loading.value = true;
  try {
    // 🚀 생산거래처 고유 구분값 C9 적용
    const res = await api.post('/ha00/HA00_00P_STR', { gubun: 'C9', cmpycd: authStore.cmpycd, codenm: searchWord })
    baseRef.value?.setData(res.data || [])
  } finally { loading.value = false }
}
</script>
