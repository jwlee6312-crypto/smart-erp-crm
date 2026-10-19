import { useSearchMenu } from '@/composables/useSearchMenu'

const { selectMenu } = useSearchMenu()
export async function activation(topMenuCode: string, sideMenuCode: string, title: string) {
	await selectMenu(topMenuCode)
	document.getElementById(topMenuCode)?.setAttribute('class', 'nav-link active') // 상단메뉴
	document.getElementById(`title${sideMenuCode}`)?.setAttribute('aria-expanded', 'true') // 대제목
	document.getElementById(`open${sideMenuCode}`)?.setAttribute('class', 'collapse show') // 펼침
	document
		.getElementById(title)
		?.setAttribute('class', 'router-link-active router-link-exact-active sb-nav-link') // 소제목
}