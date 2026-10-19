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
    const commonPath = '/ha00/HA00_00P_STR'

    if (type === 'DEPT') {
      Object.assign(modalProps, {
        title: '부서 선택', path: commonPath, defaultField: 'deptnm', large: false,
        data: { gubun: 'D0', cmpycd: authStore.cmpycd, gbncd: extraData.gbncd || '', code: extraData.search || '', remark: '' },
        columns: [
          { title: '부서코드', field: 'deptcd', width: 120, hozAlign: 'center', headerSort: false },
          { title: '부서명', field: 'deptnm', minWidth: 200, widthGrow: 1, hozAlign: 'left', headerSort: false }
        ],
        onConfirm: callback
      })
    } else if (type === 'CUST') {
      const gubun = (extraData.gubun || 'C4').toUpperCase()
      Object.assign(modalProps, {
        title: '거래처 선택', path: commonPath, defaultField: 'custnm', large: true,
        data: { gubun: gubun, cmpycd: authStore.cmpycd, gbncd: extraData.gbncd || '', code: extraData.search || '', remark: '' },
        columns: [
          { title: '거래처코드', field: 'custcd', width: 120, hozAlign: 'center', headerSort: false },
          { title: '거래처명', field: 'custnm', minWidth: 250, widthGrow: 1, hozAlign: 'left', headerSort: false }
        ],
        onConfirm: callback
      })
    } else if (type === 'MGT') {
      Object.assign(modalProps, {
        title: extraData.title || '관리번호(계좌/카드) 선택', path: commonPath, defaultField: 'mgtnm', large: true,
        data: { gubun: 'M0', cmpycd: authStore.cmpycd, gbncd: extraData.mgtgbn || '', code: extraData.search || '', remark: extraData.acctcd || '' },
        columns: [
          { title: '계좌/카드/관리번호', field: 'mgtno', width: 180, hozAlign: 'center', headerSort: false, mutator: (v: any, d: any) => v || d.col0 || d.mgtno },
          { title: '명칭/예금주명', field: 'mgtnm', minWidth: 180, widthGrow: 1, hozAlign: 'left', mutator: (v: any, d: any) => v || d.col1 || d.mgtnm },
          { title: '은행/카드사', field: 'bankcd', width: 120, hozAlign: 'center', mutator: (v: any, d: any) => v || d.col2 || d.banknm },
          { title: '비고/계좌정보', field: 'remark', minWidth: 150, hozAlign: 'left', mutator: (v: any, d: any) => v || d.col3 || d.gujoa }
        ],
        onConfirm: callback
      })
    } else if (type === 'OFFSET') {
      Object.assign(modalProps, {
        title: '대체대상 전표 선택', path: commonPath, defaultField: 'REMARK', large: true,
        data: { gubun: 'P1', cmpycd: authStore.cmpycd, gbncd: extraData.dacctcd || '', code: extraData.custcd || '', remark: '' },
        columns: [
          { title: '전표일자', field: 'SLIPYMD', width: 100, hozAlign: 'center' },
          { title: '전표번호', field: 'SLIPNO', width: 120, hozAlign: 'center' },
          { title: '순번', field: 'SROWNO', width: 60, hozAlign: 'center' },
          { title: '적요', field: 'REMARK', minWidth: 200, widthGrow: 1 },
          { title: '거래처', field: 'CUSTNM', width: 150 },
          { title: '미결잔액', field: 'JANAMT', width: 110, hozAlign: 'right', formatter: 'money', formatterParams: { precision: 0 } }
        ],
        onConfirm: callback
      })
    } else if (type === 'ACCT') {
      const gubun = (extraData.gubun || 'A0').toUpperCase()
      const isMethod = gubun === 'E0'
      Object.assign(modalProps, {
        title: isMethod ? '지불수단 선택' : '계정과목 선택', path: commonPath, defaultField: isMethod ? 'codenm' : 'acctnm', large: true,
        data: { gubun: gubun, cmpycd: authStore.cmpycd, gbncd: extraData.gbncd || '', code: extraData.search || '', remark: '' },
        columns: [
          { title: '코드', field: 'acctcd', width: 100, hozAlign: 'center' },
          { title: '명칭', field: isMethod ? 'codenm' : 'acctnm', minWidth: 200, widthGrow: 1 },
          { title: '비고', field: isMethod ? 'remark' : 'cacctnm', width: 150 }
        ],
        onConfirm: callback
      })
    } else if (type === 'ITEM') {
      const gubun = extraData.gubun || 'I1'
      const path = gubun.startsWith('I') ? '/hp00/HP00_000S_STR' : commonPath
      Object.assign(modalProps, {
        title: extraData.title || '품목 선택', path: path, defaultField: 'itemnm', large: true,
        data: { gubun: gubun, cmpycd: authStore.cmpycd, gbncd: extraData.gbncd || extraData.codegbn || '', code: extraData.search || '', remark: extraData.iogbn || '' },
        columns: [
          { title: '품목코드', field: 'itemcd', width: 120, hozAlign: 'center' },
          { title: '품목명', field: 'itemnm', minWidth: 250, widthGrow: 1, hozAlign: 'left' },
          { title: '규격', field: 'itsize', width: 150 },
          { title: '단위', field: 'unit', width: 80, hozAlign: 'center' }
        ],
        onConfirm: callback
      })
    } else if (type === 'WH') {
      Object.assign(modalProps, {
        title: '창고 선택', path: '/hs00/HS00_000S_STR', defaultField: 'whnm', large: false,
        data: { gubun: extraData.gubun || 'W0', cmpycd: authStore.cmpycd },
        columns: [
          { title: '창고코드', field: 'whcd', width: 100, hozAlign: 'center' },
          { title: '창고명', field: 'whnm', minWidth: 150, widthGrow: 1, hozAlign: 'left' }
        ],
        onConfirm: callback
      })
    } else if (type === 'JOB_ORDER') {
      Object.assign(modalProps, {
        title: '작업지시 선택',
        path: '/hpio/HPIO_250U_POP',
        defaultField: 'lotno',
        large: true,
        searchDate: 'ordymd',
        data: {
          cmpycd: authStore.cmpycd,
          linecd: extraData.linecd || '',
          frymd: extraData.frymd || '',
          toymd: extraData.toymd || ''
        },
        columns: [
          { title: '지시일자', field: 'lotymd', width: 100, hozAlign: 'center' },
          { title: '지시번호', field: 'lotno', width: 120, hozAlign: 'center' },
          { title: '제품명', field: 'itemnm', minWidth: 200, widthGrow: 1 },
          { title: '지시량', field: 'ordqty', width: 100, hozAlign: 'right', formatter: 'money', formatterParams: { precision: 0 } },
          { title: '불출량', field: 'outqty', width: 100, hozAlign: 'right', formatter: 'money', formatterParams: { precision: 0 } },
          { title: '생산량', field: 'prodqty', width: 100, hozAlign: 'right', formatter: 'money', formatterParams: { precision: 0 } }
        ],
        onConfirm: callback
      })
    }
    modalVisible.value = true
  }

  return { modalVisible, modalProps, openHelp }
}
