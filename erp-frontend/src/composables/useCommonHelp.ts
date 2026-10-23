import { reactive, ref } from 'vue'
import { useAuthStore } from '@/stores/authStore'
import type { ModalProps } from '@/types/modal'

export function useCommonHelp() {
  const authStore = useAuthStore()
  const modalVisible = ref(false)
  const modalProps = reactive<ModalProps>({
    title: '', path: '', defaultField: '', columns: [], data: {}, onConfirm: () => {}, type: 'table'
  })

  const openHelp = (type: string, callback: (data: any) => void, extraData: any = {}) => {
    if (type === 'CUST') {
      Object.assign(modalProps, {
        title: '거래처 선택', path: '/erp/customer', defaultField: 'custnm', large: true,
        data: { keyword: '' },
        columns: [
          { title: '거래처코드', field: 'custcd', width: 120, hozAlign: 'center', headerSort: false },
          { title: '거래처명', field: 'custnm', minWidth: 250, widthGrow: 1, hozAlign: 'left', headerSort: false },
          { title: '대표자', field: 'ceo', width: 120, hozAlign: 'center' },
          { title: '전화번호', field: 'telno', width: 150, hozAlign: 'center' }
        ],
        onConfirm: callback
      })
    } else if (type === 'ITEM') {
      Object.assign(modalProps, {
        title: extraData.title || '품목 선택', path: '/erp/item', defaultField: 'itemnm', large: true,
        data: { keyword: '' },
        columns: [
          { title: '품목코드', field: 'itemcd', width: 120, hozAlign: 'center' },
          { title: '품목명', field: 'itemnm', minWidth: 250, widthGrow: 1, hozAlign: 'left' },
          { title: '규격', field: 'itsize', width: 150 },
          { title: '단위', field: 'unit', width: 80, hozAlign: 'center' }
        ],
        onConfirm: callback
      })
    } else {
      const gubun = extraData.gubun || 'D0'
      const table = extraData.table || (gubun.startsWith('W') || gubun.startsWith('T') ? 'hsba' : 'haba')
      Object.assign(modalProps, {
        title: extraData.title || '공통코드 선택', path: '/erp/code', defaultField: 'codenm', large: false,
        data: { table: table, gbncd: gubun, keyword: '' },
        columns: [
          { title: '코드', field: 'code', width: 100, hozAlign: 'center' },
          { title: '명칭', field: 'codenm', minWidth: 200, widthGrow: 1, hozAlign: 'left' }
        ],
        onConfirm: callback
      })
    }
    modalVisible.value = true
  }

  return { modalVisible, modalProps, openHelp }
}
