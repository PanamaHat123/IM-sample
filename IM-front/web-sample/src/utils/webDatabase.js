

export const initDB = (databaseName,version)=>{
  return new Promise((resolve,reject)=>{
    if(!window){
      console.log("not web env")
      reject("not web env")
      return
    }
    //  兼容浏览器
    let indexedDB =
      window.indexedDB ||
      window.mozIndexedDB ||
      window.webkitIndexedDB ||
      window.msIndexedDB;
    if(!indexedDB){
      console.log("no database engine")
      reject("no database engine")
      return
    }
    const request = indexedDB.open(databaseName,version);
    let db;
    request.onsuccess = function (event) {
      db = event.target.result; // 数据库对象
      console.log("db open success");
      resolve(db);
    };
    request.onerror = function (event) {
      console.log("db open failed");
      reject("db open failed")
    };
    // db create or update
    request.onupgradeneeded = function (event) {
      console.log("onupgradeneeded");
      db = event.target.result;
      let objectStore;

      objectStore = db.createObjectStore("conversationList", {
        keyPath: "conversationId",
        // autoIncrement: true
      });
      objectStore.createIndex("messageKey", "messageKey", { unique: false });
    };

  })


}
