<template>
	<a-select
		v-model:value="modelValue"
		v-bind="formItem.props"
		:options="formItem.options"
		v-on="formItem.eventObject"
	/>
</template>
<script lang="ts">
import { defineComponent, PropType, computed } from 'vue';
import { Select } from 'ant-design-vue';

export default defineComponent({
	name: 'SchemaFormSelect',
	components: {
		[Select.name]: Select,
		[Select.Option.name]: Select.Option
	},
	props: {
		formItem: {
			// 表单项
			type: Object as PropType<FormItem>,
			default: () => ({})
		},
		value: undefined as any // 表单项值
	},
	emits: ['update:value'],
	setup(props, { attrs, emit }) {
		const modelValue = computed({
			get: () => props.value,
			set: (val) => emit('update:value', val)
		});

		return {
			modelValue
		};
	}
});
</script>
