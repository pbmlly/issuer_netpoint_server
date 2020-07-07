-- ===================================用户登录 begin=================================
#sql("userLogin")
select u.UUID,u.USER_NAME,u.USER_PWD,u.USER_ID,u.POS_ID,u.ORG_CODE,u.LAST_MODIFY_PASSWORD_TIME,
case when u.IS_ACTIVE=1 and p.IsValid=1 then 1 else 0 end as IS_ACTIVE,p.name as POS_NAME,u.TYPE,u.CHANNEL_TYPE
from pos_user u
inner JOIN sys_positioninfo p on u.POS_ID=p.code
where  user_id=?
#end

#根据机构编码查询角色
#sql("queryRolesByOrgCodeAndType")
 select r.roleCode,r.roleName,ro.orgCode as userId from sys_role_org ro
INNER JOIN sys_role r on ro.roleCode = r.roleCode and ro.orgType='U'
where ifnull(ro.isVoid,0)=0 and ifnull(r.isVoid,0)=0 and ro.orgCode=?
union
 select r.roleCode,r.roleName,u.USER_ID as userId from sys_role_org ro
INNER JOIN sys_role r on ro.roleCode = r.roleCode
INNER JOIN pos_user u on ro.orgCode = u.pos_id and ro.orgType='P'
where ifnull(ro.isVoid,0)=0 and ifnull(r.isVoid,0)=0 and u.USER_ID=?
#end
-- ===================================./用户登录 end=================================
-- ===================================数据字典查询 begin=================================
#sql("queryDictionary")
select d.code,d.name
from sys_dictionaryinfo d
INNER JOIN sys_dictionaryinfo p on d.ParentId = p.id
where p.code =?
#end

#sql("queryAllDictionary")
select id,parentId,code,name from sys_dictionaryinfo
#end
-- ===================================./数据字典查询 end=================================

-- ===================================基础信息 begin=================================
-- 删除用户信息表
#sql("deleteUserHistoryInfoByPK")
delete FROM etc_UserInfo_History
where  id= ? and opTime = ?
#end

#sql("deleteUserInfoByPK")
delete FROM etc_UserInfo   where  id = ?
#end

-- 删除车辆信息表
#sql("deleteVehicleHistoryInfoByPK")
delete  FROM etc_VehicleInfo_History
where id= ? and opTime = ?
#end

#sql("deleteVehicleInfoByPK")
delete FROM  etc_VehicleInfo   where  id = ?
#end

-- 删除obu信息表
#sql("deleteObuHistoryInfoByPk")
delete FROM Etc_ObuInfo_History
where id= ? and opTime = ?
#end

#sql("deleteObuInfoByPk")
delete FROM Etc_ObuInfo
where id= ?
#end


-- 删除card信息表
#sql("deleteCardHistoryInfoByPk")
delete FROM Etc_CardInfo_History
where id= ? and opTime = ?
#end

#sql("deleteCardInfoByPk")
delete FROM Etc_CardInfo
where id= ?
#end
-- 查询卡信息
#sql("selectCardInfoByPk")
select * FROM Etc_CardInfo
where id= ?
#end

-- 查询卡的区域信息
#sql("selectPositioninfoByCode")
select code, posType,areaType from sys_positioninfo where code = ?
#end
-- ===================================./基础信息 end=================================

-- ===================================查询订单信息 begin==============================
--查询订单信息 processType 1-手持机  2-快发 3-通用类型（手持、开发） orderStatus=2- 未支付 3- 已支付 postStatus=1- 未发货
#sql("queryOrderInfo")
SELECT
	  orderId,createTime,vehicleCode,userId,accountName,
		userType,
		(SELECT NAME FROM sys_dictionaryinfo  WHERE code=online_orders.userType AND parentId=18 LIMIT 1) AS userTypeDesc,
		cardType,
    (SELECT NAME FROM sys_dictionaryinfo  WHERE code=online_orders.cardType AND parentId=88 LIMIT 1) AS cardTypeDesc,
    payCardType,
    (SELECT NAME FROM sys_dictionaryinfo  WHERE code=online_orders.payCardType AND parentId=430 LIMIT 1) AS payCardTypeDesc,
    mobile,userIdType,
    (SELECT NAME FROM sys_dictionaryinfo  WHERE code=online_orders.userIdType AND parentId=21 LIMIT 1) AS userIdTypeDesc,
    (SELECT NAME FROM sys_dictionaryinfo  WHERE code=online_orders.plateColor AND parentId=51 LIMIT 1) AS plateColorDesc,
    userIdNum,
		postName,postPhone,postAddr,plateNum,plateColor,vehicleType,channelId,
   (SELECT NAME FROM sys_dictionaryinfo  WHERE code=online_orders.vehicleType AND parentId=62 LIMIT 1) AS vehicleTypeDesc,
		outsideDimensions,vin,limitPerNum,totalWeight,engineNo,wheelCount,
		axleCount,axleDistance,axisType,orderType, postStatus,orderStatus,
		 (SELECT NAME FROM sys_dictionaryinfo  WHERE code=online_orders.orderType AND parentId=656 LIMIT 1) AS orderTypeDesc,
		 (SELECT NAME FROM sys_dictionaryinfo  WHERE code=online_orders.postStatus AND parentId=661 LIMIT 1) AS postStatusDesc,
		 (SELECT NAME FROM sys_dictionaryinfo  WHERE code=online_orders.orderStatus AND parentId=650 LIMIT 1) AS orderStatusDesc
FROM online_orders WHERE processType in (2,3) AND #(condition)
#end

--查询手持机订单信息  processType 1-手持机  2-快发 3-通用类型（手持、开发）
#sql("queryHandSetOrderInfo")
SELECT
	  orderId,createTime,vehicleCode,userId,accountName,
		userType,
		(SELECT NAME FROM sys_dictionaryinfo  WHERE code=online_orders.userType AND parentId=18 LIMIT 1) AS userTypeDesc,
		cardType,
    (SELECT NAME FROM sys_dictionaryinfo  WHERE code=online_orders.cardType AND parentId=88 LIMIT 1) AS cardTypeDesc,
    payCardType,
    (SELECT NAME FROM sys_dictionaryinfo  WHERE code=online_orders.payCardType AND parentId=430 LIMIT 1) AS payCardTypeDesc,
    mobile,userIdType,
    (SELECT NAME FROM sys_dictionaryinfo  WHERE code=online_orders.userIdType AND parentId=21 LIMIT 1) AS userIdTypeDesc,
    (SELECT NAME FROM sys_dictionaryinfo  WHERE code=online_orders.plateColor AND parentId=51 LIMIT 1) AS plateColorDesc,
    userIdNum,
		postName,postPhone,postAddr,plateNum,plateColor,vehicleType,channelId,
   (SELECT NAME FROM sys_dictionaryinfo  WHERE code=online_orders.vehicleType AND parentId=62 LIMIT 1) AS vehicleTypeDesc,
		outsideDimensions,vin,limitPerNum,totalWeight,engineNo,wheelCount,
		axleCount,axleDistance,axisType,orderType, postStatus,orderStatus,
		 (SELECT NAME FROM sys_dictionaryinfo  WHERE code=online_orders.orderType AND parentId=656 LIMIT 1) AS orderTypeDesc,
		 (SELECT NAME FROM sys_dictionaryinfo  WHERE code=online_orders.postStatus AND parentId=661 LIMIT 1) AS postStatusDesc,
		 (SELECT NAME FROM sys_dictionaryinfo  WHERE code=online_orders.orderStatus AND parentId=650 LIMIT 1) AS orderStatusDesc
FROM online_orders
WHERE processType in (1,3) and (status=0 OR (status in (1,2) AND posId=? AND operatorId=?)) AND plateNum=? AND plateColor=?
AND userIdType=? and userIdNum=? AND orderType=?
#end

-- 查询微信小程序与绑卡信息 只查询处理中的订单 status=1-处理中 processType=4-微信小程序二发
#sql("queryWXLittleProBindOrder")
  select b.orderId,b.cardId,b.obuId,o.orderType,o.plateNum,o.plateColor,o.bankCode,o.status from online_orders_bind b
  inner join online_orders o on b.orderId = o.orderId and o.status > 0 and o.processType=4
  where b.cardId =? or b.obuId = ? order by b.createTime,o.createTime desc
#end

-- 查询微信小程序二发订单信息  processType 1-手持机  2-快发 3-通用类型（手持、开发） 4-微信小程序二发
#sql("queryWXLittleProgram2OrderInfo")
SELECT
	  o.orderId,o.createTime,o.vehicleCode,o.userId,o.accountName,
		o.userType,
		(SELECT NAME FROM sys_dictionaryinfo  WHERE code=o.userType AND parentId=18 LIMIT 1) AS userTypeDesc,
		o.cardType,
    (SELECT NAME FROM sys_dictionaryinfo  WHERE code=o.cardType AND parentId=88 LIMIT 1) AS cardTypeDesc,
    o.payCardType,
    (SELECT NAME FROM sys_dictionaryinfo  WHERE code=o.payCardType AND parentId=430 LIMIT 1) AS payCardTypeDesc,
    o.mobile,o.userIdType,
    (SELECT NAME FROM sys_dictionaryinfo  WHERE code=o.userIdType AND parentId=21 LIMIT 1) AS userIdTypeDesc,
    (SELECT NAME FROM sys_dictionaryinfo  WHERE code=o.plateColor AND parentId=51 LIMIT 1) AS plateColorDesc,
    o.userIdNum,o.postName,o.postPhone,o.postAddr,o.plateNum,o.plateColor,o.vehicleType,o.channelId,
   (SELECT NAME FROM sys_dictionaryinfo  WHERE code=o.vehicleType AND parentId=62 LIMIT 1) AS vehicleTypeDesc,
		o.outsideDimensions,o.vin,o.limitPerNum,o.totalWeight,o.engineNo,o.wheelCount,
		o.axleCount,o.axleDistance,o.axisType,o.orderType, o.postStatus,o.orderStatus,
		 (SELECT NAME FROM sys_dictionaryinfo  WHERE code=o.orderType AND parentId=656 LIMIT 1) AS orderTypeDesc,
		 (SELECT NAME FROM sys_dictionaryinfo  WHERE code=o.postStatus AND parentId=661 LIMIT 1) AS postStatusDesc,
		 (SELECT NAME FROM sys_dictionaryinfo  WHERE code=o.orderStatus AND parentId=650 LIMIT 1) AS orderStatusDesc
FROM online_orders o
WHERE o.processType =4 and o.status = 1 AND o.orderId=? AND #(condition) order by o.createTime desc
#end

-- 查询订单状态  orderStatus=2- 未支付 3- 已支付 postStatus=1- 未发货
#sql("queryOrderStatus")
select `status`,posId,operatorId,orderStatus,postStatus from online_orders WHERE orderId=?
#end
-- 更新订单状态  orderStatus=2- 未支付 3- 已支付 postStatus=1- 未发货
#sql("updateOrderStatus")
UPDATE online_orders SET `status`=1,posId=?,operatorId=?,updateTime=? WHERE orderId=? AND `status` = 0 and (orderStatus in (2,3) or orderStatus is null) and postStatus = 1
#end

-- 更新订单状态
#sql("updateOrderMile")
UPDATE online_orders SET delivery=?,expressType=?,expressId=? WHERE orderId=?
#end

-- 判断订单图片是否存在
#sql("isExistOnlineImgByUserId")
select count(1) as num from onlinepicture where UserId = ?
#end
-- ===================================./基础信息 end=================================

-- ===================================客户编码查询 begin=================================
-- 根据证件号查询客户编码
#sql("findUserIdByUserIdNum")
select id,operation from userupload where userIdType = #para(userIdType) and userIdNum = #para(userIdNum) order  by csntFilename desc
#end

-- 查询易构用户信息
#sql("findYGUserUserIdByUserIdNum")
  select id from TBL_USER where userIdType = #para(userIdType) and userIdNum = #para(userIdNum) and flag in (1,2)
#end


-- 查询用户临时信息
#sql("findUserIdTempByUserIdNum")
  select id from etc_userid_temp where userIdType = #para(userIdType) and userIdNum = #para(userIdNum)
#end

-- 根据userId查询客户编码
#sql("findUserIdById")
select id from userupload where id = #para(id)
#end


#sql("findUserIdCenterByUserIdNum")
select id from etc_userinfo
  where userIdType = #para(userIdType) and userIdNum = #para(userIdNum) and operation < 3
#end

-- 根据userId查询客户编码临时表
#sql("findUserIdTempById")
select id from etc_userid_temp where id = #para(id)
#end

-- 根据userId查询易构客户编码
#sql("findYGUserIdById")
  select id from TBL_USER where id = #para(id)
#end
-- ===================================./客户编码查询 end=================================


-- region ===================================./工行自动审核功能结束 end==============================
-- 查询工行在线订单未审核数据，
#sql("queryOnlineapplyList")
select a.* ,
(select id from tbl_agency where oldid =  a.bankCode) as newbankid,
(select b.channelid from channelid_bankid_relation b where b.oldid = a.bankCode) as channelid
from onlineapply a
where  ( a.examineResult is null || a.examineResult = 0 ) and a.bankCode = '9100000'
  #if(limit)
    limit #para(limit)
  #end
#end

-- 查询车辆信息是否在本地存在
#sql("querycheckvehicleinfoisexist")
select id from vehicleupload  where id = ?
#end

-- 查询车辆信息是否在易购存在
#sql("querycheckvehicleinfoisexistYG")
SELECT id FROM  TBL_VEHICLE  where id = ?
#end

-- 自动审核成功，更新审核状态
#sql("updateOnlineapplyAudit")
update onlineapply set flowStatus = 1,approvalId='*',approvalTime = NOW(),
    examineresult = 1,examinedescription = '通过'
    where  id = ?
#end
-- endregion ===================================./工行自动审核功能结束 end==============================

-- ===================================车管所车牌查询 begin=================================
#sql("findGgsjtsVehicle")
SELECT jjv.HPHM,jjv.HPZL
,jjv.SYR AS "ownerName"
,jjv.SFZMHM AS "ownerIdNum"
,jjv.LXDH AS "ownerTel"
,jjv.ZSXXDZ AS "address"
,v.HPZLNAME AS "vehicleType"
,jjv.CLPP1||jjv.CLXH AS "vehicleModel"
,jjv.CLSBDH AS "VIN"
,jjv.FDJH AS "engineNum"
,jjv.DABH AS "fileNum"
,jjv.HDZK AS "approvedCount"
,jjv.ZZL AS "totalMass"
,jjv.ZBZL AS "maintenanceMass"
,jjv.HDZZL AS "permittedWeight"
,jjv.CWKC||'X'||jjv.CWKK||'X'||jjv.CWKG AS "outsideDimensions"
,jjv.ZQYZL AS "permittedTowWeight"
,jjv.JYHGBZBH AS "testRecord"
FROM  JG_JGPT_VEHICLE jjv
INNER JOIN VEHICLE v ON jjv.HPZL=v.HPZL
WHERE jjv.HPHM=?
ORDER BY jjv.HPZL
#end
-- ===================================./车管所车牌查询 end=================================

-- ===================================OUBId激活 begin=================================
-- 查询OBU状态
#sql("findObuInfoById")
  select o.id as obuId,o.isActive,o.vehicleId,c.id as cardId
from etc_obuinfo o
INNER JOIN etc_cardinfo c on o.userId = c.userId and o.vehicleId = c.vehicleId
where o.id = #para(id)
#end
-- 查询OBU状态 status=2-已处理完成 orderStatus< 4-已撤单 s.status = 2-接受申请 4-确认撤单
#sql("findObuInfoCardInfoById")
  select d.orderType,s.serviceType,o.id as obuId,o.isActive,o.vehicleId,c.id as cardId,d.postStatus
from online_orders d
inner JOIN etc_obuinfo o on d.obuId = o.id
inner JOIN etc_cardinfo c on o.userId = c.userId and o.vehicleId = c.vehicleId
left JOIN im_order_service_list s on d.orderId = s.orderId and s.status in (2,4)
where d.status=2 and IFNULL(d.orderStatus,2) < 4 and d.obuId = #para(id) order by d.createTime desc,c.createTime desc
#end


-- 查询OBU信息
#sql("findAllObuInfoById")
  select * from etc_obuinfo where id = ? order by createTime desc
#end

-- 查询OBU二次激活申请审核信息
#sql("findActivateApplyByObuId")
  select * from etc_obuactivate_apply  where obuid = ? order by createTime desc
#end

-- 跟新OBU状态
#sql("updateObuInfoById")
 update etc_obuinfo set isActive=#para(isActive),activeTime=#para(activeTime),
 activeType=#para(activeType), activeChannel=#para(activeChannel),updatetime=#para(updatetime)
 where id=#para(id)
#end
-- ===================================./OUBId激活 end=================================

-- ===================================OUB二次激活申请 begin=================================
-- 查询OBU二次激活申请单状态为“0-未审核或1-审核通过”的申请条数
#sql("countObuActivateApply")
  select count(1) as num  from etc_obuactivate_apply where obuId = ? and result in (0,1)
#end
-- 通过OBUId查询卡信息
#sql("findCardInfoByObuId")
  select c.id as cardId,o.vehicleId,o.userId,c.bankPost,o.activeTime,o.activeType,o.activeChannel from etc_obuinfo o inner join etc_cardInfo c on o.userId=c.userid and o.vehicleId=c.vehicleId
  where o.Id = ? and o.operation < 3
#end
-- 查询OBU二次激活申请单审核结果
#sql("queryObuApplyResult")
  select result, reason  from etc_obuactivate_apply where obuId = ? and cardId = ? order by createTime desc
#end
-- 判断是否为互联网发行订单
#sql("queryInternetIssuerObu")
  select * from online_orders where obuId = ?
#end
-- ===================================./OUB二次激活申请 end=================================

-- beginregion ===================================卡和OBU发行信息查询 begin=================================
-- 查询处理完成订单的卡、OBU发行信息 部中心订单的channelType='041001' and  postStatus=2  s.status = 2-接受申请 4-确认撤单
#sql("queryProcessedOrderCardOBUInfoList")
select d.orderId,d.accountId,d.vehicleId,d.plateNum,d.plateColor,left(d.channelType,3) as channelType,
c.id as cardId,c.cardType,c.brand as cardBrand,c.model as cardModel,c.agencyId,c.enableTime as cardEnableTime,c.expireTime as cardExpireTime,c.issuedType,c.channelId,c.issuedTime,
o.id as obuId,o.brand as obuBrand,o.model as obuModel,o.obuSign,o.enableTime as obuEnableTime,o.expireTime as obuExpireTime,o.activeTime,o.activeType,o.activeChannel,
s.serviceType
from  online_orders d
inner join etc_obuinfo o on d.obuId = o.id
left join etc_cardinfo c on o.userId = c.userId and o.vehicleId = c.vehicleId
left JOIN im_order_service_list s on d.orderId = s.orderId and s.status in (2,4)
where (d.channelType like '04%'  or d.channelType = '050102') and d.STATUS = 2 and d.postStatus >= 2 and IFNULL(d.orderStatus,2) < 4 and d.obuId = ?
#end
-- endregion ===================================./卡和OBU发行信息查询 end=================================

-- ===================================营改增车辆信息查询 begin=================================
-- 查询易构车辆信息
#sql("findYGVehicleInfoById")
    select id,type,userId,ownerName,ownerIdType,ownerIdNum,ownerTel,address,contact,registeredType,channelId,registeredTime,
  vehicleType,vehicleModel,useCharacter,VIN,engineNum,registerDate,issueDate,fileNum,approvedCount,totalMass,maintenanceMass,
  permittedWeight,outsideDimensions,permittedTowWeight,testRecord,wheelCount,axleCount,axleDistance,axisType,'' as vehicleFeatureVersion,
  '' as vehicleFeatureCode,'' as payAccountNum,flag as operation
  from TBL_VEHICLE where id = #para(id)
#end

-- 查询汇聚平台车辆信息
#sql("findHJPTVehicleInfoById")
    select id,type,userId,ownerName,ownerIdType,ownerIdNum,ownerTel,address,contact,registeredType,channelId,registeredTime,
  vehicleType,vehicleModel,useCharacter,VIN,engineNum,registerDate,issueDate,fileNum,approvedCount,totalMass,maintenanceMass,
  permittedWeight,outsideDimensions,permittedTowWeight,testRecord,wheelCount,axleCount,axleDistance,axisType,'' as vehicleFeatureVersion,
  '' as vehicleFeatureCode,'' as payAccountNum,operation
  from vehicleupload where id = #para(id)   order by  csntfileName desc
#end

##查询线下监控车辆信息条数
#sql("queryEtcOflVehicleCountById")
select count(1) as num
from etc_ofl_vehicleinfo
 where vehicleId = ? and accountid is not null
#end
-- 查询发行平台车辆信息
#sql("findIssueVehicleInfoById")
    select id,type,userId,ownerName,ownerIdType,ownerIdNum,ownerTel,address,contact,registeredType,channelId,registeredTime,
  vehicleType,vehicleModel,useCharacter,VIN,engineNum,registerDate,issueDate,fileNum,approvedCount,totalMass,maintenanceMass,
  permittedWeight,outsideDimensions,permittedTowWeight,testRecord,wheelCount,axleCount,axleDistance,axisType,'' as vehicleFeatureVersion,
  '' as vehicleFeatureCode,'' as payAccountNum,operation
  from etc_vehicleinfo where id = #para(id) and operation < 3
#end
-- ===================================./营改增车辆信息查询 end=================================
-- ===================================营改增OBU信息查询 begin=================================
-- 查询易构车辆信息
#sql("findYGObuInfoById")
    select o.id,o.brand,o.model,'' as obuSign,o.userId,o.vehicleId,o.enableTime,o.expireTime,o.registeredType,o.registeredChannelId,o.registeredTime,
    o.installType,o.installChannelId,o.installTime,o.status,o.statusChangeTime,o.flag as operation,
'' as isActive,'' as activeTime,''as activeType,'' as activeChannel,u.userIdNum,u.userIdType
  from TBL_OBU o inner join tbl_user u on o.userId=u.id where o.id = #para(id) and o.flag < 3
#end

-- 查询汇聚平台车辆信息
#sql("findHJPTObuInfoById")
    select o.id,o.brand,o.model,'' as obuSign,o.userId,o.vehicleId,o.enableTime,o.expireTime,o.registeredType,o.registeredChannelId,o.registeredTime,
    o.installType,o.installChannelId,o.installTime,o.status,o.statusChangeTime,o.operation,
'' as isActive,'' as activeTime,''as activeType,'' as activeChannel,u.userIdNum,u.userIdType
  from obuupload o INNER join userupload u on o.userId = u.id where o.id = #para(id) and o.operation < 3  order by o.csntfileName desc
#end
-- 查询发行库是否存在OBU
#sql("findCenterObuInfoById")
    select o.id,o.brand,o.model, o.obuSign,o.userId,o.vehicleId,o.enableTime,o.expireTime,o.registeredType,o.registeredChannelId,o.registeredTime,
    o.installType,o.installChannelId,o.installTime,o.status,o.statusChangeTime,o.operation,
  o.isActive,  o.activeTime, o.activeType, o.activeChannel,u.userIdNum,u.userIdType
  from etc_obuinfo o INNER JOIN etc_userinfo u on o.userId = u.id where o.id = ? and o.operation < 3
#end
-- ===================================./营改增OBU信息查询 end=================================
-- ===================================营改增卡信息查询 begin=================================
-- 查询易构车辆信息
#sql("findYGCardInfoById")
    select c.id,c.cardType,c.brand,c.model,c.agencyId,c.userId,c.vehicleId,c.enableTime,c.expireTime,c.issuedType,c.channelId,c.issuedTime,
    c.status,c.statusChangeTime,c.flag as operation,u.userIdNum,u.userIdType
  from TBL_USERCARD c INNER JOIN tbl_user u on c.userId = u.id where c.id = #para(id) and c.flag < 3
#end

-- 查询汇聚平台车辆信息
#sql("findHJPTCardInfoById")
   select c.id,c.cardType,c.brand,c.model,c.agencyId,c.userId,c.vehicleId,c.enableTime,c.expireTime,c.issuedType,c.channelId,c.issuedTime,
    c.status,c.statusChangeTime,c.operation,u.userIdNum,u.userIdType
  from cardupload c INNER JOIN userupload u on c.userId = u.id where c.id = #para(id) and c.operation < 3 order by c.csntfileName desc
#end

-- 查询issuer_center信息
#sql("queryCenterCardInfoById")
   select c.id,c.cardType,c.brand,c.model,c.agencyId,c.userId,c.vehicleId,c.enableTime,c.expireTime,c.issuedType,c.channelId,c.issuedTime,
    c.status,c.statusChangeTime,c.operation,u.userIdNum,u.userIdType,c.bankPost
  from etc_cardinfo c LEFT JOIN etc_userinfo u on c.userId = u.id where c.id = ? and c.operation < 3
#end

-- 查询issuer_center卡的待注销信息
#sql("queryCountCardCancelConfirmById")
   select count(1) as num
  from etc_card_cancel_confirm where cardId = ? and confirmStatus = 0
#end
-- ===================================./营改增卡信息查询 end=================================

-- beginregion ===================================./在线申请订单信息 start==============================
#sql("queryOnlineApplyBycarNumber")
select bookId,id,bookDate,carNumber,calcolor,
(Select  name from Sys_DictionaryInfo
  where ParentId=(select id from Sys_DictionaryInfo where code='vehicle_colour')
  and code  = calcolor) as calcolorDesc,
(case   customertype
    when 1 then '个人客户'
    when 2 then '单位客户'
  else NULL
 end ) customerTypeDesc, customerType,seats,
(select name from tbl_agency where tbl_agency.OLDID = bankcode) as bankcodedesc,
type,(case Type when 1 then '储值卡'  when 2 then '记账卡' else NULL end ) typeDesc,
cardType,
(Select  name from Sys_DictionaryInfo
  where ParentId=(select id from Sys_DictionaryInfo where code='payCardType')
  and code  = cardType) as cardTypeDesc,
velchel,passPortType,
(select sys_dictionaryinfo.name from sys_dictionaryinfo
 where sys_dictionaryinfo.code=passporttype
and sys_dictionaryinfo.ParentId=(select id from sys_dictionaryinfo where Code='userIdType')) as passporttypedesc,
passPortId,customerName,postName,postTel,postArea,postAddres,postId,model,
vin,engineNo,dimension,examineResult,examineDescription
from onlineapply
where carNumber = ? and calcolor = ?
order by bookDate desc
#end

#sql("queryOnlineImgByUserId")
select imgpositive,imgback,imghome,imginfo,imgheadstock,imghold from onlinepicture
where UserId = ?
ORDER by  createtime desc
#end

#sql("queryVehicleTypeDesc")
 Select  name from Sys_DictionaryInfo
 where ParentId=(select id from Sys_DictionaryInfo where code='Charge_car_type')
 and code  = ?
#end
#sql("queryOnlineApplyById")
select a.*,c.id as newbankid,
 (select channelid from channelid_bankid_relation where channelid_bankid_relation.bankid  = c.id) as channelid
 from onlineapply a
left join tbl_agency c on c.OLDID = a.bankCode
 where a.id =?
#end

-- 审核成功，更新审核状态
#sql("updateOnlineAudit")
update onlineapply set flowStatus = 1,approvalId= ? ,approvalTime = NOW(),
    examineresult = 1,examinedescription = ?
    where  id = ?
#end
-- endregion ===================================./在线申请订单信息 end==============================

-- ===================================接收邮寄信息 begin=================================
#sql("queryOnlineOrderExpress")
select od.orderId,left(od.channelType,3) as channelType,od.orderType,od.accountId,od.delivery,od.expressType,
od.obuId,obu.brand as obubrand ,obu.model as obuModel,obu.obuSign,od.cardId,card.cardType,
 card.brand,card.model,od.status,od.postStatus,od.orderStatus
from online_orders od
left JOIN etc_cardinfo card on od.cardId = card.id
left JOIN etc_obuinfo  obu  on  od.obuId = obu.id
where od.orderId = ?
#end
-- ===================================接收邮寄信息 end=================================

-- region ===================================接收卡信息接口 begin=================================
-- 查询汇聚平台卡表中 未删除的卡信息
#sql("queryHJPTCardinfoByVehicleId")
 select t.id from
( select * from cardupload
where vehicleId = ?  order by csntfileName desc LIMIT 1  ) t
where operation <> 3 and status not in (4,5,2,3)
#end
-- 查询发行系统卡表中 未删除的卡信息
#sql("queryIssueCardinfoByVehicleId")
select id from etc_cardinfo where operation <> 3 and status not in (2,3,4,5) and vehicleId = ?
#end
-- 查询汇聚平台卡表中 未删除的卡信息
#sql("queryYGCardinfoByVehicleId")
select id from TBL_USERCARD  where flag <> 3 and status not in (2,3,4,5)  and vehicleId = ?
#end
-- endregion ===================================接收卡信息接口 end=================================

-- region ===================================接收OBU信息接口 begin=================================
-- 查询汇聚平台OBU表中 未删除的卡信息
#sql("queryIssueObuinfoByVehicleId")
select id from etc_obuinfo where operation <> 3  and status not in (2,3,4,5) and vehicleId = ?
#end
-- 查询汇聚平台OBU表中 未删除的卡信息
#sql("queryHJPTObuinfoByVehicleId")
select t.id from
(select * from obuupload
where vehicleId = ?  order by csntfileName desc LIMIT 1  ) t
 where operation <> 3 and status not in (2,3,4,5)
#end
-- 查询汇聚平台OBU表中 未删除的卡信息
#sql("queryYGObuinfoByVehicleId")
select id from TBL_OBU  where flag <> 3  and status not in (2,3,4,5) and vehicleId = ?
#end
-- endregion ===================================接收OBU信息接口 end=================================

-- region ===================================在线订单接收接口 begin=================================
#sql("queryBankIdByChannelId")
select * from channelid_bankid_relation where channelid = ?
#end
#sql("queryOnlineOrderByOrderId")
select * from online_orders where orderId = ?
#end
#sql("checkVehIsApp")
select online_orders.*,
(SELECT max(name) as issname FROM  sys_issuerchannel
where sys_issuerchannel.code =online_orders.channelType ) as issname
 from online_orders where vehicleCode = ?  and (orderStatus < 4 or orderStatus is null )  and status < 2
#end
#sql("checkOflVehBindBank")
 select * from etc_ofl_vehicleinfo where vehicleId = ? and  bankPost is not  null
#end

-- endregion ===================================在线订单接收接口 end=================================

-- region ===================================根据订单编号订单查询接口 begin=================================
#sql("queryOrderByOrderId")
SELECT
	  orderId,createTime,vehicleCode,userId,accountName,
		userType,
		(SELECT NAME FROM sys_dictionaryinfo  WHERE code=online_orders.userType
      AND parentId=(select id from Sys_DictionaryInfo where code='userType') LIMIT 1) AS userTypeDesc,
		cardType,
    (SELECT NAME FROM sys_dictionaryinfo WHERE code=online_orders.cardType
   AND parentId=(select id from Sys_DictionaryInfo where code='cardType_1') LIMIT 1) AS cardTypeDesc,
    payCardType,
    (SELECT NAME FROM sys_dictionaryinfo  WHERE code=online_orders.payCardType
   AND parentId=(select id from Sys_DictionaryInfo where code='payCardType') LIMIT 1) AS payCardTypeDesc,
    mobile,userIdType,
    (SELECT NAME FROM sys_dictionaryinfo  WHERE code=online_orders.userIdType
      AND parentId=(select id from Sys_DictionaryInfo where code='user_id_type') LIMIT 1) AS userIdTypeDesc,
    (SELECT NAME FROM sys_dictionaryinfo  WHERE code=online_orders.plateColor
     AND parentId=(select id from Sys_DictionaryInfo where code='VehicleColor') LIMIT 1) AS plateColorDesc,
    userIdNum,obuId,cardId,
		postName,postPhone,postAddr,plateNum,plateColor,vehicleType,channelId,
   (SELECT NAME FROM sys_dictionaryinfo  WHERE code=online_orders.vehicleType
  AND parentId=(select id from Sys_DictionaryInfo where code='vehicleType') LIMIT 1) AS vehicleTypeDesc,
		outsideDimensions,vin,limitPerNum,totalWeight,engineNo,wheelCount,
		axleCount,axleDistance,axisType,orderType, postStatus,orderStatus,
		 (SELECT NAME FROM sys_dictionaryinfo  WHERE code=online_orders.orderType
  AND parentId=(select id from Sys_DictionaryInfo where code='orderType') LIMIT 1) AS orderTypeDesc,
		 (SELECT NAME FROM sys_dictionaryinfo  WHERE code=online_orders.postStatus
  AND parentId=(select id from Sys_DictionaryInfo where code='expressStatus') LIMIT 1) AS postStatusDesc,
		 (SELECT NAME FROM sys_dictionaryinfo  WHERE code=online_orders.orderStatus
  AND parentId=(select id from Sys_DictionaryInfo where code='online_orderstatus') LIMIT 1) AS orderStatusDesc
FROM online_orders WHERE  orderId = ?
#end
-- endregion ===================================根据订单编号订单查询接口 end=================================

-- region ===================================4.6.	车辆发行卡、OBU信息查询接口 begin=================================
#sql("queryIssuerByVehicle")
SELECT left(veh.id,POSITION('_' IN veh.id) -1) as plateNum,
    SUBSTR(veh.id,POSITION('_' IN veh.id) + 1) as plateColor,
   card.id as cardId,obu.id as  obuId,
   obu.isActive,obu.activeTime,obu.activeTime,obu.activeChannel
  FROM etc_vehicleinfo veh
 LEFT JOIN  (select * from  etc_cardinfo  cd  where cd.vehicleid = ?
                    and cd.status not in (4,5) and cd.operation < 3
                    ORDER BY createtime limit 1 ) card on veh.id = card.vehicleId
 LEFT JOIN  (select * from etc_obuinfo cb where cb.vehicleid = ?
       and cb.status not in (4,5) and cb.operation < 3
       ORDER BY createtime limit 1 ) obu  on veh.id = obu.vehicleId
where veh.id = ?
#end
-- endregion ===================================4.6.	车辆发行卡、OBU信息查询接口 end=================================

-- region ===================================线下开户检查 begin=================================
##根据证件号和类型查询用户信息
#sql("queryEtcVehicleByVehicleId")
SELECT  v.*,c.id as cardId,o.id as obuId from etc_vehicleinfo v
LEFT JOIN etc_cardinfo c on v.id = c.vehicleId and c.operation < 3 and c.`status` in (1,6)  -- 1-正常 6-卡挂失
LEFT JOIN etc_obuinfo o on v.id = o.vehicleId and o.operation < 3 and o.`status` in (1)  -- 1-正常
where v.id = ? and v.operation < 3
#end

##根据证件号和类型查询营改增用户信息
#sql("queryEtcUserByUserId")
SELECT  * from etc_userinfo
where  userIdType  = ? and  userIdNum = ? and operation < 3
#end

##根据证件号和类型查询线下监控用户信息
#sql("queryEtcUserWithOflUserByUserIdNumAndType")
SELECT  u.* from etc_userinfo u left join etc_ofl_userinfo ou on u.id = ou.userId
where  u.userIdType  = ? and  u.userIdNum = ? and u.operation < 3
#end
##根据证件号和类型查询和部门线下监控用户信息
#sql("queryEtcUserWithOflUserByUserIdNumAndTypeDepartment")
SELECT  u.* from etc_userinfo u inner join etc_ofl_userinfo ou on u.id = ou.userId
where  u.userIdType  = ? and  u.userIdNum = ? and u.department = ? and u.operation < 3
#end

#sql("queryOnlineOrderByVeh")
select * from (
SELECT  * from online_orders where  vehicleCode = ?   order by createTime  desc limit 1 ) t
where  orderStatus < 4 or orderStatus is null
#end
##根据车辆查询卡待注销数据
#sql("queryCardCancelByVeh")
select * from etc_card_cancel_confirm where vehicleId = ? and confirmStatus=0
 order by createTime desc
#end

##查询营改增车辆信息
#sql("queryEtcVehicleById")
select a.* ,
b.accountid,b.linkmobile,b.bankusername,b.certsn,b.posid,b.gentime,
b.trx_serno,b.employeeid,b.org_trx_serno,b.bankPost,b.acc_type,
b.cardType,b.usertype
from etc_vehicleinfo a
  LEFT JOIN etc_ofl_vehicleinfo b on a.id = b.vehicleId
 where a.id = ? and a.operation < 3
#end

##查询线下监控车辆信息
#sql("queryEtcOflVehicleById")
select a.* ,
b.accountid,b.linkmobile,b.bankusername,b.certsn,b.posid,b.gentime,
b.trx_serno,b.employeeid,b.org_trx_serno,b.bankPost,b.acc_type,
b.cardType,b.usertype
from etc_vehicleinfo a
  inner JOIN etc_ofl_vehicleinfo b on a.id = b.vehicleId
 where a.id = ? and a.operation < 3
#end

#sql("queryEtcObuByVeh")
select * from
( select * from  etc_obuinfo
 where vehicleId = ? and operation < 3 and status not in (2,3,4,5) order by createTime desc LIMIT 1  ) t
#end

#sql("queryEtcCardByVeh")
select * from
( select * from  etc_cardinfo where vehicleId = ? and operation < 3 and status not in (2,3,4,5)
order by createTime desc LIMIT 1  ) t
#end

#sql("queryEtcCardIssuseRcByVeh")
select b.businessType as cardBusinessType  from etc_cardinfo a
  LEFT JOIN etc_issued_record b on a.id = b.cardId
  where a.vehicleId = ? and b.businessType in (3,4,6)
  ORDER BY a.createTime desc,b.createTime desc  LIMIT 1
#end
#sql("queryEtcObuIssuseRcByVeh")
select b.businessType as obuBusinessType  from etc_obuinfo a
LEFT JOIN etc_issued_record b on a.id = b.obuId
where a.vehicleId = ? and b.businessType in (2,4,5)
ORDER BY a.createTime desc,b.createTime desc  LIMIT 1
#end
-- endregion ===================================线下开户检查 end=================================

-- ===================================卡发行检查接口 begin=================================
-- 查询易构发行信息
#sql("CheckYGCardInfoById")
    select id,cardType,brand,model,agencyId,userId,vehicleId,enableTime,expireTime,issuedType,channelId,issuedTime,
    status,statusChangeTime,flag as operation
  from TBL_USERCARD where id = #para(id)
#end
#sql("CheckYGCardInfoByVeh")
    select id,cardType,brand,model,agencyId,userId,vehicleId,enableTime,expireTime,issuedType,channelId,issuedTime,
    status,statusChangeTime,flag as operation
  from TBL_USERCARD where VEHICLEID = #para(id)  and  FLAG != 3 and status not in (4,5,2,3)
   and substr(CARDTYPE,0,1) != '2'
#end

-- 查询汇聚平台发行信息
#sql("CheckHJPTCardInfoById")
   select id,cardType,brand,model,agencyId,userId,vehicleId,enableTime,expireTime,issuedType,channelId,issuedTime,
    status,statusChangeTime,operation from
  (select * from cardupload
   where id = #para(id)   order by csntfileName desc LIMIT 1  ) t
   where operation <> 3 and status not in (4,5,2,3)
#end

#sql("CheckHJPTCardInfoByVeh")
  select id,cardType,brand,model,agencyId,userId,vehicleId,enableTime,expireTime,issuedType,channelId,issuedTime,
    status,statusChangeTime,operation from
    (select * from cardupload
where VEHICLEID = #para(id)   order by csntfileName desc LIMIT 1  ) t
 where operation <> 3 and status not in (4,5,2,3)
#end

-- 查询issuer_center信息
#sql("CheckCenterCardInfoById")
   select id,cardType,brand,model,agencyId,userId,vehicleId,enableTime,expireTime,issuedType,channelId,issuedTime,
    status,statusChangeTime,operation
  from etc_cardinfo where id = ?
#end

#sql("CheckCenterCardInfoByVeh")
   select id,cardType,brand,model,agencyId,userId,vehicleId,enableTime,expireTime,issuedType,channelId,issuedTime,
    status,statusChangeTime,operation
  from etc_cardinfo where VEHICLEID = ?  and   operation != 3 and status not in (4,5,2,3)
#end

#根据车辆查询OBUid
#sql("queryOBUidByVeh")
   select id from etc_obuinfo where VEHICLEID = ?  and operation != 3 and status not in (4,5,2,3)
#end
-- ===================================./卡发行检查接口 end=================================
-- ===================================Obu发行检查接口 begin=================================
-- 查询易构发行信息
#sql("CheckYGObuInfoById")
    select *
  from TBL_OBU where id = #para(id)
#end
#sql("CheckYGObuInfoByVeh")
    select *
  from TBL_OBU where VEHICLEID = #para(id)  and  FLAG != 3 and status not in (4,5,2,3)
#end

-- 查询汇聚平台发行信息
#sql("CheckHJPTObuInfoById")
 select * from (
 select * from obuupload
 where id = #para(id)  order by csntfileName desc LIMIT 1  ) t
where operation != 3 and status not in (4,5,2,3)
#end

#sql("CheckHJPTObuInfoByVeh")
    select * from (
 select * from obuupload
 where vehicleId = #para(id) order by csntfileName desc LIMIT 1  ) t
where operation != 3 and status not in (4,5,2,3)
#end

-- 查询issuer_center信息
#sql("CheckCenterObuInfoById")
   select *
  from etc_obuinfo where id = ?
#end

#sql("CheckCenterObuInfoByVeh")
   select *
  from etc_obuinfo where vehicleId = ?  and   operation != 3 and status not in (4,5,2,3)
#end

-- ===================================./Obu发行检查接口 end=================================
-- ===================================线下客户列表查询 begin=================================
#sql("QueryOfflineListUserInfo")
select id,userType,userName,userIdType,userIdNum,
(select openid from etc_ofl_userinfo where etc_ofl_userinfo.userid = etc_userinfo.id ) as openid,
tel,address,registeredType,channelId,registeredTime,department,
agentName,agentIdType,agentIdNum ,bank,bankAddr,bankAccount,taxpayerCode,status,
statusChangeTime,faceFeatureVersion,faceFeatureCode,operation,
(select max(name) from sys_positioninfo where code = etc_userinfo.channelId) as channelDesc,
   (SELECT NAME FROM sys_dictionaryinfo  WHERE code=etc_userinfo.userType
  AND parentId=(select id from Sys_DictionaryInfo where code='userType') LIMIT 1) AS userTypeDesc,
   (SELECT NAME FROM sys_dictionaryinfo  WHERE code=etc_userinfo.userIdType
  AND parentId=(select id from Sys_DictionaryInfo where code='user_id_type') LIMIT 1) AS userIdTypeDesc,
   (SELECT NAME FROM sys_dictionaryinfo  WHERE code=etc_userinfo.registeredType
  AND parentId=(select id from Sys_DictionaryInfo where code='registeredType') LIMIT 1) AS registeredTypeDesc,
   (SELECT NAME FROM sys_dictionaryinfo  WHERE code=etc_userinfo.agentIdType
  AND parentId=(select id from Sys_DictionaryInfo where code='user_id_type') LIMIT 1) AS agentIdTypeDesc,
   (SELECT NAME FROM sys_dictionaryinfo  WHERE code=etc_userinfo.status
  AND parentId=(select id from Sys_DictionaryInfo where code='userStatus') LIMIT 1) AS userStatusDesc,
  operatorId
 from etc_userinfo
 WHERE   #(condition)   and operation < 3  order by createTime desc
    limit  #(limitsize)
#end
#sql("QueryOfflineListUserInfoCt")
select count(1) as count
 from etc_userinfo
 WHERE   #(condition)    and operation < 3
#end

#sql("QueryOfflineListUserInfoAll")
select id,userType,userName,userIdType,userIdNum,
(select openid from etc_ofl_userinfo where etc_ofl_userinfo.userid = etc_userinfo.id ) as openid,
tel,address,registeredType,channelId,registeredTime,department,
agentName,agentIdType,agentIdNum ,bank,bankAddr,bankAccount,taxpayerCode,status,
statusChangeTime,faceFeatureVersion,faceFeatureCode,operation,
(select max(name) from sys_positioninfo where code = etc_userinfo.channelId) as channelDesc,
   (SELECT NAME FROM sys_dictionaryinfo  WHERE code=etc_userinfo.userType
  AND parentId=(select id from Sys_DictionaryInfo where code='userType') LIMIT 1) AS userTypeDesc,
   (SELECT NAME FROM sys_dictionaryinfo  WHERE code=etc_userinfo.userIdType
  AND parentId=(select id from Sys_DictionaryInfo where code='user_id_type') LIMIT 1) AS userIdTypeDesc,
   (SELECT NAME FROM sys_dictionaryinfo  WHERE code=etc_userinfo.registeredType
  AND parentId=(select id from Sys_DictionaryInfo where code='registeredType') LIMIT 1) AS registeredTypeDesc,
   (SELECT NAME FROM sys_dictionaryinfo  WHERE code=etc_userinfo.agentIdType
  AND parentId=(select id from Sys_DictionaryInfo where code='user_id_type') LIMIT 1) AS agentIdTypeDesc,
   (SELECT NAME FROM sys_dictionaryinfo  WHERE code=etc_userinfo.status
  AND parentId=(select id from Sys_DictionaryInfo where code='userStatus') LIMIT 1) AS userStatusDesc,
  operatorId
 from etc_userinfo
 WHERE   #(condition)   and operation < 3  order by createTime desc
    limit  #(limitsize)
#end


#sql("QueryOfflineListUserInfoCtAll")
select count(1) as count
 from etc_userinfo
 WHERE   #(condition)    and operation < 3
#end

-- ===================================线下客户列表查询 end=================================

-- ===================================线下渠道用户查询 begin=================================
#根据证件号和证件类型查询线下渠道用户信息
#sql("queryEtcOflUserByUserIdNumAndType")
SELECT  * from etc_ofl_userinfo
where  userIdNum = ? and userIdType = ?
#end
#sql("queryEtcOflUserByUserId")
SELECT  * from etc_ofl_userinfo
where  userId = ?
#end
-- ===================================./线下渠道用户查询 end=================================
-- ===================================线下渠道车辆查询 begin=================================
#根据vehicleId查询为在线下渠道注册的车辆信息
#sql("queryRegistedEtcOflVehicleByVehicleId")
SELECT  * from etc_ofl_vehicleinfo
where vehicleId = ? and depVehicleId is not null
#end
-- ===================================./线下渠道车辆查询 end=================================

-- ===================================Obu状态变更接口 begin=================================
#根据obu编码查询营改增obu信息
#sql("queryEtcObuInfoById")
SELECT  *  from etc_obuinfo
where  id = ? and operation < 3
#end
#根据obuid,type,status查询obu黑名单信息
#sql("queryEtcObuBlacklistByIdAndTypeAndStatus")
SELECT  issuerId,creationTime,type,OBUId,status from etc_obublacklist
where  obuId = ? and type = ? and status = ?
order by creationTime  desc
#end
-- ===================================./Obu发行检查接口 end=================================

-- ===================================card状态变更接口 begin=================================
#根据card编码查询营改增卡信息
#sql("queryEtcCardInfoById")
SELECT  * from etc_cardinfo
   where  id = ? and operation < 3
#end

-- ===================================./card状态变更接口 end=================================
-- ===================================挂起接口 begin=================================
#根据vheicleId and userId查询营改增卡信息
#sql("queryEtcCardInfoByVehicleIdAndUserId")
SELECT  * from (
 select * from etc_cardinfo where vehicleId = ? order by createTime desc limit 1
) t where  t.operation < 3
#end
#根据vheicleId and userId查询营改增OBU信息
#sql("queryEtcOBUInfoByVehicleIdAndUserId")
SELECT  * from (
 select * from etc_obuinfo where vehicleId = ? order by createTime desc limit 1
) t
where  t.operation < 3
#end
#根据cardid,type,status查询卡黑名单信息
#sql("queryEtcCardBlacklistByIdAndTypeAndStatus")
SELECT  issuerId,creationTime,type,cardId,status from etc_cardblacklist
where  cardId = ? and type = ? and status = ?
order by creationTime desc
#end
-- ===================================./挂起接口 end=================================
-- ===================================卡注销接口 begin=================================
#根据vheicleId查询线下渠道车辆信息
#sql("queryEtcOflVehicleByVehicleId")
SELECT v.vehicleId,v.userId,v.accountId,v.linkMobile,v.bankUserName,v.certsn,v.posId,v.genTime,v.acc_type,v.bankPost
 from etc_ofl_vehicleinfo v
where v.vehicleId = ?
#end
-- ===================================./卡注销接口 end=================================

-- ===================================卡信息接收接口 begin=================================
#查询卡信息
#sql("queryCardInfoBycardId")
  select * from etc_cardinfo where id =?
#end

#sql("queryOflUserByUserId")
  select * from etc_ofl_userinfo  where userId =?
#end

#sql("queryOflVehInfoByVehId")
  select * from etc_ofl_vehicleinfo where vehicleid =?
#end

#sql("queryIssuedRcByUserAndVehAndBusinessTyp")
   SELECT * from etc_issued_record
     where vehicleId = ? and userId = ? and businessType = ?
     ORDER BY createTime desc
#end
-- ===================================./卡信息接收接口 end=================================
-- ===================================obu信息接收接口 begin=================================
#查询OBU信息
#sql("queryObuInfoByObuId")
  select * from etc_obuinfo where id =?
#end

-- ===================================./obu信息接收接口 end=================================
-- ===================================用户信息查询接口 begin=================================
#sql("queryUserInfo")
  select id,userType,userName,userIdType,userIdNum,tel,address,registeredType,channelId,registeredTime,department,
   agentName,agentIdType,agentIdNum,bank,bankAddr,bankAccount,taxpayerCode,status,statusChangeTime,faceFeatureVersion,
   faceFeatureCode,operation from etc_userinfo where userIdType = ? and userIdNum = ?
#end

-- ===================================用户信息查询接口 end=================================
-- ===================================车辆列表查询 begin=================================
#sql("queryOfflineCarList")
select ev.id,ev.type,ev.userId,ev.ownerName,ev.ownerIdType,ev.ownerIdNum,ev.ownerTel,ev.address,
 eu.tel,ifnull(ev.vehUserType,0) as vehUserType,
 IFNULL(eu.userIdType,etp.userIdType) as userIdType ,
IFNULL(eu.userIdNum,etp.userIdNum) as userIdNum ,
 (select openid from etc_ofl_userinfo where etc_ofl_userinfo.userid =   ev.userId) as openid,
 ev.contact,ev.registeredType,ev.channelId,ev.registeredTime,ev.vehicleType,ev.vehicleModel,ev.useCharacter,ev.VIN,
 ev.engineNum,ev.registerDate,ev.issueDate,ev.fileNum,ev.approvedCount,ev.totalMass,ev.maintenanceMass,ev.permittedWeight,
 ev.outsideDimensions,ev.permittedTowWeight,ev.testRecord,ev.wheelCount,ev.axleCount,ev.axleDistance,ev.axisType,
 ev.vehicleFeatureVersion,ev.vehicleFeatureCode,ev.payAccountNum,ev.operation,substring_index(ev.id,'_',1) as vehiclePlate,
 (SELECT NAME FROM sys_dictionaryinfo  WHERE code=substring_index(ev.id,'_',-1) AND
 parentId=(select id from Sys_DictionaryInfo where code='VehicleColor') LIMIT 1) AS vehicleColorDesc,
 (SELECT NAME FROM sys_dictionaryinfo  WHERE code=ev.type AND
 parentId=(select id from Sys_DictionaryInfo where code='vehicleType') LIMIT 1) AS typeDesc,
 (SELECT NAME FROM sys_dictionaryinfo  WHERE code=ev.ownerIdType AND
 parentId=(select id from Sys_DictionaryInfo where code='userIdType') LIMIT 1) AS ownerIdTypeDesc,
  (SELECT NAME FROM sys_dictionaryinfo  WHERE code=eu.userIdType AND
 parentId=(select id from Sys_DictionaryInfo where code='userIdType') LIMIT 1) AS userIdTypeDesc,
 (SELECT NAME FROM sys_dictionaryinfo  WHERE code=ev.registeredType AND
 parentId=(select id from Sys_DictionaryInfo where code='registeredType') LIMIT 1) AS registeredTypeDesc,
 (SELECT NAME FROM sys_dictionaryinfo  WHERE code=ev.useCharacter AND
 parentId=(select id from Sys_DictionaryInfo where code='use_character') LIMIT 1) AS useCharacterDesc,
 (SELECT max(name) FROM sys_positioninfo  WHERE code=ev.channelId  LIMIT 1) AS channelDesc,
	ov.accountid,ov.linkmobile,ov.bankusername,ov.certsn,ov.posid,ov.gentime,ov.trx_serno,ov.employeeid,
	ov.org_trx_serno,ov.acc_type,ov.bankPost,ov.usertype,ov.protocolnumber,ov.cardType,ev.operatorId
 from etc_vehicleinfo ev
 left join etc_userinfo eu on eu.id = ev.userId
 left join etc_userinfo_temp etp on etp.id = ev.userId
 left join etc_ofl_vehicleinfo ov on ov.vehicleId = ev.id
WHERE #(condition) and ev.operation < 3 and ev.channelType in ('010001' ,'020001','050101') order by ev.createTime desc
    limit  #(limitsize)
#end
#sql("queryOfflineCarSize")
select count(1) as count from etc_vehicleinfo ev
 left join etc_userinfo eu on eu.id = ev.userId
WHERE #(condition) and ev.operation < 3 and ev.channelType in ('010001' ,'020001','050101')
#end
-- ===================================车辆列表查询 end=================================
-- ===================================卡列表查询接口 begin=================================
#sql("queryOfflineCardList")
   select ec.id,ec.cardType,ec.brand,ec.model,ec.agencyId,ec.userId,ec.vehicleId,ec.enableTime,ec.expireTime,
   eu.userType,ec.channelId,
  ec.issuedType,ec.issuedTime,
(case when ecan.cardid is not null then 12 else ec.status  end) as status,
ec.statusChangeTime,ec.operation,
  ifnull((select businessType from etc_issued_record rc where
             rc.cardId = ec.id and rc.userId = ec.userId and rc.vehicleId = ec.vehicleId
             order by rc.createtime desc  limit 1),1 )as businessType,
  case (select businessType from etc_issued_record rc where
             rc.cardId = ec.id and rc.userId = ec.userId and rc.vehicleId = ec.vehicleId
             order by rc.createtime desc  limit 1)
  when 2 THEN "已更换"
  when 3 THEN "已更换"
  when 4 THEN "已更换"
  when 5 THEN "已补办"
  when 6 THEN "已补办"
  when 7 THEN "已挂起"
  when 8 THEN "已解挂"
  when 9 THEN "已核销"
  when 10 THEN "已挂失"
  when 11 THEN "已解挂失"
  when 20 THEN "已挂起"
  when 21 THEN "已挂起"
  when 22 THEN "已核销"
  when 23 THEN "已核销"
  when 24 THEN "已解挂"
  when 25 THEN "已解挂"
  when 26 THEN "已挂失"
  when 27 THEN "已挂失"
  when 28 THEN "已解挂失"
  when 29 THEN "已解挂失"
  else "新办"
  END "businessTypeDesc",
  (select reason from etc_issued_record rc where
             rc.cardId = ec.id and rc.userId = ec.userId and rc.vehicleId = ec.vehicleId
             order by rc.createtime desc  limit 1) as reason,
 concat((SELECT NAME FROM sys_dictionaryinfo  WHERE code=substr(ec.cardType,1,1) AND
  parentId=(select id from Sys_DictionaryInfo where code='cardType_1') LIMIT 1),',',
 (SELECT NAME FROM sys_dictionaryinfo  WHERE code=substr(ec.cardType,2,1) AND
  parentId=(select id from Sys_DictionaryInfo where code='cardType_2') LIMIT 1),',',
 (SELECT NAME FROM sys_dictionaryinfo  WHERE code=substr(ec.cardType,3,1) AND
  parentId=(select id from Sys_DictionaryInfo where code='cardType_3') LIMIT 1)) AS cardTypeDesc,
 (SELECT NAME FROM sys_dictionaryinfo  WHERE code=substr(ec.userType,1,1) AND
  parentId=(select id from Sys_DictionaryInfo where code='userType') LIMIT 1) as cardAccountTypeDesc,
 (SELECT NAME FROM sys_dictionaryinfo  WHERE code=ec.brand AND
  parentId=(select id from Sys_DictionaryInfo where code='brand') LIMIT 1) as brandDesc,
 (SELECT NAME FROM sys_dictionaryinfo  WHERE code=ec.issuedType AND
  parentId=(select id from Sys_DictionaryInfo where code='issuedType') LIMIT 1) as issuedTypeDesc,

(case when ecan.cardid is not null then '预注销' else
 (SELECT NAME FROM sys_dictionaryinfo  WHERE code=ec.status AND
  parentId=(select id from Sys_DictionaryInfo where code='status') LIMIT 1)  end) as statusDesc,


substring_index(ec.vehicleId,'_',1) as vehiclePlate,
  (SELECT NAME FROM sys_dictionaryinfo  WHERE code=substring_index(ec.vehicleId,'_',-1) AND
 parentId=(select id from Sys_DictionaryInfo where code='VehicleColor') LIMIT 1) AS vehicleColorDesc,
 (SELECT max(name) FROM sys_positioninfo  WHERE code=ec.channelId  LIMIT 1) AS channelDesc,
   (SELECT NAME FROM sys_dictionaryinfo  WHERE code=eu.userType AND
 parentId=(select id from Sys_DictionaryInfo where code='userType') LIMIT 1) AS userTypeDesc,
 IFNULL(eu.userName,etp.userName) as userName ,
IFNULL(eu.userIdType,etp.userIdType) as userIdType ,
IFNULL(eu.userIdNum,etp.userIdNum) as userIdNum ,
 case when ocp.cardId is not null then 1 else 0 end as isUploadDiscount,
case when ocp.cardId is not null then '是' else '否' end as isUploadDiscountDesc,ec.operatorId
  from etc_cardinfo ec
  left join etc_userinfo eu on eu.id = ec.userId
  left join etc_userinfo_temp etp on etp.id = ec.userId
  left join  (select  DISTINCT cardid from etc_card_cancel_confirm where confirmStatus = 0) ecan on ecan.cardid = ec.id
	LEFT JOIN tbl_officecardparamdic ocp on CONCAT(ocp.cardnetid,ocp.cardid)=ec.id
WHERE #(condition)   and ec.channelType in ('010001' ,'020001','050101') and left(ec.cardType,1) != 2
      and ec.operation < 3
  order by ec.createTime desc
    limit #(limitsize)
#end
#sql("queryOfflineCardSize")
  select count(1) as count from etc_cardinfo ec
  left join etc_userinfo eu on eu.id = ec.userId
  left join etc_userinfo_temp etp on etp.id = ec.userId
  left join (select businessType,cardId,userId,vehicleId from etc_issued_record ORDER BY createTime desc limit 1) eir
  on eir.cardId = ec.id and eir.userId = ec.userId and eir.vehicleId = ec.vehicleId
 WHERE #(condition) and ec.channelType in ('010001' ,'020001','050101') and left(ec.cardType,1) != 2
       and ec.operation < 3
#end
-- ===================================卡列表查询接口全部渠道 begin=================================
#sql("queryAllCardList")
   select ec.id,ec.cardType,ec.brand,ec.model,ec.agencyId,ec.userId,ec.vehicleId,ec.enableTime,ec.expireTime,
   eu.userType,ec.channelId,
  ec.issuedType,ec.issuedTime,ec.status,ec.statusChangeTime,ec.operation,
  ifnull((select businessType from etc_issued_record rc where
             rc.cardId = ec.id and rc.userId = ec.userId and rc.vehicleId = ec.vehicleId
             order by rc.createtime desc  limit 1),1 )as businessType,
  case (select businessType from etc_issued_record rc where
             rc.cardId = ec.id and rc.userId = ec.userId and rc.vehicleId = ec.vehicleId
             order by rc.createtime desc  limit 1)
  when 2 THEN "已更换"
  when 3 THEN "已更换"
  when 4 THEN "已更换"
  when 5 THEN "已补办"
  when 6 THEN "已补办"
  when 7 THEN "已挂起"
  when 8 THEN "已解挂"
  when 9 THEN "已核销"
  when 10 THEN "已挂失"
  when 11 THEN "已解挂失"
  when 20 THEN "已挂起"
  when 21 THEN "已挂起"
  when 22 THEN "已核销"
  when 23 THEN "已核销"
  when 24 THEN "已解挂"
  when 25 THEN "已解挂"
  when 26 THEN "已挂失"
  when 27 THEN "已挂失"
  when 28 THEN "已解挂失"
  when 29 THEN "已解挂失"
  else "新办"
  END "businessTypeDesc",
  (select reason from etc_issued_record rc where
             rc.cardId = ec.id and rc.userId = ec.userId and rc.vehicleId = ec.vehicleId
             order by rc.createtime desc  limit 1) as reason,
 concat((SELECT NAME FROM sys_dictionaryinfo  WHERE code=substr(ec.cardType,1,1) AND
  parentId=(select id from Sys_DictionaryInfo where code='cardType_1') LIMIT 1),',',
 (SELECT NAME FROM sys_dictionaryinfo  WHERE code=substr(ec.cardType,2,1) AND
  parentId=(select id from Sys_DictionaryInfo where code='cardType_2') LIMIT 1),',',
 (SELECT NAME FROM sys_dictionaryinfo  WHERE code=substr(ec.cardType,3,1) AND
  parentId=(select id from Sys_DictionaryInfo where code='cardType_3') LIMIT 1)) AS cardTypeDesc,
 (SELECT NAME FROM sys_dictionaryinfo  WHERE code=substr(ec.userType,1,1) AND
  parentId=(select id from Sys_DictionaryInfo where code='userType') LIMIT 1) as cardAccountTypeDesc,
 (SELECT NAME FROM sys_dictionaryinfo  WHERE code=ec.brand AND
  parentId=(select id from Sys_DictionaryInfo where code='brand') LIMIT 1) as brandDesc,
 (SELECT NAME FROM sys_dictionaryinfo  WHERE code=ec.issuedType AND
  parentId=(select id from Sys_DictionaryInfo where code='issuedType') LIMIT 1) as issuedTypeDesc,
 (SELECT NAME FROM sys_dictionaryinfo  WHERE code=ec.status AND
  parentId=(select id from Sys_DictionaryInfo where code='status') LIMIT 1) as statusDesc,
substring_index(ec.vehicleId,'_',1) as vehiclePlate,
  (SELECT NAME FROM sys_dictionaryinfo  WHERE code=substring_index(ec.vehicleId,'_',-1) AND
 parentId=(select id from Sys_DictionaryInfo where code='VehicleColor') LIMIT 1) AS vehicleColorDesc,
 (SELECT max(name) FROM sys_positioninfo  WHERE code=ec.channelId  LIMIT 1) AS channelDesc,
   (SELECT NAME FROM sys_dictionaryinfo  WHERE code=eu.userType AND
 parentId=(select id from Sys_DictionaryInfo where code='userType') LIMIT 1) AS userTypeDesc,
  IFNULL(eu.userName,etp.userName) as userName ,
IFNULL(eu.userIdType,etp.userIdType) as userIdType ,
IFNULL(eu.userIdNum,etp.userIdNum) as userIdNum ,
 case when ocp.cardId is not null then 1 else 0 end as isUploadDiscount,
case when ocp.cardId is not null then '是' else '否' end as isUploadDiscountDesc,ec.operatorId
  from etc_cardinfo ec
  left join etc_userinfo eu on eu.id = ec.userId
  left join etc_userinfo_temp etp on etp.id = ec.userId
	LEFT JOIN tbl_officecardparamdic ocp on CONCAT(ocp.cardnetid,ocp.cardid)=ec.id
WHERE #(condition)      order by ec.createTime desc
    limit #(limitsize)
#end

#sql("queryAllCardSize")
  select count(1) as count from etc_cardinfo ec
  left join etc_userinfo eu on eu.id = ec.userId
  left join (select businessType,cardId,userId,vehicleId from etc_issued_record ORDER BY createTime desc limit 1) eir
  on eir.cardId = ec.id and eir.userId = ec.userId and eir.vehicleId = ec.vehicleId
 WHERE #(condition)
#end

#sql("queryUserIdBankPost")
 select max(bankpost) as bankpost  from sys_positioninfo  a
LEFT JOIN pos_user b on  a.code = b.POS_ID
where b.USER_ID =  ?
#end

-- ===================================银行卡绑定/解绑通知接口 begin=================================
#根据车辆编码查询线下渠道的用户信息
#sql("queryEtcOflUserInfoByVehicleId")
  select u.* from etc_ofl_userinfo u inner join etc_ofl_vehicleinfo v on u.userId = v.userId where v.vehicleId =?
#end


#sql("queryTblAgency")
  SELECT * FROM tbl_agency  where id = ?
#end
-- ===================================./银行卡绑定/解绑通知接口 end=================================
-- ===================================用户信息修改接口 begin=================================
#根据用户编码查询卡数量信息
#sql("queryEtcCardinfoCountByUserId")
  select count(1) as num from etc_cardinfo where userId = ? and operation < 3
#end
#根据用户编码查询OBU数量信息
#sql("queryEtcObuinfoCountByUserId")
  select count(1) as num from etc_obuinfo where userId = ? and operation < 3
#end
-- ===================================./用户信息修改接口 end=================================
-- ===================================车辆信息修改接口 begin=================================
#根据车辆编码查询卡数量信息
#sql("queryEtcCardinfoCountByVehicleId")
  select count(1) as num from etc_cardinfo where vehicleId = ? and operation < 3
#end
#sql("queryEtcVehinfoByVehicleId")
  select * from etc_vehicleinfo where id = ? and operation < 3
#end

#sql("queryEtcOflVehinfoByVehicleId")
  select * from etc_ofl_vehicleinfo where vehicleId = ?
#end

#根据车辆编码查询OBU数量信息
#sql("queryEtcObuinfoCountByVehicleId")
  select count(1) as num from etc_obuinfo where vehicleId = ? and operation < 3
#end

#sql("queryEtcVehinfoByObu")
  SELECT id FROM etc_obuinfo where vehicleId = ? and status in (1,6) and operation < 3
#end

#sql("queryCardIdIsActive")
  SELECT * FROM  etc_cardinfo where status in (1,6) and operation < 3
   and  vehicleId = ?
#end

#sql("queryEtcVehinfoBycard")
 select LEFT(bankpost,11) as bankpost from (
 SELECT * FROM etc_cardinfo
 where vehicleId = ?  ORDER BY issuedTime desc  limit 1) a
where left(a.cardtype,1) = 2 and LEFT(bankpost,11) != '63010199999'
#end
-- ===================================./车辆信息修改接口 end=================================

-- ===================================卡列表查询接口 end=================================
-- ===================================Obu列表查询接口 begin=================================
#sql("queryOfflineObuList")
  select eo.id,eo.brand,eo.model,eo.obuSign,eo.userId,eo.vehicleId,eo.enableTime,eo.expireTime,eo.registeredType,
  eu.userType,
  eo.registeredChannelId,eo.registeredTime,eo.installType,eo.installChannelId,eo.installTime,eo.status,
  eo.statusChangeTime,eo.operation,
     ifnull((select businessType from etc_issued_record rc where
             rc.obuId = eo.id and rc.userId = eo.userId and rc.vehicleId = eo.vehicleId
             order by rc.createtime desc  limit 1),1 )as businessType,
  case   (select businessType from etc_issued_record rc where
             rc.obuId = eo.id and rc.userId = eo.userId and rc.vehicleId = eo.vehicleId
             order by rc.createtime desc  limit 1)
  when 2 THEN "已更换"
  when 3 THEN "已更换"
  when 4 THEN "已更换"
  when 5 THEN "已补办"
  when 6 THEN "已补办"
  when 7 THEN "已挂起"
  when 8 THEN "已解挂"
  when 9 THEN "已核销"
  when 10 THEN "已挂失"
  when 11 THEN "已解挂失"
  when 20 THEN "已挂起"
  when 21 THEN "已挂起"
  when 22 THEN "已核销"
  when 23 THEN "已核销"
  when 24 THEN "已解挂"
  when 25 THEN "已解挂"
  when 26 THEN "已挂失"
  when 27 THEN "已挂失"
  when 28 THEN "已解挂失"
  when 29 THEN "已解挂失"
  else "新办"
  END "businessTypeDesc",
     (select reason from etc_issued_record rc where
             rc.obuId = eo.id and rc.userId = eo.userId and rc.vehicleId = eo.vehicleId
             order by rc.createtime desc  limit 1) as reason,
 (SELECT NAME FROM sys_dictionaryinfo  WHERE code=eo.brand AND
  parentId=(select id from Sys_DictionaryInfo where code='brandOBU') LIMIT 1) as brandDesc,
(SELECT NAME FROM sys_dictionaryinfo  WHERE code=eo.obuSign AND
  parentId=(select id from Sys_DictionaryInfo where code='obuSign') LIMIT 1) as obuSignDesc,
(SELECT NAME FROM sys_dictionaryinfo  WHERE code=eo.registeredType AND
  parentId=(select id from Sys_DictionaryInfo where code='registeredTypeOBU') LIMIT 1) as registeredTypeDesc,
(SELECT NAME FROM sys_dictionaryinfo  WHERE code=eo.installType AND
  parentId=(select id from Sys_DictionaryInfo where code='installType') LIMIT 1) as installTypeDesc,
 (SELECT NAME FROM sys_dictionaryinfo  WHERE code=eo.status AND
  parentId=(select id from Sys_DictionaryInfo where code='statusOBU') LIMIT 1) as statusDesc,
substring_index(eo.vehicleId,'_',1) as vehiclePlate,
  (SELECT NAME FROM sys_dictionaryinfo  WHERE code=substring_index(eo.vehicleId,'_',-1) AND
 parentId=(select id from Sys_DictionaryInfo where code='VehicleColor') LIMIT 1) AS vehicleColorDesc,
 (SELECT max(name) FROM sys_positioninfo  WHERE code=eo.installChannelId  LIMIT 1) AS channelDesc,
  (SELECT NAME FROM sys_dictionaryinfo  WHERE code=eu.userType AND
 parentId=(select id from Sys_DictionaryInfo where code='userType') LIMIT 1) AS userTypeDesc,
  IFNULL(eu.userName,etp.userName) as userName ,
IFNULL(eu.userIdType,etp.userIdType) as userIdType ,
IFNULL(eu.userIdNum,etp.userIdNum) as userIdNum ,
  eo.operatorId
  from etc_obuinfo eo left join etc_userinfo eu on eu.id = eo.userId
   left join etc_userinfo_temp etp on etp.id = eo.userId
WHERE #(condition) and eo.channelType in ('010001' ,'020001','050101') and  eo.operation < 3 order by eo.createTime desc
    limit  #(limitsize)
#end

#sql("queryOfflineObuSize")
  select count(1) as count  from etc_obuinfo eo left join etc_userinfo eu on eu.id = eo.userId
  left join (select businessType,cardId,userId,vehicleId from etc_issued_record ORDER BY createTime desc limit 1) eir
  on eir.cardId = eo.id and eir.userId = eo.userId and eir.vehicleId = eo.vehicleId
 WHERE #(condition) and eo.channelType in ('010001' ,'020001','050101')  and  eo.operation < 3
#end
-- ===================================Obu列表查询接口 end=================================
-- ===================================线下车辆接收信息接口 begin=================================
#sql("queryTblAgencyById")
 SELECT * FROM tbl_agency where  id = ?
#end
-- ===================================线下车辆接收信息接口 end=================================
-- ===================================客服申请单接收 begin=================================

#sql("queryCenterEtcUserByIdtypeNum")
 SELECT * FROM etc_userinfo where  userIdType = ? and userIdNum = ?
#end

-- ===================================客服申请单接收 end=================================

-- ===================================储值卡充值 begin=================================
##根据id查询储值卡充值流水
#sql("queryReChargeListById")
 SELECT * FROM etcts_storecard_recharge_list where  id = ?
#end

##根据pid查询储值卡充值流水条数
#sql("queryChargeBackCountBypId")
 SELECT count(1) as num FROM etcts_storecard_recharge_list where  pid = ? and status = 1 and businessType in (3,4)
#end

##查询储值卡充值流水列表
#sql("listReChargeListByCondition")
 SELECT id,pid,convert(paidAmount/100,decimal(15,2)) as paidAmount,
 convert(giftAmount/100,decimal(15,2)) as giftAmount,
 convert(rechargeAmount/100,decimal(15,2)) as rechargeAmount,
 convert(remainAmount/100,decimal(15,2)) as remainAmount,
 cardId,userName,userIdType,
 (SELECT NAME FROM sys_dictionaryinfo  WHERE code=userIdType AND
 parentId=(select id from Sys_DictionaryInfo where code='userIdType') LIMIT 1) AS userIdTypeDesc,
 userIdNum,plateNum,plateColor,
 (SELECT NAME FROM sys_dictionaryinfo  WHERE code=plateColor
     AND parentId=(select id from Sys_DictionaryInfo where code='VehicleColor') LIMIT 1) AS plateColorDesc,
     rechargeChannelType,channelId,
 businessType,
 case when businessType in (1,2) then '充值' when businessType in (3,4) then '充值冲正' end as businessTypeDesc,
 payType,
  (SELECT NAME FROM sys_dictionaryinfo  WHERE code=payType
     AND parentId=(select id from Sys_DictionaryInfo where code='storeCardPayType') LIMIT 1) AS payTypeDesc,
 opTime,orgId,operatorId,channelType,status
 FROM etcts_storecard_recharge_list where  #(condition) and status = 1 order by createTime desc
    limit #(limitsize)
#end
##查询储值卡充值流水列表总条数
#sql("listReChargeListByConditionSize")
  select count(1) as count,sum(rechargeAmount / 100) as rechargeAmountSum from etcts_storecard_recharge_list
 WHERE #(condition) and status = 1
#end

-- 查询issuer_center信息
#sql("queryCenterStoreCardInfoByVeh")
   select c.id,c.cardType,c.brand,c.model,c.agencyId,c.userId,c.vehicleId,c.enableTime,c.expireTime,c.issuedType,c.channelId,c.issuedTime,
    c.status,c.statusChangeTime,c.operation,u.userIdNum,u.userIdType,c.bankPost
  from etc_cardinfo c left JOIN etc_userinfo u on c.userId = u.id
   where #(condition)  and c.operation < 3 and  left(c.cardtype,1) = '2'
   order by c.status
#end

#sql("queryCenterEtcUserById")
 SELECT * FROM etc_userinfo where id =?
#end
-- ===================================储值卡充值 end=================================
-- ===================================根据车牌编号查询车辆发行相关信息 begin=================================
#根据车牌编号查询车辆发行相关信息
#sql("queryIssuerInfoByVehicleCode")
 SELECT veh.id,veh.userId,us.userName,us.userType,
 ( Select  name from Sys_DictionaryInfo
     where ParentId=(select id from Sys_DictionaryInfo where code='userType')
   and code  = us.userType) as userTypeDesc,
us.tel as mobile,us.userIdType,
 ( Select  name from Sys_DictionaryInfo
     where ParentId=(select id from Sys_DictionaryInfo where code='user_id_type')
   and code  = us.userIdType) as userIdTypeDesc,
 us.userIdNum,veh.type,
 ( Select  name from Sys_DictionaryInfo
     where ParentId=(select id from Sys_DictionaryInfo where code='Charge_car_type')
   and code  = veh.type) as vehicleTypeDesc,
veh.outsideDimensions,veh.vin,veh.approvedCount,veh.totalMass,veh.engineNum,
veh.wheelCount,veh.axleCount,veh.axleDistance,veh.axisType,
obu.id as obuId,obu.status as obuStatus,
 ( Select  name from Sys_DictionaryInfo
     where ParentId=(select id from Sys_DictionaryInfo where code='statusOBU')
   and code  = obu.status) as obuStatusDesc,
   obu.enableTime,obu.isActive,obu.ActiveTime,
card.id as cardId,card.status as cardStatus,
 ( Select  name from Sys_DictionaryInfo
     where ParentId=(select id from Sys_DictionaryInfo where code='status')
   and code  = card.status) as cardStatusDesc ,card.issuedTime
 FROM  etc_vehicleinfo veh
LEFT JOIN etc_userinfo us on veh.userId = us.id
LEFT JOIN
( select * from etc_cardinfo where vehicleId = ?
  ORDER BY createTime desc limit 1) card on card.vehicleId =  veh.id
LEFT JOIN
( select * from etc_obuinfo where vehicleId = ?
  ORDER BY createTime desc limit 1) obu on obu.vehicleId =  veh.id
where veh.id = ?
#end

-- ===================================./根据车牌编号查询车辆发行相关信息 end=================================

-- =================================== 8820 车辆解除绑定 begin=================================
#sql("queryIssuInfoByVehicleId")
SELECT id FROM etc_obuinfo where vehicleId = ? and status in (1,6) and operation < 3
UNION
SELECT id FROM etc_cardinfo where vehicleId = ? and status in (1,6) and operation < 3
#end

#sql("queryEtcUserInfoByUserId")
  select * from etc_userinfo where id = ?
#end
#sql("queryEtcObuInfoByOBUId")
  select * from etc_obuinfo where id = ?
#end
#sql("queryEtcCardInfoByCardId")
  select * from etc_cardinfo where id = ?
#end

-- 获取CARD全量黑名单数量
#sql("countBlackCardId")
    SELECT COUNT(1) FROM basic_cardblacklistdown_validate
    WHERE cardId=? AND provinceCode=63
#end


-- =================================== 8821银行卡换卡检查 begin=================================
-- 获取车辆的正常ETC卡数量
#sql("countNomorlCardId")
    SELECT count(*) as num from etc_cardinfo
     where vehicleId = ? and status = 1
#end
-- 获取车辆的正常ETC卡
#sql("queryNomorlCardId")
    SELECT *  from etc_cardinfo
     where vehicleId = ? and status = 1
#end

-- =================================== 8940 快发手持图片接收 begin=================================
-- 根据车辆编号，用户编号判断图片是否接收，接收
#sql("queryPictureExist")
 SELECT * FROM onlinepicture where userid = ? and  CarNumber = ? and Calcolor = ? and businessType in (3,4)
#end
-- =================================== 8941 查询发行图片 begin=================================
#sql("queryIssuerPicture")
 SELECT * FROM onlinepicture where  CarNumber = ? and Calcolor = ?
 order by CreateTime desc
#end

#sql("queryCenterOrderIssuer")
select * from online_orders where left(channelType,3) = '041'  and vehicleCode = ?
order by createTime desc
limit 1
#end


-- =================================== 批量自动审核 begin=================================
#sql("queryAuditOnlineApply")
SELECT * FROM onlineapply where  (examineResult  is null or examineResult = 0)
limit 1000
#end