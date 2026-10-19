<template>
  <!-- 🚀 주소 검색은 외부 서비스를 사용하므로 HelpBase를 쓰지 않고 개별 구성하여 일관성 유지 -->
  <div v-if="visible" class="modal fade show d-block" tabindex="-1" style="background: rgba(0, 0, 0, 0.5); z-index: 1070; backdrop-filter: blur(2px);">
    <div class="modal-dialog modal-dialog-centered">
      <div class="modal-content border-0 shadow-lg" style="border-radius: 8px; overflow: hidden;">
        <div class="modal-header py-2 bg-white border-bottom shadow-sm">
          <h5 class="modal-title fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
            <i class="bi bi-geo-alt-fill text-primary me-2"></i> 주소 검색
          </h5>
          <button type="button" class="btn-close shadow-none" style="font-size: 10px;" tabindex="-1" @click="close"></button>
        </div>
        <div class="modal-body p-0" style="height: 480px;">
          <div ref="postcodeWrapper" style="width:100%; height:100%;"></div>
        </div>
        <div class="modal-footer py-1 bg-white border-top text-end">
          <button class="btn btn-outline-secondary btn-sm px-4 fw-bold" @click="close">취소</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, nextTick } from 'vue'

const props = defineProps<{ visible: boolean }>()
const emit = defineEmits(['update:visible', 'confirm', 'close'])

const postcodeWrapper = ref<HTMLElement | null>(null)

const close = () => { emit('update:visible', false); emit('close'); }

watch(() => props.visible, async (isVisible) => {
  if (!isVisible) return
  await nextTick()

  if (!(window as any).daum) {
    alert('주소 서비스를 로드할 수 없습니다.');
    close();
    return;
  }

  new (window as any).daum.Postcode({
    oncomplete: (data: any) => {
      // 🚀 데이터 선택 시 부모에게 표준 객체 전달 후 즉시 종료
      emit('confirm', {
        postno: data.zonecode,
        address: data.address,
        roadAddress: data.roadAddress,
        jibunAddress: data.jibunAddress
      });
      close();
    },
    width: '100%',
    height: '100%'
  }).embed(postcodeWrapper.value);
})
</script>
