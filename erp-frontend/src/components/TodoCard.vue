<template>
	<div class="p-3 bg-body rounded shadow-sm">
		<h5 class="border-bottom pb-1 mb-0">{{ props.todo.title }}</h5>
		<div class="d-flex text-muted pt-3">
			<svg
				class="bd-placeholder-img flex-shrink-0 me-2 rounded"
				width="32"
				height="32"
				xmlns="http://www.w3.org/2000/svg"
				role="img"
				aria-label="Placeholder: 32x32"
				preserveAspectRatio="xMidymid slice"
				focusable="false"
			>
				<title>Placeholder</title>
				<circle
					cx="16"
					cy="16"
					r="6"
					:fill="
						typeof props.todo.subData1 === 'string'
							? '#0d6efd' // 문자열 → 파랑
							: props.todo.subData1 === 0
								? '#2bcf31' // 0 → 초록
								: '#ff3347' // 그 외 → 빨강
					"
				>
					<animate
						attributeName="opacity"
						values="0.1;0.5;1;0"
						dur="1.5s"
						repeatCount="indefinite"
					/>
					<animate attributeName="r" values="5;8" dur="1.5s" repeatCount="indefinite" />
				</circle>
			</svg>

			<div class="pb-3 mb-0 small lh-sm border-bottom w-100">
				<div class="d-flex justify-content-between">
					<strong class="text-gray-dark">{{ props.todo.subTitle1 }}</strong>
					<router-link
						:to="`/${props.todo.link1}`"
						:title="props.todo.link1"
						@click.prevent="goPage(props.todo.link1, props.todo.linkName1)"
					>
						{{ props.todo.button }}
					</router-link>
				</div>
				<span class="d-block">{{ props.todo.subData1 }}{{ props.todo.checkType }}</span>
			</div>
		</div>
		<div class="d-flex text-muted pt-3">
			<svg
				class="bd-placeholder-img flex-shrink-0 me-2 rounded"
				width="32"
				height="32"
				xmlns="http://www.w3.org/2000/svg"
				role="img"
				aria-label="Placeholder: 32x32"
				preserveAspectRatio="xMidymid slice"
				focusable="false"
			>
				<title>Placeholder</title>
				<circle
					cx="16"
					cy="16"
					r="6"
					:fill="
						typeof props.todo.subData2 === 'string'
							? '#0d6efd' // 문자열 → 파랑
							: props.todo.subData2 === 0
								? '#2bcf31' // 0 → 초록
								: '#ff3347' // 그 외 → 빨강
					"
				>
					<animate
						attributeName="opacity"
						values="0.1;0.5;1;0"
						dur="1.5s"
						repeatCount="indefinite"
					/>
					<animate attributeName="r" values="5;8" dur="1.5s" repeatCount="indefinite" />
				</circle>
			</svg>

			<div class="pb-3 mb-0 small lh-sm border-bottom w-100">
				<div class="d-flex justify-content-between">
					<strong class="text-gray-dark">{{ props.todo.subTitle2 }}</strong>
					<router-link
						:to="`/${props.todo.link2}`"
						:title="props.todo.link2"
						@click.prevent="goPage(props.todo.link2, props.todo.linkName2)"
					>
						{{ props.todo.button }}
					</router-link>
				</div>
				<span class="d-block">{{ props.todo.subData2 }}{{ props.todo.checkType }}</span>
			</div>
		</div>
		<div class="d-flex text-muted pt-3">
			<svg
				class="bd-placeholder-img flex-shrink-0 me-2 rounded"
				width="32"
				height="32"
				xmlns="http://www.w3.org/2000/svg"
				role="img"
				aria-label="Placeholder: 32x32"
				preserveAspectRatio="xMidymid slice"
				focusable="false"
			>
				<title>Placeholder</title>
				<circle
					cx="16"
					cy="16"
					r="6"
					:fill="
						typeof props.todo.subData3 === 'string'
							? '#0d6efd' // 문자열 → 파랑
							: props.todo.subData3 === 0
								? '#2bcf31' // 0 → 초록
								: '#ff3347' // 그 외 → 빨강
					"
				>
					<animate
						attributeName="opacity"
						values="0.1;0.5;1;0"
						dur="1.5s"
						repeatCount="indefinite"
					/>
					<animate attributeName="r" values="5;8" dur="1.5s" repeatCount="indefinite" />
				</circle>
			</svg>
			<div class="pb-3 mb-0 small lh-sm border-bottom w-100">
				<div class="d-flex justify-content-between">
					<strong class="text-gray-dark">{{ props.todo.subTitle3 }}</strong>
					<router-link
						:to="`/${props.todo.link3}`"
						:title="props.todo.link3"
						@click.prevent="goPage(props.todo.link3, props.todo.linkName3)"
					>
						{{ props.todo.button }}
					</router-link>
				</div>
				<span class="d-block">{{ props.todo.subData3 }}{{ props.todo.checkType }}</span>
			</div>
		</div>
		<div class="d-flex text-muted pt-3">
			<svg
				class="bd-placeholder-img flex-shrink-0 me-2 rounded"
				width="32"
				height="32"
				xmlns="http://www.w3.org/2000/svg"
				role="img"
				aria-label="Placeholder: 32x32"
				preserveAspectRatio="xMidymid slice"
				focusable="false"
			>
				<title>Placeholder</title>
				<circle
					cx="16"
					cy="16"
					r="6"
					:fill="
						typeof props.todo.subData4 === 'string'
							? '#0d6efd' // 문자열 → 파랑
							: props.todo.subData4 === 0
								? '#2bcf31' // 0 → 초록
								: '#ff3347' // 그 외 → 빨강
					"
				>
					<animate
						attributeName="opacity"
						values="0.1;0.5;1;0"
						dur="1.5s"
						repeatCount="indefinite"
					/>
					<animate attributeName="r" values="5;8" dur="1.5s" repeatCount="indefinite" />
				</circle>
			</svg>
			<div class="pb-3 mb-0 small lh-sm border-bottom w-100">
				<div class="d-flex justify-content-between">
					<strong class="text-gray-dark">{{ props.todo.subTitle4 }}</strong>
					<router-link
						:to="`/${props.todo.link4}`"
						:title="props.todo.link4"
						@click.prevent="goPage(props.todo.link4, props.todo.linkName4)"
					>
						{{ props.todo.button }}
					</router-link>
				</div>
				<span class="d-block">{{ props.todo.subData4 }}{{ props.todo.checkType }}</span>
			</div>
		</div>
		<div class="d-flex text-muted pt-3">
			<svg
				class="bd-placeholder-img flex-shrink-0 me-2 rounded"
				width="32"
				height="32"
				xmlns="http://www.w3.org/2000/svg"
				role="img"
				aria-label="Placeholder: 32x32"
				preserveAspectRatio="xMidymid slice"
				focusable="false"
			>
				<title>Placeholder</title>
				<circle
					cx="16"
					cy="16"
					r="6"
					:fill="
						typeof props.todo.subData4 === 'string'
							? '#0d6efd' // 문자열 → 파랑
							: props.todo.subData4 === 0
								? '#2bcf31' // 0 → 초록
								: '#ff3347' // 그 외 → 빨강
					"
				>
					<animate
						attributeName="opacity"
						values="0.1;0.5;1;0"
						dur="1.5s"
						repeatCount="indefinite"
					/>
					<animate attributeName="r" values="5;8" dur="1.5s" repeatCount="indefinite" />
				</circle>
			</svg>
			<div class="pb-3 mb-0 small lh-sm border-bottom w-100">
				<div class="d-flex justify-content-between">
					<strong class="text-gray-dark">{{ props.todo.subTitle5 }}</strong>
					<router-link
						:to="`/${props.todo.link5}`"
						:title="props.todo.link5"
						@click.prevent="goPage(props.todo.link5, props.todo.linkName5)"
					>
						{{ props.todo.button }}
					</router-link>
				</div>
				<span class="d-block">{{ props.todo.subData5 }}{{ props.todo.checkType }}</span>
			</div>
		</div>
	</div>
</template>
<script setup lang="ts">
import router from '@/router'
import { addDynamicRoute } from '@/router/dynamicRoute.ts'

interface TodoCardData {
	title: string
	subTitle1: string
	subTitle2: string
	subTitle3: string
	subTitle4: string
	subTitle5: string
	subData1: number | string | null
	subData2: number | string | null
	subData3: number | string | null
	subData4: number | string | null
	subData5: number | string | null
	link1: string
	link2: string
	link3: string
	link4: string
	link5: string
	linkName1: string
	linkName2: string
	linkName3: string
	linkName4: string
	linkName5: string
	checkType: string
	button: string | null
}

/** ✅ defineProps는 최상위에서! */
const props = defineProps<{
	todo: TodoCardData
}>()

function goPage(pgmid: string, pgmnm: string) {
	addDynamicRoute(pgmid, pgmnm)
	router.push({ path: `/${pgmid}` })
}
</script>
