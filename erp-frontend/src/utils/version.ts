// 최신 버전 확인 함수
export async function fetchLatestVersion(): Promise<string | null> {
	try {
		const res = await fetch('/version.json', { cache: 'no-store' })
		const data = await res.json()
		return data.version
	} catch {
		return null
	}
}