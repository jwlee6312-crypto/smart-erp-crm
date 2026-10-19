<!--
	=============================================================
	프로그램명	  : SmartCore 통합 오퍼레이션 콕핏 (최종 레이아웃)
    프로그램 ID	: DEFAULT_PAGE
	작성일자	    : 2026.09.13
	작성자	      : AI Assistant
	설명         : 3대 성과지표 + 통합 리스트(좌) + 전사 캘린더(우) 배치
	=============================================================
-->

<template>
    <div class="cockpit-wrapper bg-soft-gray h-100 overflow-auto p-3 text-start">
        <!-- [1] 상단 통합 관제 헤더 -->
        <header class="d-flex justify-content-between align-items-center mb-2 border-bottom pb-2">
            <div class="d-flex align-items-center gap-3">
                <h6 class="fw-bold text-dark mb-0">
                    <i class="bi bi-grid-1x2-fill text-primary me-2"></i>OPERATION COCKPIT
                </h6>
                <div class="vr opacity-25" style="height: 15px;"></div>
                <p class="text-muted extra-small mb-0">실시간 전사 운영 통합 대시보드</p>
            </div>
            <div class="digital-clock d-flex align-items-center gap-2 px-3 py-1 bg-white rounded-3 shadow-xs border">
                <i class="bi bi-clock-fill text-primary small"></i>
                <span class="fw-bold text-primary font-monospace" style="font-size: 1rem;">{{ currentTime }}</span>
            </div>
        </header>

        <!-- [2] 리얼타임 피드 -->
        <div class="live-feed-ribbon mb-3 p-2 bg-dark text-white rounded-3 border shadow-sm overflow-hidden">
            <div class="d-flex align-items-center gap-3">
                <span class="badge bg-danger rounded-pill px-2 py-1 fw-bold extra-small pulse-border">LIVE FEED</span>
                <div class="flex-grow-1 extra-small fw-bold overflow-hidden" style="position: relative; height: 20px;">
                    <div class="scrolling-text">
                        <span v-for="(item, idx) in liveFeeds" :key="idx" class="me-5">
                            [{{ item.media_type || '알림' }}] {{ item.content }} ({{ item.time_str }})
                        </span>
                        <span v-if="liveFeeds.length === 0">실시간 시스템 모니터링 가동 중... 전사 지표가 정상 범위 내에서 운영되고 있습니다.</span>
                    </div>
                </div>
            </div>
        </div>

        <!-- [3] 3대 핵심 목표 이행 현황 (YTD) -->
        <div class="row g-2 mb-3">
            <div class="col-md-4" v-for="goal in performanceGoals" :key="goal.label">
                <div class="card border-0 shadow-sm rounded-3 bg-white p-3 border-bottom border-3" :class="'border-' + goal.color">
                    <div class="d-flex justify-content-between align-items-end mb-2">
                        <div>
                            <div class="text-muted fw-bold mb-1" style="font-size: 11px;">{{ goal.label }}</div>
                            <div class="fw-bolder text-dark h5 mb-0">{{ goal.rate }}%</div>
                        </div>
                        <div class="text-end">
                            <div class="extra-small text-muted mb-1">실적/계획 (억)</div>
                            <div class="extra-small fw-bold text-dark">{{ goal.actual }} / {{ goal.plan }}</div>
                        </div>
                    </div>
                    <div class="progress bg-light" style="height: 6px;">
                        <div class="progress-bar progress-bar-striped progress-bar-animated"
                             :class="'bg-' + goal.color" role="progressbar" :style="{ width: goal.rate + '%' }"></div>
                    </div>
                </div>
            </div>
        </div>

        <!-- [4] 메인 컨텐츠 영역 (좌: 리스트 / 우: 캘린더) -->
        <div class="row g-3">
            <!-- ⬅️ 좌측: 주요 통합 리스트 -->
            <div class="col-md-7 d-flex flex-column gap-3">

                <!-- (A) 통합 미결 현황 (5대 핵심 지표) -->
                <div class="card border-0 shadow-sm rounded-4 bg-white overflow-hidden">
                    <div class="card-header bg-white py-2 px-3 border-bottom d-flex justify-content-between align-items-center">
                        <span class="fw-bold text-dark small"><i class="bi bi-exclamation-octagon-fill me-2 text-danger"></i>전사 통합 미결 현황</span>
                    </div>
                    <div class="card-body p-0">
                        <div class="row g-0 text-center border-bottom">
                            <div class="col py-3 border-end">
                                <div class="extra-small text-muted mb-1">발주미입고</div>
                                <div class="fw-bold text-warning h6 mb-0">{{ statsMap.purchase_pending || 0 }}<span class="mini-text ms-1">건</span></div>
                            </div>
                            <div class="col py-3 border-end">
                                <div class="extra-small text-muted mb-1">매입미정산</div>
                                <div class="fw-bold text-dark h6 mb-0">3<span class="mini-text ms-1">건</span></div>
                            </div>
                            <div class="col py-3 border-end">
                                <div class="extra-small text-muted mb-1">주문미출고</div>
                                <div class="fw-bold text-success h6 mb-0">{{ statsMap.sales_pending || 0 }}<span class="mini-text ms-1">건</span></div>
                            </div>
                            <div class="col py-3 border-end">
                                <div class="extra-small text-muted mb-1">매출미정산</div>
                                <div class="fw-bold text-primary h6 mb-0">8<span class="mini-text ms-1">건</span></div>
                            </div>
                            <div class="col py-3">
                                <div class="extra-small text-muted mb-1">생산미완료</div>
                                <div class="fw-bold text-danger h6 mb-0">{{ statsMap.prod_pending || 0 }}<span class="mini-text ms-1">건</span></div>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- (B) 주요 일정 및 만기 알람 -->
                <div class="card border-0 shadow-sm rounded-4 bg-white overflow-hidden">
                    <div class="card-header bg-white py-2 px-3 border-bottom d-flex justify-content-between align-items-center">
                        <span class="fw-bold text-dark small"><i class="bi bi-clock-history me-2 text-primary"></i>주요 일정 및 만기 알람</span>
                    </div>
                    <div class="card-body p-0 overflow-auto" style="max-height: 220px;">
                        <div class="table-responsive">
                            <table class="table table-hover table-sm extra-small mb-0">
                                <thead class="bg-light sticky-top">
                                    <tr><th class="ps-3">카테고리</th><th>내용</th><th class="text-center">기한</th><th class="text-end pe-3">비고</th></tr>
                                </thead>
                                <tbody>
                                    <tr v-for="(item, idx) in expireList" :key="idx" class="border-light">
                                        <td class="ps-3"><span class="badge bg-soft-blue text-primary">{{ item.category || '만기' }}</span></td>
                                        <td class="fw-bold text-dark">{{ item.title || item.cust }}</td>
                                        <td class="text-center text-danger fw-bold">{{ item.date }}</td>
                                        <td class="text-end pe-3 text-muted">{{ item.amt || '-' }}</td>
                                    </tr>
                                    <tr v-if="expireList.length === 0">
                                        <td colspan="4" class="text-center py-4 text-muted">등록된 일정이 없습니다.</td>
                                    </tr>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>

                <!-- (C) 전사 주요 공지사항 -->
                <div class="card border-0 shadow-sm rounded-4 bg-white overflow-hidden">
                    <div class="card-header bg-dark py-2 px-3 d-flex justify-content-between align-items-center">
                        <span class="fw-bold text-white small"><i class="bi bi-megaphone-fill me-2"></i>전사 주요 공지사항</span>
                    </div>
                    <div class="card-body p-0 overflow-auto" style="max-height: 250px;">
                        <div class="list-group list-group-flush">
                            <div v-for="notice in noticeList" :key="notice.id" class="list-group-item p-3 border-light">
                                <div class="d-flex justify-content-between align-items-start mb-1">
                                    <span class="fw-bold text-dark small text-truncate" style="max-width: 80%;">{{ notice.title }}</span>
                                    <span class="badge bg-light text-muted border-0">{{ notice.date }}</span>
                                </div>
                                <p class="text-muted mb-0 extra-small text-truncate-2">{{ notice.content || '공지사항 본문 내용이 여기에 표시됩니다.' }}</p>
                            </div>
                            <div v-if="noticeList.length === 0" class="p-5 text-center text-muted extra-small">최신 공지가 없습니다.</div>
                        </div>
                    </div>
                </div>
            </div>

            <!-- ➡️ 우측: 전사 운영 캘린더 -->
            <div class="col-md-5">
                <div class="card border-0 shadow-sm rounded-4 bg-white h-100 overflow-hidden">
                    <div class="card-header bg-white py-2 px-3 border-bottom d-flex justify-content-between align-items-center">
                        <span class="fw-bold text-dark small"><i class="bi bi-calendar3 me-2 text-primary"></i>전사 운영 캘린더 ({{ currentMonth }}월)</span>
                        <div class="btn-group btn-group-xs">
                            <button class="btn btn-outline-secondary px-2"><i class="bi bi-chevron-left"></i></button>
                            <button class="btn btn-outline-secondary px-2"><i class="bi bi-chevron-right"></i></button>
                        </div>
                    </div>
                    <div class="card-body p-3">
                        <!-- 가상 캘린더 레이아웃 -->
                        <div class="calendar-grid">
                            <div class="calendar-day-head" v-for="d in ['일','월','화','수','목','금','토']" :key="d">{{ d }}</div>
                            <div class="calendar-day empty" v-for="n in 2" :key="'e'+n"></div>
                            <div class="calendar-day" v-for="day in 30" :key="day" :class="{'today': day === todayDate}">
                                <span class="day-num">{{ day }}</span>
                                <div v-if="day === 15" class="event-dot bg-danger" title="점검"></div>
                                <div v-if="day === 22" class="event-dot bg-primary" title="마감"></div>
                                <div v-if="day === todayDate" class="event-tag">Today</div>
                            </div>
                        </div>
                        <!-- 일정 요약 -->
                        <div class="mt-4 p-3 rounded bg-light">
                            <div class="fw-bold extra-small text-dark mb-2 border-bottom pb-1">오늘의 주요 일정</div>
                            <ul class="list-unstyled mb-0 extra-small text-secondary lh-lg">
                                <li><i class="bi bi-dot text-primary"></i> 오전 10:00 주간 운영 회의</li>
                                <li><i class="bi bi-dot text-danger"></i> 오후 02:00 신규 협력사 미팅</li>
                                <li><i class="bi bi-dot text-success"></i> 전사 보안 교육 이수 마감</li>
                            </ul>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { api } from '@/utils/axios'

// [1] 시계 및 라이브 데이터
const currentTime = ref('')
const currentMonth = computed(() => new Date().getMonth() + 1)
const todayDate = computed(() => new Date().getDate())

const liveFeeds = ref<any[]>([])
const noticeList = ref<any[]>([])
const expireList = ref<any[]>([])
const statsMap = ref<any>({})

const updateClock = () => {
    currentTime.value = new Date().toLocaleTimeString('ko-KR', { hour12: false });
}

// [2] 3대 성과지표 (YTD 가상 데이터)
const performanceGoals = ref([
    { label: '전사 매출 목표 이행률', plan: 120, actual: 98, rate: 81.6, color: 'primary' },
    { label: '영업활동 수주 달성률', plan: 150, actual: 115, rate: 76.6, color: 'success' },
    { label: '생산활동 계획 대비 실적', plan: 100, actual: 89, rate: 89.0, color: 'info' }
])

// [3] 데이터 연동 로직
const fetchDashboardData = async () => {
    try {
        const { data } = await api.get('/briefing-dashboard/data');
        liveFeeds.value = data.feed || [];
        noticeList.value = data.notices || [];
        expireList.value = data.expiry || [];
        if (data.stats) statsMap.value = data.stats;
    } catch (e) {
        console.error('콕핏 데이터 로드 실패', e);
    }
}

onMounted(() => {
    updateClock();
    setInterval(updateClock, 1000);
    fetchDashboardData();
    setInterval(fetchDashboardData, 300000);
})
</script>

<style scoped>
.cockpit-wrapper { height: calc(100vh - 65px); font-family: 'Pretendard', sans-serif; letter-spacing: -0.01rem; }
.bg-soft-gray { background-color: #f1f5f9; }
.bg-soft-blue { background-color: #eef2ff; }
.extra-small { font-size: 0.72rem; }
.mini-text { font-size: 0.65rem; color: #94a3b8; }

/* 🚀 캘린더 스타일 */
.calendar-grid { display: grid; grid-template-columns: repeat(7, 1fr); gap: 1px; background-color: #eee; border: 1px solid #eee; }
.calendar-day-head { background-color: #f8fafc; padding: 5px; text-align: center; font-size: 10px; font-weight: 700; color: #64748b; }
.calendar-day { background-color: #fff; height: 55px; padding: 5px; position: relative; cursor: pointer; }
.calendar-day:hover { background-color: #f1f5f9; }
.calendar-day.today { background-color: #eef2ff; }
.day-num { font-size: 11px; font-weight: 600; color: #475569; }
.event-dot { width: 5px; height: 5px; border-radius: 50%; position: absolute; bottom: 8px; left: 50%; transform: translateX(-50%); }
.event-tag { position: absolute; top: 5px; right: 5px; font-size: 8px; background: #3b82f6; color: #fff; padding: 1px 4px; border-radius: 3px; }

/* 공통 리스트 효과 */
.list-group-item { transition: background 0.2s; border-color: #f1f5f9 !important; }
.list-group-item:hover { background-color: #f8fafc; }
.text-truncate-2 { display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }

/* 애니메이션 */
.scrolling-text { position: absolute; width: auto; white-space: nowrap; animation: scroll-left 45s linear infinite; }
@keyframes scroll-left { 0% { transform: translateX(100%); } 100% { transform: translateX(-100%); } }
.pulse-border { animation: pulse-soft 2.5s infinite; }
@keyframes pulse-soft {
    0% { box-shadow: 0 0 0 0 rgba(220, 53, 69, 0.4); }
    70% { box-shadow: 0 0 0 8px rgba(220, 53, 69, 0); }
    100% { box-shadow: 0 0 0 0 rgba(220, 53, 69, 0); }
}

.btn-group-xs > .btn { padding: 1px 5px; font-size: 10px; }
.shadow-xs { box-shadow: 0 1px 2px rgba(0,0,0,0.05); }
</style>_