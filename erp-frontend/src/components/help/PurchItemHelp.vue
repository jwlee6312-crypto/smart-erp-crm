<template>
  <HelpBase
    ref="baseRef"
    v-model:visible="visible_val"
    title="구매품목 선택"
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
  { title: "품목코드", field: "itemcd", width: 120, hozAlign: "center" },
  { title: "품목명", field: "itemnm", minWidth: 200, widthGrow: 1 },
  { title: "규격", field: "itsize", width: 150 },
  { title: "단위", field: "unit", width: 80, hozAlign: "center" }
]

const fetchData = async (searchWord: string) => {
  loading.value = true;
  try {
    const res = await api.post('/hs00/HS00_000S_STR', { gubun: 'I1', cmpycd: authStore.cmpycd, gbncd: '3', codenm: searchWord })
    baseRef.value?.setData(res.data || [])
  } finally { loading.value = false }
}
</script>
