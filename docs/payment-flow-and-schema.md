# 支付运行流程与数据库关联说明

本文件用于集中描述支付端到端运行流程、关键类与方法、以及数据库核心表与字段关系，便于你修改流程/字段后让我据此快速调整代码。后续请直接在本文件中标注你的变更需求。

## 概览
- 范围：统一下单、渠道支付、异步/同步回调、商户查单、关闭/取消、补单轮询
- 依据：控制器/服务/渠道适配/通知模块源码与实体、Mapper、DDL
- 使用说明：你可编辑“可修改点”或在各章节内补充/调整字段与流程；我会按本文件的变更执行代码修改与验证

## 运行流程
- 统一下单入口
  - 验签与请求解析：ApiController.getRQByWithMchSign（[ApiController.java](../jeepay-payment/src/main/java/com/jeequan/jeepay/pay/ctrl/ApiController.java)）
  - 下单主流程：AbstractPayOrderController.unifiedOrder（[AbstractPayOrderController.java](../jeepay-payment/src/main/java/com/jeequan/jeepay/pay/ctrl/payorder/AbstractPayOrderController.java)）
  - 路由与校验：
    - 商户/应用配置：ConfigContextQueryService（[ConfigContextQueryService.java](../jeepay-payment/src/main/java/com/jeequan/jeepay/pay/service/ConfigContextQueryService.java)）
    - 产品/通道/费率选择：findRouteConfig（同 AbstractPayOrderController）
    - 渠道服务装配：checkMchWayCodeAndGetService（同 AbstractPayOrderController）
  - 订单生成与入库：genPayOrder → PayOrderService.save（[PayOrderService.java](../jeepay-service/src/main/java/com/jeequan/jeepay/service/impl/PayOrderService.java)）
- 渠道支付与返回
  - 抽象与实现：IPaymentService / AbstractPaymentService / VortaqpayPaymentService（[VortaqpayPaymentService.java](../jeepay-payment/src/main/java/com/jeequan/jeepay/pay/channel/vortaqpay/VortaqpayPaymentService.java)）
  - 返回包装：ChannelRetMsg → CommonPayDataRS（支付数据/跳转/条码等）
  - 状态推进与轮询：processChannelMsg（AbstractPayOrderController）
- 异步/同步回调
  - 回调入口：ChannelNoticeController.doNotify / doReturn（[ChannelNoticeController.java](../jeepay-payment/src/main/java/com/jeequan/jeepay/pay/ctrl/payorder/ChannelNoticeController.java)）
  - 渠道回调服务：IChannelNoticeService（按 ifCode 命名的实现类）
  - 落库与通知：PayOrderService.updateIng2Success|Fail → PayOrderProcessService.confirmSuccess → PayMchNotifyService.payOrderNotify
- 查单与补单
  - 商户查单：QueryOrderController.queryOrder → DB 查询 → QueryPayOrderRS（[QueryOrderController.java](../jeepay-payment/src/main/java/com/jeequan/jeepay/pay/ctrl/payorder/QueryOrderController.java)）
  - 轮询补单：PayOrderReissueMQReceiver.receive → ChannelOrderReissueService.processPayOrder（装配 IPayOrderQueryService）
- 关闭/取消订单
  - CloseOrderController.closeOrder（[CloseOrderController.java](../jeepay-payment/src/main/java/com/jeequan/jeepay/pay/ctrl/payorder/CloseOrderController.java)）
  - INIT 直接关闭；ING 需装配 IPayOrderCloseService 调上游成功后 DB 关闭

## 核心表与关联
- t_pay_order（支付订单）
  - 标识与关联：pay_order_id；mch_no → t_mch_info；app_id → t_mch_app
  - 渠道与方式：if_code（接口类型）、way_code（支付方式）、channel_order_no（上游单号）
  - 金额与状态：amount、currency、state（INIT/ING/SUCCESS/FAIL/CLOSED）、expired_time、success_time
  - 商户交互：notify_url、subject、body、client_ip
  - 错误信息：err_code、err_msg
  - 费率与通道扩展：mch_fee_rate/amount、channel_id/channel_provider_id/channel_if_code
  - 参考：实体（[PayOrder.java](../jeepay-core/src/main/java/com/jeequan/jeepay/core/entity/PayOrder.java)）、Mapper（[PayOrderMapper.java](../jeepay-service/src/main/java/com/jeequan/jeepay/service/mapper/PayOrderMapper.java)）、DDL（[init.sql](./sql/init.sql)）
- t_refund_order（退款订单）
  - 关联：pay_order_id → t_pay_order；channel_pay_order_no ↔ t_pay_order.channel_order_no
  - 金额状态：refund_amount、currency、state、success_time
  - 参考：实体（[RefundOrder.java](../jeepay-core/src/main/java/com/jeequan/jeepay/core/entity/RefundOrder.java)）、DDL（[init.sql](./sql/init.sql)）
- t_mch_info（商户）
  - 关联：mch_no ← t_pay_order.mch_no
  - 参考：实体（[MchInfo.java](../jeepay-core/src/main/java/com/jeequan/jeepay/core/entity/MchInfo.java)）、DDL（[init.sql](./sql/init.sql)）
- t_mch_app（应用）
  - 关联：app_id ← t_pay_order.app_id
  - 参考：实体（[MchApp.java](../jeepay-core/src/main/java/com/jeequan/jeepay/core/entity/MchApp.java)）
- t_pay_interface_config（接口配置）
  - 维度与键：info_type + info_id + if_code（商户维度 info_type=2，info_id=app_id；服务商维度 info_type=1，info_id=isv_no）
  - 参考：实体（[PayInterfaceConfig.java](../jeepay-core/src/main/java/com/jeequan/jeepay/core/entity/PayInterfaceConfig.java)）
- t_pay_way（支付方式）
  - 关联：way_code ← t_pay_order.way_code
  - 参考：实体（[PayWay.java](../jeepay-core/src/main/java/com/jeequan/jeepay/core/entity/PayWay.java)）
- t_mch_notify_record（商户通知）
  - 关联：order_id + order_type（1支付/2退款），mch_order_no、mch_no、app_id
  - 参考：实体（[MchNotifyRecord.java](../jeepay-core/src/main/java/com/jeequan/jeepay/core/entity/MchNotifyRecord.java)）、DDL（[init.sql](./sql/init.sql)）
- t_mch_fund_flow（资金流水）
  - 关联：mch_no；biz_order_id（关联支付或退款订单的业务ID）
  - 参考：实体（[MchFundFlow.java](../jeepay-core/src/main/java/com/jeequan/jeepay/core/entity/MchFundFlow.java)）、DDL（[init.sql](./sql/init.sql)）

## 请求/响应对象
- 统一下单：UnifiedOrderRQ / UnifiedOrderRS（[UnifiedOrderRQ.java](file:///Users/robot/www/jeepay/jeepay/jeepay-payment/src/main/java/com/jeequan/jeepay/pay/rqrs/payorder/UnifiedOrderRQ.java) | [UnifiedOrderRS.java](file:///Users/robot/www/jeepay/jeepay/jeepay-payment/src/main/java/com/jeequan/jeepay/pay/rqrs/payorder/UnifiedOrderRS.java)）
 - 统一下单：UnifiedOrderRQ / UnifiedOrderRS（[UnifiedOrderRQ.java](../jeepay-payment/src/main/java/com/jeequan/jeepay/pay/rqrs/payorder/UnifiedOrderRQ.java) | [UnifiedOrderRS.java](../jeepay-payment/src/main/java/com/jeequan/jeepay/pay/rqrs/payorder/UnifiedOrderRS.java)）
 - 查单：QueryPayOrderRQ / QueryPayOrderRS（[QueryPayOrderRS.java](../jeepay-payment/src/main/java/com/jeequan/jeepay/pay/rqrs/payorder/QueryPayOrderRS.java)）
 - 关闭：ClosePayOrderRQ / ClosePayOrderRS（[ClosePayOrderRS.java](../jeepay-payment/src/main/java/com/jeequan/jeepay/pay/rqrs/payorder/ClosePayOrderRS.java)）
 - 渠道返回包装：ChannelRetMsg（包含 ChannelState/needQuery/responseEntity）（[ChannelRetMsg.java](../jeepay-payment/src/main/java/com/jeequan/jeepay/pay/rqrs/msg/ChannelRetMsg.java)）

## 可修改点（后续定制建议）
- 路由策略：AbstractPayOrderController.findRouteConfig（产品/通道选择、费率来源）
- 渠道选择：if_code 与 way_code 映射；新增/替换渠道服务实现（IPaymentService、IChannelNoticeService、IPayOrderQuery/CloseService）
- 订单字段：t_pay_order 扩展（渠道自定义、风控标记、分账相关等）
- 通知策略：t_mch_notify_record 的重试次数、签名内容、回传参数构造（PayMchNotifyService）
- 配置维度：t_pay_interface_config 的 info_type 与 info_id 组合（服务商/商户维度切换）
- 状态机：INIT/ING/SUCCESS/FAIL/CLOSED 的流转条件（processChannelMsg 与各 Service 更新方法）

## 变更指引
- 若要变更流程：在“运行流程”章节按模块补充或调整步骤说明，并标注需要我修改的类/方法
- 若要变更数据库：在“核心表与关联”下对应表增加/修改/删除字段项，标注字段含义与约束
- 我将以本文件为源执行代码调整（控制器/服务/渠道/实体/DDL），并在修改完成后回填校验与代码引用
