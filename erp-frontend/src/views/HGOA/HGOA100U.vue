<!--
	=============================================================
	프로그램명	  : 캠페인 옴니채널 상담 워크스페이스 (Premium Design)
    프로그램 ID	: HGOA100U
	작성일자	    : 2026.07.08
	작성자	      : AI Assistant
	Description	: 캠페인 고객 상담 및 결과 저장 (고급형 UI/UX 적용)
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="omni-workspace bg-soft-gray text-start d-flex flex-column h-100 overflow-hidden">
        <!-- 1. 고밀도 상단 헤더 -->
		<header class="omni-header shadow-sm bg-white border-bottom py-1 px-3 d-flex align-items-center justify-content-between flex-shrink-0">
			<div class="d-flex align-items-center gap-3">
				<div class="logo-badge bg-primary text-white rounded-3 px-2 py-1 me-2">
                    <i class="bi bi-headset fs-5"></i>
                </div>
                <div class="campaign-selector">
					<select v-model="selected_camp_no" class="form-select form-select-sm fw-bold border-0 bg-light-blue shadow-none" style="min-width: 280px;" @change="handle_camp_change">
						<option value="">대상 캠페인을 선택하세요</option>
						<option v-for="cp in campaigns" :key="cp.camp_no" :value="cp.camp_no">
							[{{ cp.survgbnm || '유형' }}] {{ cp.camp_nm }}
						</option>
					</select>
				</div>
                <div class="stats-group d-flex gap-2 align-items-center ms-4 overflow-auto hide-scrollbar flex-nowrap">
					<div class="stat-badge pointer" :class="{active: current_filter === ''}" @click="apply_filter('')">
						<span class="label">전체</span><span class="value">{{ stats.tot_cnt || 0 }}</span>
					</div>
					<div v-for="item in detail_stats" :key="item.rslt_cd"
						 class="stat-badge pointer" :class="{active: current_filter === item.rslt_cd}" @click="apply_filter(item.rslt_cd)">
						<span class="label">{{ item.rslt_nm }}</span><span class="value">{{ item.cnt }}</span>
					</div>
				</div>
			</div>
			<div class="agent-info-box d-flex align-items-center gap-3">
                <div v-if="ctiStore.isTalking" class="rec-indicator"><span class="dot red"></span> 녹음중</div>
				<div class="inner-badge">내선: {{ authStore.inner_no || '101' }}</div>
			</div>
		</header>

		<main class="omni-body row g-2 p-2 flex-grow-1 overflow-hidden">
            <!-- [좌측] 고객 리스트 및 이력 -->
			<aside class="col-md-3 col-lg-2 d-flex flex-column h-100 gap-2 overflow-hidden">
				<div class="card shadow-sm border-0 flex-grow-1 overflow-hidden d-flex flex-column">
					<div class="card-header bg-white py-2 px-3 border-bottom d-flex gap-1 align-items-center">
						<i class="bi bi-people-fill text-primary"></i>
                        <input type="text" v-model="search_keyword" class="form-control form-control-sm border-0 bg-light" placeholder="고객 검색..." @keyup.enter="load_customer_list">
						<i class="bi bi-search text-muted pointer" @click="load_customer_list"></i>
					</div>
					<div class="flex-grow-1 position-relative">
						<div ref="customer_table_ref" class="tabulator-omni"></div>
					</div>
				</div>
				<div class="card shadow-sm border-0 h-40 overflow-hidden d-flex flex-column bg-white">
					<div class="card-header bg-light-gray py-2 px-3 fw-bold small border-bottom">상담 타임라인</div>
					<div class="p-2 timeline-scroll overflow-auto flex-grow-1">
						<div v-for="(log, idx) in timeline_data" :key="idx" class="timeline-box">
                            <div class="time">{{ format_date(log.addtime) }}</div>
                            <div class="desc"><span class="badge bg-soft-blue text-primary">{{ log.rslt_nm }}</span> {{ log.remark || log.memo }}</div>
						</div>
                        <div v-if="timeline_data.length === 0" class="text-center py-4 text-muted small">이력이 없습니다.</div>
					</div>
				</div>
			</aside>

            <!-- [중앙] 메인 상담 영역 -->
			<section class="col-md-6 col-lg-7 d-flex flex-column h-100 gap-2 overflow-hidden">
                <!-- 선택된 고객 헤더 -->
				<div class="card shadow-sm border-0 overflow-hidden customer-active-card" v-if="selected_customer">
					<div class="card-body p-3 d-flex align-items-center justify-content-between">
                        <div class="d-flex align-items-center gap-4">
                            <div class="avatar-large bg-primary text-white">{{ selected_customer.cust_nm?.[0] }}</div>
                            <div>
                                <h4 class="fw-bold mb-0">{{ selected_customer.cust_nm }} <small class="text-muted ms-2 fs-6">{{ customer_info.tel_no }}</small></h4>
                                <div class="text-muted small mt-1">{{ customer_info.email }}</div>
                            </div>
                        </div>
                        <div class="d-flex gap-2">
                            <button class="btn btn-primary rounded-pill px-4" @click="make_call" :disabled="ctiStore.isTalking">전화하기</button>
                            <button class="btn btn-outline-info rounded-pill px-4" @click="handle_invite">초대</button>
                        </div>
					</div>
                    <div class="ext-data-strip px-3 py-2 bg-light d-flex gap-3 overflow-auto hide-scrollbar">
                        <div v-for="(val, key) in filtered_ext_data" :key="key" class="ext-item">
                            <span class="key">{{ key }}</span><span class="val">{{ val }}</span>
                        </div>
                    </div>
				</div>

				<div class="card shadow-sm border-0 flex-grow-1 overflow-hidden d-flex flex-column bg-white rounded-3">
					<!-- 선택 전 대시보드 화면 -->
                    <div v-if="!selected_customer" class="empty-dashboard d-flex flex-column align-items-center justify-content-center h-100 text-center p-5">
                        <div class="empty-icon-wrap mb-4">
                            <i class="bi bi-ui-checks-grid display-1 text-light-blue"></i>
                        </div>
                        <h4 class="fw-bold text-dark">캠페인을 선택한 다음 조회된 고객을 선택하여 상담을 시작하세요</h4>
                        <p class="text-muted">좌측 목록에서 상담 대상을 클릭하면 설문 및 채팅이 활성화됩니다.</p>
                        <div class="row g-3 mt-4 w-75">
                            <div class="col-4">
                                <div class="p-3 border rounded bg-light">
                                    <div class="text-muted extra-small">진행률</div>
                                    <div class="h5 fw-bold mb-0">{{ Math.round((stats.tot_cnt - stats.ready_cnt) / (stats.tot_cnt || 1) * 100) }}%</div>
                                </div>
                            </div>
                            <div class="col-4">
                                <div class="p-3 border rounded bg-light">
                                    <div class="text-muted extra-small">잔여 대상</div>
                                    <div class="h5 fw-bold mb-0">{{ stats.ready_cnt || 0 }}건</div>
                                </div>
                            </div>
                            <div class="col-4">
                                <div class="p-3 border rounded bg-light">
                                    <div class="text-muted extra-small">나의 실적</div>
                                    <div class="h5 fw-bold mb-0">12건</div>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- 설문 영역 -->
                    <template v-else>
                        <div class="card-header bg-white py-2 px-3 fw-bold text-primary border-bottom d-flex align-items-center">
                            <i class="bi bi-clipboard2-check me-2"></i> 상담 설문 조사
                        </div>
                        <div class="card-body p-4 overflow-auto flex-grow-1">
                            <div v-for="(q, idx) in survey_questions" :key="q.surv_no" class="survey-card mb-4 p-3 border rounded-3 shadow-sm-hover">
                                <div class="q-header d-flex gap-2 mb-3">
                                    <span class="q-no">Q{{ idx + 1 }}</span>
                                    <span class="q-text fw-bold">{{ q.question }}</span>
                                </div>
                                <div class="ans-group d-flex flex-wrap gap-2 mb-2">
                                    <div v-for="sample in q.sample_list" :key="sample.no"
                                          class="ans-chip"
                                          :class="{selected: q.user_ans_no === sample.no}"
                                          @click="q.user_ans_no = sample.no; q.user_essay = sample.text">
                                        {{ sample.text }}
                                    </div>
                                </div>
                                <input type="text" v-model="q.user_essay" class="form-control form-control-sm border-0 bg-light" placeholder="기타 답변이 있다면 입력하세요...">
                            </div>
                        </div>
                        <!-- 결과 저장 하단 바 -->
                        <div class="card-footer bg-white p-3 border-top shadow-sm">
                            <div class="row g-2">
                                <div class="col-md-3">
                                    <select v-model="call_result.rslt_cd" class="form-select border-primary-subtle fw-bold">
                                        <option value="">결과 선택</option>
                                        <option v-for="code in result_codes" :key="code.codecd" :value="code.codecd">{{ code.codenm }}</option>
                                    </select>
                                </div>
                                <div class="col-md-6">
                                    <input type="text" v-model="call_result.memo" class="form-control" placeholder="상담 중요 내용을 메모하세요 (AI 요약에 활용됩니다)">
                                </div>
                                <div class="col-md-3">
                                    <button class="btn btn-primary w-100 fw-bold py-2 shadow-sm" @click="handle_save_all" :disabled="is_saving">
                                        상담 통합 저장 완료
                                    </button>
                                </div>
                            </div>
                        </div>
                    </template>
				</div>
			</section>

			<!-- [우측] 채팅 상담 영역 -->
			<aside class="col-md-3 col-lg-3 d-flex flex-column h-100 overflow-hidden">
				<div class="card shadow-sm border-0 h-100 d-flex flex-column bg-white rounded-3">
                    <div class="card-header bg-dark text-white py-3 px-3 d-flex justify-content-between align-items-center">
                        <h6 class="fw-bold mb-0 small"><i class="bi bi-chat-dots-fill me-2 text-info"></i>실시간 옴니채널 상담</h6>
                        <button class="btn btn-xs btn-outline-info" @click="handle_clear_chat">초기화</button>
                    </div>
                    <div class="chat-log flex-grow-1 p-3 bg-light-blue-subtle overflow-auto" ref="agent_scroll_ref">
                        <div v-for="(msg, idx) in agent_chat_history" :key="idx" class="message-wrapper" :class="{mine: msg.is_me}">
                            <div class="message-bubble shadow-sm">{{ msg.text }}</div>
                        </div>
                        <div v-if="agent_chat_history.length === 0" class="chat-empty text-center text-muted py-5 mt-5">
                            <i class="bi bi-chat-square-quote display-4 opacity-25"></i>
                            <div class="small mt-2">채팅 상담 내역이 없습니다.</div>
                        </div>
                    </div>
                    <div class="chat-input-area p-3 bg-white border-top">
                        <div class="input-group">
                            <input type="text" v-model="agent_reply_input" class="form-control border-0 bg-light px-3" placeholder="메시지를 입력하세요..." @keyup.enter="send_agent_reply">
                            <button class="btn btn-primary" @click="send_agent_reply"><i class="bi bi-send-fill"></i></button>
                        </div>
                    </div>
                </div>
			</aside>
		</main>
	</div>
</template>

<script lang="ts">
export default {
    name: 'HGOA100U' // 💡 Keep-alive 캐싱을 위한 명시적 이름
}
</script>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted, nextTick } from 'vue'
import { TabulatorFull as Tabulator } from 'tabulator-tables'
import 'tabulator-tables/dist/css/tabulator_bootstrap5.min.css'
import { useAlerts } from '@/composables/useAlerts'
import { api } from '@/utils/axios'
import { useAuthStore } from '@/stores/authStore'
import { useCtiStore } from '@/stores/ctiStore'
import { fetchCrmSelectData } from '@/composables/useFetchSelectData'
import AppAlert from '@/components/AppAlert.vue'

const { showAlert, showError, vAlert, vAlertError, alertMessage } = useAlerts()
const authStore = useAuthStore(); const ctiStore = useCtiStore()

const current_filter = ref(''); const session_start_time = ref('');
const stats = reactive<any>({ tot_cnt: 0, ready_cnt: 0 }); const detail_stats = ref<any[]>([])
const campaigns = ref<any[]>([]); const selected_camp_no = ref('')
const search_keyword = ref(''); const selected_customer = ref<any>(null)
const customer_info = reactive({ email: '', tel_no: '' })
const filtered_ext_data = ref<any>({}); const result_codes = ref<any[]>([])
const survey_questions = ref<any[]>([]);
const call_result = reactive({ rslt_cd: '', memo: '' })
const agent_chat_history = ref<any[]>([]); const agent_reply_input = ref('')
const timeline_data = ref<any[]>([])
const is_saving = ref(false)
const current_rec_file = ref('') // 🚀 [추가] 현재 발신 중인 녹취파일명 보관용

let customer_table_instance: Tabulator | null = null; const customer_table_ref = ref<HTMLDivElement | null>(null)
const agent_scroll_ref = ref<HTMLElement | null>(null)

const parseExtData = (ext: any) => {
    if (!ext) return {};
    try {
        let p = ext;
        if (typeof p === 'string') { p = JSON.parse(p); if (typeof p === 'string') p = JSON.parse(p); }
        return typeof p === 'object' ? p : {};
    } catch (e) { return {}; }
}

const fetch_messages = async () => {
    if (!customer_info.email) return;
    try {
        const res = await api.get('/common/chat/messages', { params: { email: customer_info.email } });
        const list = res.data?.data || [];
        const reversedList = [...list].reverse();
        const newHistory = reversedList.filter((m: any) => String(m.message_type).split('.')[0] !== '2').map((m: any) => ({
            text: m.content || '',
            is_me: String(m.message_type).split('.')[0] === '1' || m.message_type === 'outgoing'
        }));
        if (newHistory.length > 0 && JSON.stringify(newHistory) !== JSON.stringify(agent_chat_history.value)) {
            agent_chat_history.value = newHistory;
            nextTick(scrollChatToBottom);
        }
    } catch (e) {}
}

const send_agent_reply = async () => {
    const content = agent_reply_input.value.trim();
    if (!content || !customer_info.email) return;
    try {
        await api.post('/common/chat/reply', { email: customer_info.email, content: content });
        agent_reply_input.value = '';
        await fetch_messages();
    } catch (e) { vAlertError('전송 실패'); }
}

const handle_clear_chat = async () => {
    if (!customer_info.email) return vAlertError('대상 고객 없음');
    if (!confirm('대화 이력을 삭제하고 상담창을 초기화하시겠습니까?')) return;
    try {
        await api.post('/common/chat/clear', { email: customer_info.email });
        agent_chat_history.value = [];
        vAlert('초기화되었습니다.');
    } catch (e) { vAlertError('실패'); }
}

const scrollChatToBottom = () => { if (agent_scroll_ref.value) agent_scroll_ref.value.scrollTop = agent_scroll_ref.value.scrollHeight; }

const handle_camp_change = async () => {
    if (!selected_camp_no.value) {
        reset_consult_state();
        customer_table_instance?.clearData();
        return;
    }
    reset_consult_state();
    try {
        const res = await api.get('/crm/outbound/camp-stats', { params: { camp_no: selected_camp_no.value } });
        Object.assign(stats, res.data); detail_stats.value = res.data.details || [];
        const camp = campaigns.value.find(c => c.camp_no === selected_camp_no.value);
        if (camp) {
            const sres = await api.get('/crm/outbound/surv-form', { params: { surv_gb: camp.surv_gb } });
            const list = Array.isArray(sres.data) ? sres.data : [];
            survey_questions.value = list.map((q: any) => {
                const samples = q.answers ? q.answers.split(/ \/ |, /).map((s: string) => {
                    const p = s.split(':');
                    return {
                        no: p[0]?.trim(),
                        text: (p[1] || p[0])?.trim(),
                        point: p[2]?.trim() || '0'
                    };
                }) : [];
                return { surv_no: q.surv_no, question: q.question, user_essay: '', user_ans_no: '', sample_list: samples };
            });
        }
        load_customer_list();
    } catch (e) { vAlertError('로드 실패'); }
}

const reset_consult_state = () => {
    selected_customer.value = null;
    customer_info.email = '';
    customer_info.tel_no = '';
    filtered_ext_data.value = {};
    survey_questions.value.forEach(q => { q.user_ans_no = ''; q.user_essay = ''; });
    call_result.rslt_cd = '';
    call_result.memo = '';
    agent_chat_history.value = [];
    timeline_data.value = [];
}

const load_customer_list = async () => {
    if (!selected_camp_no.value) return;
    try {
        const { data } = await api.get('/crm/outbound/call-list', { params: { camp_no: selected_camp_no.value, keyword: search_keyword.value, filter: current_filter.value } });
        customer_table_instance?.setData(data);
    } catch (e) {}
}

const handle_save_all = async () => {
    if (!selected_customer.value) return vAlertError('대상 선택 필요');
    if (!call_result.rslt_cd) return vAlertError('결과 선택 필요');
    is_saving.value = true;
    try {
        const chat_log_str = agent_chat_history.value.map(m => `[${m.is_me?'상담원':'고객'}] ${m.text}`).join('\n');
        await api.post('/crm/outbound/save-consolidated', {
            cmpycd: authStore.cmpycd || '',
            camp_no: selected_camp_no.value,
            call_seq: selected_customer.value.call_seq,
            cust_email: customer_info.email,
            cust_nm: selected_customer.value.cust_nm,
            call_telno: customer_info.tel_no,
            rslt_cd: call_result.rslt_cd,
            memo: call_result.memo,
            userid: authStore.userid,
            line_num: authStore.inner_no,
            start_time: session_start_time.value,
            end_time: new Date().toISOString().replace('T', ' ').substring(0, 19),
            media_type: agent_chat_history.value.length > 0 ? 'chat' : 'call',
            chat_history: chat_log_str,
            rec_file: current_rec_file.value || '', // 🚀 [해결] 백엔드 필드명(rec_file)에 맞춰 전달
            surveys: survey_questions.value.map(q => {
                const selectedSample = q.sample_list.find(s => s.no === q.user_ans_no);
                return {
                    surv_no: q.surv_no,
                    ans_no: q.user_ans_no || '001',
                    remark: q.user_essay,
                    point: selectedSample ? selectedSample.point : '0'
                };
            })
        });
        vAlert('정상적으로 저장되었습니다.');
        handle_camp_change();
    } catch (e) { vAlertError('저장 실패'); } finally { is_saving.value = false; }
}

const init_customer_table = () => {
    if (customer_table_instance) customer_table_instance.destroy();
    customer_table_instance = new Tabulator(customer_table_ref.value!, {
        layout: "fitColumns", height: "100%", selectable: 1, placeholder: "No Data",
        columns: [
            { title: "고객명", field: "cust_nm", hozAlign: "left", formatter: (cell:any) => {
                const d = cell.getRow().getData();
                return `<div class="py-1"><div class="fw-bold small text-dark">${d.cust_nm}</div><div class="text-muted" style="font-size:0.65rem">${d.tel_no || ''}</div></div>`;
            }},
            { title: "결과", field: "rslt_nm", hozAlign: "center", width: 70, formatter: (c:any) => `<span class="badge ${c.getValue()==='완료'?'bg-primary':'bg-light text-dark border'}">${c.getValue() || '대기'}</span>` }
        ]
    });
    customer_table_instance.on("rowClick", (e, row) => {
        const d = row.getData();
        selected_customer.value = d;
        customer_info.tel_no = d.tel_no; customer_info.email = d.email;
        filtered_ext_data.value = parseExtData(d.ext_data);
        session_start_time.value = new Date().toISOString().replace('T', ' ').substring(0, 19);
        fetch_messages();
    });
}

const apply_filter = (code: string) => { current_filter.value = code; load_customer_list(); }
const format_date = (dt: any) => dt ? new Date(dt).toLocaleString() : '';
const make_call = async () => {
    if (selected_customer.value && customer_info.tel_no) {
        try {
            const res = await api.get('/crm/cti/make-call', {
                params: {
                    exten: authStore.inner_no,
                    dest: customer_info.tel_no.replace(/-/g, ''),
                    context: 'outbound-call'
                }
            });
            if (res.data && res.data.recFile) {
                // 🚀 [해결] 발신 성공 시 파일명을 화면 메모리에 저장
                current_rec_file.value = res.data.recFile;
                console.log('🎬 [OUTBOUND] 녹취 시작:', current_rec_file.value);
            }
        } catch (e) {
            vAlertError('발신 요청 실패');
        }
    }
}
const handle_invite = async () => { if (customer_info.email) { await api.post('/mail/send-invite', { toEmail: customer_info.email, custNm: selected_customer.value.cust_nm, custcd: selected_customer.value.call_seq.toString() }); vAlert('초대장 발송 완료'); } }

let messageInterval: any = null;

onMounted(() => {
    init_customer_table();
    api.get('/crm/outbound/camp-list').then(res => campaigns.value = res.data);
    fetchCrmSelectData('920').then(res => result_codes.value = res);
    messageInterval = setInterval(fetch_messages, 3000);
});

onUnmounted(() => {
    if (messageInterval) clearInterval(messageInterval);
    if (customer_table_instance) { customer_table_instance.destroy(); customer_table_instance = null; }
});
</script>

<style scoped>
/* 전역 스타일 */
.omni-workspace { height: 100vh; font-family: 'Pretendard', sans-serif; }
.bg-soft-gray { background-color: #f4f7fa; }
.bg-light-blue { background-color: #eef2f7; }
.bg-light-blue-subtle { background-color: #f0f4f9; }

/* 헤더 & 통계 */
.omni-header { height: 50px; }
.logo-badge { background: linear-gradient(135deg, #005a9f, #0078d4); }
.stat-badge { padding: 3px 12px; border-radius: 20px; border: 1px solid #ddd; background: #fff; font-size: 0.7rem; cursor: pointer; transition: 0.2s; }
.stat-badge .label { color: #666; margin-right: 5px; }
.stat-badge .value { font-weight: 800; color: #333; }
.stat-badge.active { background: #005a9f; color: #fff; border-color: #005a9f; }
.stat-badge.active .label, .stat-badge.active .value { color: #fff; }
.inner-badge { background: #f8f9fa; border: 1px solid #dee2e6; border-radius: 4px; padding: 2px 10px; font-weight: bold; font-size: 0.7rem; }

/* 타임라인 */
.timeline-box { padding: 6px; border-left: 2px solid #ddd; margin-bottom: 4px; background: #fafafa; }
.timeline-box .time { font-size: 0.6rem; color: #999; }
.timeline-box .desc { font-size: 0.7rem; color: #444; margin-top: 1px; }

/* 고객 활성 카드 */
.customer-active-card { background: linear-gradient(to right, #ffffff, #f0f7ff); border-left: 5px solid #005a9f !important; }
.avatar-large { width: 40px; height: 40px; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 1.2rem; font-weight: 800; }
.customer-active-card h4 { font-size: 1.1rem; }
.ext-item { white-space: nowrap; font-size: 0.7rem; }
.ext-item .key { color: #666; font-weight: bold; margin-right: 4px; }
.ext-item .val { color: #005a9f; font-weight: 600; }

/* 비어있는 상태 대시보드 */
.empty-dashboard { background-color: #fff; border-radius: 12px; }
.empty-dashboard h4 { font-size: 1.1rem; }
.empty-dashboard p { font-size: 0.8rem; }
.text-light-blue { color: #d1e3f8; }

/* 설문 카드 */
.survey-card { background: #fff; transition: 0.2s; padding: 12px !important; margin-bottom: 12px !important; }
.survey-card:hover { border-color: #005a9f !important; }
.q-no { background: #005a9f; color: #fff; padding: 1px 6px; border-radius: 3px; font-size: 0.65rem; }
.q-text { font-size: 0.85rem; }
.ans-chip { padding: 4px 12px; border-radius: 15px; border: 1px solid #eee; background: #f8f9fa; cursor: pointer; font-size: 0.75rem; transition: 0.2s; }
.ans-chip:hover { background: #eef2f7; }
.ans-chip.selected { background: #005a9f; color: #fff; border-color: #005a9f; font-weight: bold; }
.survey-card .form-control-sm { font-size: 0.75rem; height: 28px; }

/* 하단 바 */
.card-footer .form-select, .card-footer .form-control { font-size: 0.8rem; height: 32px; }
.card-footer .btn { font-size: 0.85rem; padding: 6px !important; }

/* 채팅 UI */
.message-bubble { max-width: 85%; padding: 6px 12px; border-radius: 12px; font-size: 0.8rem; }
.chat-header h6 { font-size: 0.8rem; }
.chat-input-area .form-control { font-size: 0.8rem; }
.chat-empty .small { font-size: 0.75rem; }

/* 애니메이션 */
.animate-pulse { animation: pulse 2s infinite; }
@keyframes pulse { 0% { opacity: 1; } 50% { opacity: 0.4; } 100% { opacity: 1; } }
.rec-indicator { color: #d9534f; font-weight: bold; font-size: 0.75rem; display: flex; align-items: center; gap: 5px; }
.dot { width: 8px; height: 8px; border-radius: 50%; display: inline-block; }
.dot.red { background: #d9534f; animation: pulse 1s infinite; }

/* 스크롤바 */
.hide-scrollbar::-webkit-scrollbar { display: none; }
</style>
