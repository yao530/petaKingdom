<template>
	<a-modal
		v-model:visible="visible"
		v-bind="$attrs"
		:confirm-loading="confirmLoading"
		:afterClose="remove"
		@ok="onOk"
	>
		<schema-form ref="dynamicForm" :fields="fields" :form-schema="dynamicValidateForm" />
	</a-modal>
</template>

<script lang="ts">
import { defineComponent, reactive, toRefs, ref, PropType } from 'vue';
import { Modal } from 'ant-design-vue';
import { SchemaForm } from '@/components/JSON-schema-form';

export default defineComponent({
	name: 'OperateModal',
	components: { [Modal.name]: Modal, SchemaForm },
	props: {
		remove: {
			// 移除模态框
			type: Function
		},
		formSchema: {
			// 表单项
			type: Object as PropType<FormSchema>,
			default: () => ({})
		},
		fields: {
			// 默认值，一般编辑时传入
			type: Object,
			default: () => ({})
		},
		hiddenFields: {
			// 不需要显示的字段（表单项）
			type: Array,
			default: () => []
		},
		handleOk: {
			// 点击确定
			type: Function
		}
	},
	setup(props, ctx) {
		const dynamicForm = ref<InstanceType<typeof SchemaForm>>();

		const state = reactive({
			visible: true,
			confirmLoading: false,
			dynamicValidateForm: props.formSchema
		});

		props.hiddenFields.forEach((field) => {
			state.dynamicValidateForm.formItem.find((item) => item.field == field)!.hidden = !!props.fields;
		});

		const onOk = () => {
			state.confirmLoading = true;
			dynamicForm.value
				?.validate()
				.then(async () => {
					await (props.handleOk && props.handleOk(dynamicForm.value?.modelRef, state)).finally(
						() => (state.confirmLoading = false)
					);
					state.visible = false;
				})
				.catch((err) => {
					console.log('error', err);
					state.confirmLoading = false;
				});
		};

		return {
			...toRefs(state),
			onOk,
			dynamicForm
		};
	}
});
</script>

<style scoped></style>
