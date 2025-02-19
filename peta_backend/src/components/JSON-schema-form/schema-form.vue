<template>
	<a-form ref="schemaFormRef" v-bind="formItemLayout">
		<template v-for="(formItem, index) in formSchema.formItem.filter((item) => !item.hidden)" :key="formItem.field">
			<a-spin :spinning="formItem.loading || false">
				<a-form-item
					:help="formItem.help"
					:extra="formItem.extra"
					:label="formItem.label"
					:name="formItem.field"
					v-bind="{ ...formItem.props, ...validateInfos[formItem.field] }"
					:type="undefined"
				>
					<component
						:is="getComponent(setDefaultType(formItem.type))"
						v-model:value="modelRef[formItem.field]"
						:form-item="formItem"
						v-on="{ ...getTriggerEvent(formItem) }"
					/>
				</a-form-item>
			</a-spin>
		</template>
		<template v-if="$slots['operate-button']">
			<a-form-item
				:wrapper-col="{
					span: formItemLayout.wrapperCol.span,
					offset: formItemLayout.labelCol.span
				}"
			>
				<slot name="operate-button"></slot>
			</a-form-item>
		</template>
	</a-form>
</template>

<script lang="ts">
import { defineComponent, reactive, getCurrentInstance, isReactive, ref, isRef, createVNode, PropType } from 'vue';
import { Form, Spin } from 'ant-design-vue';
import { isString, isFunction, isAsyncFunction } from '@/utils/is';
import components from './components';

const useForm = Form.useForm;

export default defineComponent({
	name: 'DynamicForm',
	components: {
		...components,
		[Spin.name]: Spin,
		[Form.name]: Form,
		[Form.Item.name]: Form.Item
	},
	props: {
		formSchema: {
			// 动态验证表单
			required: true,
			type: Object as PropType<FormSchema>
		},
		fields: {
			// 预置字段默认值
			type: Object,
			default: () => ({})
		}
	},
	setup(props) {
		// a-form
		const schemaFormRef = ref<InstanceType<typeof Form>>()!;
		// 表单实例
		const formInstance = getCurrentInstance();

		// 表单布局
		const formItemLayout = {
			labelCol: { span: 5 },
			wrapperCol: { span: 20 },
			...props.formSchema.formItemLayout
		};

		// 表单项
		const modelRef = reactive(
			props.formSchema.formItem.reduce((previousValue, currentValue) => {
				currentValue.eventObject ??= {};
				// Object.keys(currentValue.eventObject).forEach(key => {
				//   const fn = currentValue.eventObject[key]
				//   currentValue.eventObject[key] = (...rest) => {
				//     fn(...rest, formInstance)
				//   }
				// })
				previousValue[currentValue.field] = currentValue.value;
				return previousValue;
			}, {})
		);
		// 如果有默认值，则覆盖
		props.fields && Object.assign(modelRef, props.fields);

		// 异步设置默认数据
		props.formSchema.formItem.forEach(async (item: FormItem) => {
			// 是否需要loading
			if (Object.prototype.hasOwnProperty.call(item, 'loading')) {
				item.loading = true;
			}
			// 异步选项
			if (isFunction(item.asyncOptions) || isAsyncFunction(item.asyncOptions)) {
				item.options = await item.asyncOptions(item, formInstance).finally(() => (item.loading = false));
			} else if (isFunction(item.asyncValue) || isAsyncFunction(item.asyncValue)) {
				// 异步默认值
				modelRef[item.field] = await item.asyncValue(item, formInstance).finally(() => (item.loading = false));
			}
		});

		// 生成表单验证规则
		const rulesRef = reactive(
			props.formSchema.formItem
				.filter((item) => !item.hidden)
				.reduce((previousValue, currentValue) => {
					currentValue.rules && (previousValue[currentValue.field] = currentValue.rules);
					return previousValue;
				}, {})
		);

		// console.log(modelRef, '表单');
		// console.log(rulesRef, '表单验证规则');

		// const watchCallback = props.formSchema.watchCallback ?? (() => ({}))
		//
		// // 是否有需要监测的字段
		// props.formSchema.watchKeys && watch(props.formSchema.watchKeys.map(item => () => modelRef[item]), (curr, prev) => watchCallback(curr, {
		//   dynamicForm: props.formSchema,
		//   modelRef
		// }))
		// watch(props.formSchema.watchKeys.map(item => () => modelRef[item]), eval(props.formSchema.watchCallback))

		const { resetFields, validate, validateInfos, validateField } = useForm(modelRef, rulesRef);

		const preset = [
			'input',
			'textarea',
			'select',
			'radio',
			'checkbox',
			'input-number',
			'input-range',
			'switch',
			'image',
			'date',
			'time',
			'tree',
			'district',
			'editor'
		];

		// 获取组件名称
		const getComponent = (type) => {
			// 预设组件
			if (preset.includes(type)) {
				return 'schema-form-' + type;
			} else if (isReactive(type) || isRef(type)) {
				// 自定义组件
				return createVNode(type);
			} else {
				// 不识别组件
				return type;
			}
		};

		// 设置触发表单项验证的事件
		const setTriggerEvent =
			({ field, trigger }) =>
			() =>
				validate(field, { trigger }).catch(() => ({}));

		// 获取触发表单项验证的时机
		const getTriggerEvent = (formItem) => {
			const events = {};
			const field = formItem.field;
			if (Array.isArray(formItem.rules)) {
				// 如果是数组
				formItem.rules.forEach((ruleItem) => {
					if (Array.isArray(ruleItem.trigger)) {
						ruleItem.trigger.forEach(
							(triggerItem) =>
								(events[triggerItem] = setTriggerEvent({
									field,
									trigger: triggerItem
								}))
						);
					} else if (isString(ruleItem.trigger)) {
						events[ruleItem.trigger] = setTriggerEvent({
							field,
							trigger: ruleItem.trigger
						});
					}
				});
			} else if (formItem.rules?.trigger) {
				// 如果是对象
				const trigger = formItem.rules?.trigger;
				events[trigger] = setTriggerEvent({ field, trigger });
				if (Array.isArray(trigger)) {
					trigger.forEach(
						(triggerItem) => (events[triggerItem] = setTriggerEvent({ field, trigger: triggerItem }))
					);
				} else if (isString(trigger)) {
					events[trigger] = setTriggerEvent({ field, trigger: trigger });
				}
			}
			return events;
		};
		function setDefaultType(type) {
			return type || 'input';
		}

		return {
			formItemLayout,
			validate,
			resetFields,
			validateField,
			validateInfos,
			modelRef,
			schemaFormRef,
			getTriggerEvent,
			getComponent,
			setDefaultType
		};
	}
});
</script>

<style lang="scss" scoped>
.ant-form {
	.ant-input-group {
		display: flex;
	}

	.ant-checkbox-wrapper {
		//margin-left: 0;
	}
}
</style>
