import Dexie from 'dexie';

export const initDB = ()=>{
  let db = new Dexie('im-store');
  db.version(1).stores({
    conversationList: '++conversationId, conversationType, fromId,toId,isMute,isTop,sequence,readedSequence,appId',
    userInfo:"appId,userId,clientType,imei",
    records:"++messageKey,messageBody,appId,fromId,toId,imei,clientType,extra,delFlag,messageSequence,messageId,conversationType,conversationId"
  });
  return db
}

