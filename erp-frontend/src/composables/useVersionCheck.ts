import { fetchLatestVersion } from '@/utils/version'

export async function checkVer() {
	const storedVersion = sessionStorage.getItem('appVersion')
	const latestVersion = await fetchLatestVersion()

	if (storedVersion && latestVersion && storedVersion !== latestVersion) {
		return false // 버전 불일치
	}

	return true // 버전 정상
}
