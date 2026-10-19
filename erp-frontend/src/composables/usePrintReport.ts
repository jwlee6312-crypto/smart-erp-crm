import { api } from '@/utils/axios'
import { useAuthStore } from '@/stores/authStore'
import { numberToHanja } from '@/utils/hanja'

/**
 * 🚀 [공통 인쇄 콤포저블] 거래명세서, 입고의뢰서, 출고의뢰서 모듈화
 * 프로그램마다 중복 코딩 없이 (ioym, iono) 키 정보만 넘겨서 즉시 바코드 인쇄!
 */
export function usePrintReport() {
    const authStore = useAuthStore()

    const fC = (n: any) => Number(n || 0).toLocaleString()
    const fSaup = (v: any) => {
        const s = String(v || '').replace(/[^0-9]/g, '');
        return s.length === 10 ? `${s.substring(0,3)}-${s.substring(3,5)}-${s.substring(5)}` : (v || '');
    }
    const formatSaupno = fSaup
    const fDate = (v: any) => {
        const s = String(v || '').replace(/[^0-9]/g, '');
        return s.length === 8 ? `${s.substring(0,4)}-${s.substring(4,6)}-${s.substring(6,8)}` : (v || '');
    }
    const getVal = (obj: any, key: string) => {
        if (!obj) return ''
        const kLower = key.toLowerCase(); const kUpper = key.toUpperCase()
        const val = obj[kLower] !== undefined ? obj[kLower] : (obj[kUpper] !== undefined ? obj[kUpper] : '')
        return val === null ? '' : String(val).trim()
    }

    /** 📄 1. 거래명세서 일괄 출력 (공급자/공급받는자 보관용 2장 양식) */
    const printSpecification = async (rawIoym: string, rawIono: string, extraItems: any = [], masterInfoFallback: any = {}) => {
        const ioym = String(rawIoym || getVal(masterInfoFallback, 'ioym') || '').trim()
        const iono = String(rawIono || getVal(masterInfoFallback, 'iono') || '').trim()

        if (!ioym || !iono) {
            console.warn('⚠️ [printSpecification] ioym 또는 iono가 누락되었습니다.', { rawIoym, rawIono, masterInfoFallback })
            throw new Error('출력할 (년월, 번호) 키 정보가 없습니다.')
        }

        const win = window.open('', '_blank', 'width=850,height=950')
        if (!win) throw new Error('브라우저 팝업 차단을 해제해 주세요.')

        try {
            win.document.write('<div style="font-family:sans-serif; padding:20px; text-align:center;">거래명세서를 생성하는 중입니다...</div>')

            const [hRes, dRes, sInfoRes, stampRes] = await Promise.allSettled([
                api.post('/hsio/HSIO_TRANS_STR', { actkind: 'S1', cmpycd: authStore.cmpycd, ioym, iono }),
                api.post('/hsio/HSIO_TRANS_STR', { actkind: 'S0', cmpycd: authStore.cmpycd, ioym, iono }),
                api.post('/haba/HABA_900U_STR', { actkind: 'S0', cmpycd: authStore.cmpycd }),
                api.post('/haba/HABA_100U_STR', { actkind: 'S0', cmpycd: authStore.cmpycd })
            ])

            let hRaw = (hRes.status === 'fulfilled' && hRes.value.data?.length) ? hRes.value.data[0] : null
            let dtlData = (dRes.status === 'fulfilled' && dRes.value.data?.length) ? dRes.value.data : null

            // 🚀 [해결] HSIO_TRANS_STR 실패 시 HSIO_620S_STR로 안전 백업 조회
            if (!hRaw || !dtlData) {
                try {
                    const [hBack, dBack] = await Promise.all([
                        api.post('/hsio/HSIO_620S_STR', { actkind: 'S1', cmpycd: authStore.cmpycd, ioym, iono, iogbn: '200', whcd: '000', fromdt: '19000101', todt: '20991231', custcd: '', slipyn: 'Y' }),
                        api.post('/hsio/HSIO_620S_STR', { actkind: 'S0', cmpycd: authStore.cmpycd, ioym, iono, iogbn: '200', whcd: '000', fromdt: '19000101', todt: '20991231', custcd: '', slipyn: 'Y' })
                    ])
                    if (!hRaw && hBack.data?.length) hRaw = hBack.data[0]
                    if (!dtlData && dBack.data?.length) dtlData = dBack.data
                } catch(e) {}
            }

            if (!hRaw) hRaw = masterInfoFallback || {}

            // 🚀 [무결성 보장] dtl이 무조건 배열이 되도록 안전 검증
            let dtl: any[] = []
            if (Array.isArray(dtlData) && dtlData.length) {
                dtl = dtlData
            } else if (Array.isArray(extraItems) && extraItems.length) {
                dtl = extraItems
            } else if (extraItems && typeof extraItems === 'object' && Object.keys(extraItems).length) {
                dtl = [extraItems]
            }

            const cInfoRaw = (sInfoRes.status === 'fulfilled' && sInfoRes.value.data?.length) ? sInfoRes.value.data[0] : {}
            const stampImg = (stampRes.status === 'fulfilled' && stampRes.value.data?.length) ? getVal(stampRes.value.data[0], 'stampimg') : ''

            const cInfo = {
                saupno: getVal(cInfoRaw, 'saupno'), cmpynm: getVal(cInfoRaw, 'cmpynm'), bossnm: getVal(cInfoRaw, 'bossnm'),
                address: getVal(cInfoRaw, 'address'), uptae: getVal(cInfoRaw, 'uptae'), upjong: getVal(cInfoRaw, 'upjong')
            }

            const h = {
                ioymd: getVal(hRaw, 'ioymd') || getVal(masterInfoFallback, 'ioymd'),
                ioym: getVal(hRaw, 'ioym') || ioym,
                iono: getVal(hRaw, 'iono') || iono,
                custnm: getVal(hRaw, 'custnm') || getVal(masterInfoFallback, 'custnm'),
                remark: getVal(hRaw, 'remark') || getVal(masterInfoFallback, 'remark'),
                rcvamt: getVal(hRaw, 'rcvamt') || getVal(masterInfoFallback, 'rcvamt') || 0,
                banknm: getVal(hRaw, 'banknm') || getVal(masterInfoFallback, 'banknm'),
                gujano: getVal(hRaw, 'gujano') || getVal(masterInfoFallback, 'gujano'),
                gaibja: getVal(hRaw, 'gaibja') || getVal(masterInfoFallback, 'gaibja'),
                salsemp: getVal(hRaw, 'salsemp') || getVal(masterInfoFallback, 'usernm') || authStore.usernm
            }

            let totalAmt = 0, totalVat = 0
            dtl.forEach((i: any) => {
                totalAmt += Number(getVal(i, 'jsanamt') || getVal(i, 'ioamt') || 0);
                totalVat += Number(getVal(i, 'jsanvat') || getVal(i, 'iovat') || 0);
            })
            const totalSum = totalAmt + totalVat
            const stampUrl = stampImg ? `/api/storage/${authStore.cmpycd}/stampimg/${stampImg}` : ''

            const renderContent = (type: string) => {
                let rowsHtml = ''
                dtl.forEach((i: any) => {
                    const qtyPnt = Number(getVal(i, 'qtypnt') || 0)
                    const ioqty = Number(getVal(i, 'ioqty')) || 0
                    const jsanamt = Number(getVal(i, 'jsanamt') || getVal(i, 'ioamt')) || 0
                    const jsanvat = Number(getVal(i, 'jsanvat') || getVal(i, 'iovat')) || 0
                    const ioymd = getVal(i, 'ioymd') || h.ioymd
                    rowsHtml += `
                        <tr height="25">
                            <td align="center" style="font-size:8.5pt;">${ioymd ? ioymd.replace(/-/g, '').substring(4, 6) + '/' + ioymd.replace(/-/g, '').substring(6, 8) : ''}</td>
                            <td align="left" style="padding-left:3px; font-size:8.5pt; white-space:nowrap; overflow:hidden;">${getVal(i, 'itemnm')}</td>
                            <td align="center" style="font-size:8.5pt;">${getVal(i, 'itsize')}</td>
                            <td align="center" style="font-size:8.5pt;">${getVal(i, 'unit')}</td>
                            <td align="right" style="padding-right:3px; font-size:8.5pt;">${fC(ioqty)}</td>
                            <td align="right" style="padding-right:3px; font-size:8.5pt;">${fC(ioqty !== 0 ? Math.round(jsanamt/ioqty) : 0)}</td>
                            <td align="right" style="padding-right:3px; font-size:8.5pt;">${fC(jsanamt)}</td>
                            <td align="right" style="padding-right:3px; font-size:8.5pt;">${fC(jsanvat)}</td>
                        </tr>`
                })
                for (let k = dtl.length; k < 12; k++) rowsHtml += '<tr height="25"><td></td><td></td><td></td><td></td><td></td><td></td><td></td><td></td></tr>'

                return `
                    <div class="report-wrapper">
                        <table class="report-table">
                            <colgroup><col width="7%"><col width="23%"><col width="20%"><col width="5%"><col width="8%"><col width="11%"><col width="13%"><col width="13%"></colgroup>
                            <thead>
                                <tr><td colspan="8" class="title-cell"><div class="title-main">거&nbsp;&nbsp;래&nbsp;&nbsp;명&nbsp;&nbsp;세&nbsp;&nbsp;표</div><div class="title-sub">(${type} 보관용)</div></td></tr>
                                <tr height="120">
                                    <td colspan="2" class="header-left">
                                        <div class="header-date" style="font-size:8.5pt;"><b>${h.ioymd ? h.ioymd.replace(/-/g, '').substring(0, 4) + '년 ' + h.ioymd.replace(/-/g, '').substring(4, 6) + '월 ' + h.ioymd.replace(/-/g, '').substring(6, 8) + '일' : ''}</b>&nbsp;[${ioym}-${iono}]</div>
                                        <div class="header-cust" style="font-size:11pt;"><b>${h.custnm}</b>&nbsp;&nbsp;&nbsp;귀중</div>
                                    </td>
                                    <td colspan="6" style="padding:0;">
                                        <table style="width:100%; border-collapse:collapse; border:none; table-layout:fixed; height:100%;">
                                            <colgroup><col width="25"><col width="75"><col width="180"><col width="45"><col></colgroup>
                                            <tr height="26"><td rowspan="5" class="bg-gray" style="border:1px solid #000; font-size:8.5pt; width:25px;">공<br>급<br>자</td><td class="bg-gray" style="border:1px solid #000; font-size:8.5pt; width:75px;">등록번호</td><td colspan="3" style="border:1px solid #000; font-weight:bold; font-size:11pt; text-align:center; letter-spacing:1px;">${formatSaupno(cInfo.saupno)}</td></tr>
                                            <tr height="35"><td class="bg-gray" style="border:1px solid #000; font-size:8.5pt;">상&nbsp;&nbsp;&nbsp;&nbsp;호</td><td style="border:1px solid #000; font-weight:bold; font-size:9.5pt; padding-left:5px; overflow:hidden; white-space:nowrap;">${cInfo.cmpynm}</td><td class="bg-gray" style="width:40px; border:1px solid #000; font-size:8.5pt;">성&nbsp;명</td><td style="border:1px solid #000; font-weight:bold; position:relative; text-align:left; padding-left:10px; font-size:9.5pt;">${cInfo.bossnm}&nbsp;&nbsp;(인)${stampUrl ? `<img src="${stampUrl}" style="position:absolute; top:-10px; right:2px; width:48px; height:48px; object-fit:contain; mix-blend-mode:multiply; z-index:10;">` : ''}</td></tr>
                                            <tr height="35"><td class="bg-gray" style="border:1px solid #000; font-size:8.5pt;">사업장주소</td><td colspan="3" align="left" style="border:1px solid #000; font-size:8pt; padding-left:5px; line-height:1.2;">${cInfo.address}</td></tr>
                                            <tr height="24"><td class="bg-gray" style="border:1px solid #000; font-size:8.5pt;">업&nbsp;&nbsp;&nbsp;&nbsp;태</td><td style="border:1px solid #000; font-size:8.5pt; padding-left:3px;">${cInfo.uptae}</td><td class="bg-gray" style="border:1px solid #000; font-size:8.5pt;">종&nbsp;목</td><td style="border:1px solid #000; font-size:8.5pt; padding-left:3px;">${cInfo.upjong}</td></tr>
                                            <tr height="24"><td class="bg-gray" style="border:1px solid #000; font-size:8.5pt;">특기사항</td><td colspan="3" align="left" style="border:1px solid #000; font-size:8pt; padding-left:3px;">${h.remark || ''}</td></tr>
                                        </table>
                                    </td>
                                </tr>
                                <tr><td colspan="8" class="total-sum-row"><b>&nbsp; 합계금액(VAT 포함) : &nbsp;&nbsp;&nbsp;一金</b>&nbsp;<span class="hanja-amount">${numberToHanja(totalSum)}</span><b> 圓 整 </b>(&nbsp;${fC(totalSum)})</td></tr>
                                <tr class="bg-gray col-header" height="30"><td>월 일</td><td>품 명</td><td>규 격</td><td>단위</td><td>수량</td><td>단가</td><td>공급가액</td><td>세액</td></tr>
                            </thead>
                            <tbody>${rowsHtml}</tbody>
                            <tfoot>
                                <tr class="bg-gray footer-total"><td colspan="6">합&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;계</td><td align="right" style="padding-right:3px; font-size:8.5pt;">&#8361; ${fC(totalAmt)}</td><td align="right" style="padding-right:3px; font-size:8.5pt;">&#8361; ${fC(totalVat)}</td></tr>
                            </tfoot>
                        </table>
                    </div>`
            }

            const html = `<html><head><title>거래명세표 (${ioym}-${iono})</title><style>
                @page { size: A4; margin: 5mm; }
                body { font-family: 'GulimChe', '굴림체', monospace; font-size: 8.5pt; color: #000; margin: 0; padding: 15px; }
                .report-wrapper { width: 660px; margin: 0 auto; position: relative; }
                .page-break { page-break-after: always; height: 1px; }
                table.report-table { width: 100%; border-collapse: collapse; border: 2px solid #000; table-layout: fixed; }
                table.report-table td { border: 1px solid #000; padding: 2px; vertical-align: middle; }
                .title-cell { height: 50px; text-align: center; border-bottom: 2px solid #000 !important; }
                .title-main { font-size: 18pt; font-weight: bold; letter-spacing: 12px; }
                .title-sub { font-size: 9pt; margin-top: 3px; }
                .header-left { border-right: none !important; text-align: center; }
                .bg-gray { background-color: #f2f2f2; text-align: center; font-weight: bold; }
                .total-sum-row { text-align: left; padding-left: 15px; height: 32px; font-size: 9pt; border-top: 2px solid #000 !important; }
                .hanja-amount { font-family: 'BatangChe', serif; font-size: 10pt; font-weight: bold; }
                .col-header td { text-align: center; font-weight: bold; height: 28px; font-size: 8.5pt; }
                .footer-total td { text-align: center; font-weight: bold; height: 28px; font-size: 8.5pt; }
            </style></head>
            <body onload="window.print()">
                ${renderContent('공급받는자')}
                <div class="page-break"></div>
                <div style="margin-top: 30px;">
                    ${renderContent('공급자')}
                </div>
            </body></html>`

            win.document.open(); win.document.write(html); win.document.close();
        } catch(e: any) {
            console.error("❌ [printSpecification Error]:", e)
            if (win && !win.closed) {
                win.document.open()
                win.document.write(`
                <div style="font-family:sans-serif; color:red; padding:30px; line-height:1.6;">
                    <h2>❌ 거래명세서 서식 생성 중 오류 발생</h2>
                    <p style="font-weight:bold; font-size:14px;">오류 메시지: ${e?.message || e}</p>
                    <pre style="background:#f8f8f8; padding:10px; border:1px solid #ccc; font-size:11px;">${e?.stack || ''}</pre>
                </div>`)
                win.document.close()
            }
            throw e
        }
    }

    /** 🏷️ 2. 의뢰서 바코드 출력 (REQ_IN: 입고의뢰서, REQ_OUT: 출고의뢰서) */
    const printReportSheet = async (
        docType: 'REQ_IN' | 'REQ_OUT' | 'CERT_OUT',
        rawIoym: string,
        rawIono: string,
        extraItems: any = [],
        masterInfoFallback: any = {}
    ) => {
        const ioym = String(rawIoym || getVal(masterInfoFallback, 'ioym') || '').trim()
        const iono = String(rawIono || getVal(masterInfoFallback, 'iono') || '').trim()

        if (!ioym || !iono) {
            console.warn('⚠️ [printReportSheet] ioym 또는 iono가 누락되었습니다.', { rawIoym, rawIono, masterInfoFallback })
            throw new Error('출력할 (년월, 번호) 키 정보가 없습니다.')
        }

        const win = window.open('', '_blank', 'width=850,height=950')
        if (!win) throw new Error('브라우저 팝업 차단을 해제해 주세요.')

        try {
            const titleMap = {
                REQ_IN: { title: '입고의뢰서', proc: '/hsio/HSIO_REQIN_STR' },
                REQ_OUT: { title: '출고의뢰서', proc: '/hsio/HSIO_REQOUT_STR' },
                CERT_OUT: { title: '출고의뢰서', proc: '/hsio/HSIO_REQOUT_STR' }
            }
            const cfg = titleMap[docType] || titleMap['REQ_OUT']

            win.document.write(`<div style="font-family:sans-serif; padding:20px; text-align:center;">${cfg.title} 서식을 생성하는 중입니다...</div>`)

            const [hRes, dRes, stampRes] = await Promise.allSettled([
                api.post(cfg.proc, { actkind: 'S1', cmpycd: authStore.cmpycd, ioym, iono }),
                api.post(cfg.proc, { actkind: 'S0', cmpycd: authStore.cmpycd, ioym, iono }),
                api.post('/haba/HABA_100U_STR', { actkind: 'S0', cmpycd: authStore.cmpycd })
            ])

            const hData = (hRes.status === 'fulfilled' && hRes.value.data?.length) ? hRes.value.data[0] : masterInfoFallback
            const dtlData = (dRes.status === 'fulfilled' && dRes.value.data?.length) ? dRes.value.data : extraItems
            const sInfo = (stampRes.status === 'fulfilled' && stampRes.value.data?.[0]) ? stampRes.value.data[0] : {}

            const h = { ...masterInfoFallback, ...hData }

            let dtl: any[] = []
            if (Array.isArray(dtlData) && dtlData.length) {
                dtl = dtlData
            } else if (Array.isArray(extraItems) && extraItems.length) {
                dtl = extraItems
            } else if (extraItems && typeof extraItems === 'object' && Object.keys(extraItems).length) {
                dtl = [extraItems]
            }

            const gLines = [];
            ['gline1', 'gline2', 'gline3', 'gline4', 'gline5'].forEach(key => {
                const val = String(sInfo[key] || '').trim();
                if (val) gLines.push(val);
            });
            if (gLines.length === 0) gLines.push('담 당', '팀 장', '부 장', '사 장');

            let rowsHtml = ''
            let qtysum = 0, amtsum = 0

            for (let i = 0; i < Math.max(dtl.length, 10); i++) {
                const item = dtl[i] || {}
                if (item.itemnm) {
                    const qty = Math.abs(Number(getVal(item, 'ioqty') || getVal(item, 'balqty') || getVal(item, 'qty') || 0))
                    const amt = Math.abs(Number(getVal(item, 'jsanamt') || getVal(item, 'ioamt') || getVal(item, 'balamt') || getVal(item, 'amt') || 0))
                    const price = Number(getVal(item, 'price') || getVal(item, 'ioprice') || (qty > 0 ? Math.round(amt / qty) : 0))
                    qtysum += qty; amtsum += amt;
                    const cd = String(getVal(item, 'itemcd')).trim()
                    const bc = String(getVal(item, 'barcode') || getVal(item, 'gtin') || cd).trim()
                    rowsHtml += `
                    <tr height="36">
                        <td class="text-center" style="font-size:8.5pt;">${i + 1}</td>
                        <td class="text-center" style="font-size:8pt; font-weight:bold; padding:2px;">
                            <div>${cd}</div>
                            ${bc ? `<svg id="itemBc_${i}" style="width:110px; height:24px;"></svg>` : ''}
                        </td>
                        <td class="text-left" style="padding-left:5px; font-size:8.5pt;">${String(getVal(item, 'itemnm')).trim()}</td>
                        <td class="text-left" style="padding-left:5px; font-size:8.5pt;">${String(getVal(item, 'itsize')).trim()}</td>
                        <td class="text-center" style="font-size:8.5pt;">${getVal(item, 'unit')}</td>
                        <td class="text-right" style="padding-right:5px; font-size:8.5pt;">${fC(qty)}</td>
                        <td class="text-right" style="padding-right:5px; font-size:8.5pt;">${fC(price)}</td>
                        <td class="text-right" style="padding-right:5px; font-size:8.5pt;">${fC(amt)}</td>
                    </tr>`
                } else {
                    rowsHtml += `<tr height="32"><td>&nbsp;</td><td>&nbsp;</td><td>&nbsp;</td><td>&nbsp;</td><td>&nbsp;</td><td>&nbsp;</td><td>&nbsp;</td><td>&nbsp;</td></tr>`
                }
            }

            const ioymStr = String(h.ioym || ioym || '').trim()
            const ionoStr = String(h.iono || iono || '').trim()
            const fullBarcode = `${ioymStr}${ionoStr}`
            const dispIono = `${ioymStr}-${ionoStr}`

            let itemBarcodesJs = ''
            dtl.forEach((item: any, idx: number) => {
                const bc = String(getVal(item, 'barcode') || getVal(item, 'gtin') || getVal(item, 'itemcd')).trim()
                if (bc) {
                    itemBarcodesJs += `
                    try {
                        JsBarcode("#itemBc_${idx}", "${bc}", {
                            format: "CODE128",
                            width: 1.2,
                            height: 22,
                            displayValue: false,
                            margin: 0
                        });
                    } catch(e) {}`
                }
            })

            const html = `
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="utf-8">
                <title>${cfg.title} (${dispIono})</title>
                <script src="https://cdn.jsdelivr.net/npm/jsbarcode@3.11.5/dist/JsBarcode.all.min.js"><\/script>
                <style>
                    body { font-family: 'Malgun Gothic', '맑은 고딕', 'GulimChe', sans-serif; color: black; margin: 0; padding: 15px; }
                    table { border-collapse: collapse; font-size: 8.5pt; width: 680px; margin: 0 auto; table-layout: fixed; }
                    th, td { border: 1px solid #BDBDBD; padding: 3px; text-align: center; }
                    .bg-eee { background-color: #f2f2f2; font-weight: bold; }
                    .text-left { text-align: left !important; }
                    .text-right { text-align: right !important; }
                    .text-center { text-align: center !important; }
                    @media print {
                        body { padding: 0; }
                        @page { size: A4; margin: 10mm; }
                    }
                </style>
            </head>
            <body>
                <table border="0" style="border:0; width:680px; margin:0 auto 10px auto; border-collapse:collapse;">
                    <tr>
                        <td width="200" align="left" style="border:0; vertical-align:middle;">
                            <svg id="barcodeSvg" style="width:180px; height:42px;"></svg>
                            <div style="font-size:9pt; font-weight:bold; color:#000; margin-top:-2px;">${dispIono}</div>
                        </td>
                        <td width="260" align="center" style="font-size:22pt; font-weight:bold; vertical-align:middle; border:0; letter-spacing:8px;">
                            ${cfg.title.split('').join('&nbsp;&nbsp;')}
                        </td>
                        <td width="220" align="right" valign="top" style="border:0;">
                            <table border="1" style="width:100%; border-collapse:collapse; height:68px;">
                                <tr>
                                    <td rowspan="2" width="20" class="bg-eee" style="font-size:9pt; line-height:1.2;">결<br>재</td>
                                    ${gLines.map(g => `<td class="bg-eee" height="18" style="font-size:8pt;">${g}</td>`).join('')}
                                </tr>
                                <tr>${gLines.map(() => '<td height="50" width="45"></td>').join('')}</tr>
                            </table>
                        </td>
                    </tr>
                </table>

                <table border="1" style="width:680px; margin:0 auto; border-collapse:collapse;">
                    <colgroup><col style="width:13%"/><col style="width:37%"/><col style="width:13%"/><col style="width:37%"/></colgroup>
                    <tr height="25">
                        <td class="bg-eee">번호</td><td class="text-left" style="font-weight:bold;">&nbsp;${dispIono}</td>
                        <td class="bg-eee">회 사 명</td><td class="text-center"><b>${getVal(h, 'ccustnm') || getVal(h, 'custnm')}</b></td>
                    </tr>
                    <tr height="25">
                        <td class="bg-eee">일자</td><td class="text-left">&nbsp;${fDate(getVal(h, 'ioymd'))}</td>
                        <td class="bg-eee">등록번호</td><td class="text-center">&nbsp;${fSaup(getVal(h, 'ccustno') || getVal(h, 'saupno'))}</td>
                    </tr>
                    <tr height="25">
                        <td class="bg-eee">창고</td><td class="text-left">&nbsp;${getVal(h, 'whnm')}</td>
                        <td class="bg-eee">소 재 지</td><td class="text-center" style="font-size:8pt;">&nbsp;${getVal(h, 'caddress')}</td>
                    </tr>
                    <tr height="25">
                        <td class="bg-eee">부서</td><td class="text-left">&nbsp;${getVal(h, 'deptnm')}</td>
                        <td class="bg-eee" style="padding:0;">
                            <div style="display:flex; height:100%;">
                                <div style="flex:1; border-right:1px solid #BDBDBD; display:flex; align-items:center; justify-content:center;">전&nbsp;&nbsp;화</div>
                                <div style="flex:1; display:flex; align-items:center; justify-content:center;">팩&nbsp;&nbsp;스</div>
                            </div>
                        </td>
                        <td style="padding:0;">
                            <div style="display:flex; height:100%;">
                                <div style="flex:1; border-right:1px solid #BDBDBD; display:flex; align-items:center; justify-content:center;">${getVal(h, 'ctelno')}</div>
                                <div style="flex:1; display:flex; align-items:center; justify-content:center;">${getVal(h, 'cfaxno')}</div>
                            </div>
                        </td>
                    </tr>
                    <tr height="25">
                        <td class="bg-eee">담당자명</td><td align="left">&nbsp;${getVal(h, 'usernm') || authStore.usernm} (인)</td>
                        <td class="bg-eee">거래처담당</td><td align="center">&nbsp;${getVal(h, 'cdamdang')}</td>
                    </tr>
                    <tr height="25"><td class="bg-eee">특기사항</td><td colspan="3" align="left">&nbsp;${getVal(h, 'remark')}</td></tr>
                </table>

                <table border="1" style="width:680px; margin:6px auto 0 auto; border-collapse:collapse;">
                    <colgroup>
                        <col style="width:5%"/><col style="width:18%"/><col style="width:28%"/><col style="width:11%"/><col style="width:5%"/><col style="width:10%"/><col style="width:11%"/><col style="width:12%"/>
                    </colgroup>
                    <thead>
                        <tr class="bg-eee" height="28">
                            <td>No.</td><td>품목코드 (바코드)</td><td>품 목 명</td><td>규 격</td><td>단위</td><td>수 량</td><td>단 가</td><td>금 액</td>
                        </tr>
                    </thead>
                    <tbody>
                        ${rowsHtml}
                    </tbody>
                    <tfoot>
                        <tr height="28" style="font-weight:bold" class="bg-eee">
                            <td class="text-center" colspan="5">합 계</td>
                            <td class="text-right" style="padding-right:5px;">${fC(qtysum)}</td>
                            <td>&nbsp;</td>
                            <td class="text-right" style="padding-right:5px;">${fC(amtsum)}</td>
                        </tr>
                    </tfoot>
                </table>

                <script>
                    function generateBarcodes() {
                        try {
                            if (window.JsBarcode) {
                                JsBarcode("#barcodeSvg", "${fullBarcode}", {
                                    format: "CODE128",
                                    width: 1.8,
                                    height: 38,
                                    displayValue: false,
                                    margin: 0
                                });
                                ${itemBarcodesJs}
                            }
                        } catch (e) {
                            console.error("Barcode Generation Error:", e);
                        }
                    }
                    window.onload = function() {
                        generateBarcodes();
                        setTimeout(function() { window.print(); }, 400);
                    };
                <\/script>
            </body>
            </html>`;

            win.document.open();
            win.document.write(html);
            win.document.close();

        } catch (e: any) {
            console.error("❌ [printReportSheet Error]:", e)
            if (win && !win.closed) {
                win.document.open()
                win.document.write(`
                <div style="font-family:sans-serif; color:red; padding:30px; line-height:1.6;">
                    <h2>❌ 서식 생성 중 오류 발생</h2>
                    <p style="font-weight:bold; font-size:14px;">오류 메시지: ${e?.message || e}</p>
                    <pre style="background:#f8f8f8; padding:10px; border:1px solid #ccc; font-size:11px;">${e?.stack || ''}</pre>
                </div>`)
                win.document.close()
            }
            throw e
        }
    }

    return {
        printSpecification,
        printReportSheet
    }
}
