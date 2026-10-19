import { globalIgnores } from 'eslint/config'
import { defineConfigWithVueTs, vueTsConfigs } from '@vue/eslint-config-typescript'
import pluginVue from 'eslint-plugin-vue'
import skipFormatting from '@vue/eslint-config-prettier/skip-formatting'
import prettier from 'eslint-plugin-prettier'

export default defineConfigWithVueTs(
	{
		name: 'app/files-to-lint',
		files: ['**/*.{ts,mts,tsx,vue}'],
		plugins: {
			prettier,
		},
	},
	globalIgnores(['**/dist/**', '**/dist-ssr/**', '**/coverage/**']),
	pluginVue.configs['flat/essential'],
	vueTsConfigs.recommended,
	skipFormatting,
	{
		rules: {
			// ✅ vue/attributes-order: 템플릿 속성 순서 규칙 추가
			'vue/attributes-order': [
				'error',
				{
					order: [
						'DEFINITION', // v-is
						'LIST_RENDERING', // v-for
						'CONDITIONALS', // v-if, v-show, v-else-if
						'RENDER_MODIFIERS', // v-once, v-pre
						'GLOBAL', // id, ref, key
						'UNIQUE', // slot, name
						'TWO_WAY_BINDING', // v-model
						'OTHER_DIRECTIVES', // custom directives
						'OTHER_ATTR', // type, value, placeholder
						'EVENTS', // @click, @input
						'CONTENT', // class, style
					],
				},
			],

			// ✅ Prettier 설정
			'prettier/prettier': [
				'error',
				{
					printWidth: 100,
					trailingComma: 'es5',
					tabWidth: 2,
					semi: false,
					singleQuote: true,
					endOfLine: 'auto',
					useTabs: true,
					arrowParens: 'always',
					quoteProps: 'consistent',
				},
			],
		},
	}
)
