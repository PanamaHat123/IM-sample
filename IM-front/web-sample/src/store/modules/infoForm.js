


export default {
  namespaced:true,
  state:{
    fromId: "app01",
    appId:10000,
    clientType:1, //web
    imei:1
  },
  mutations:{
    setInfoForm(state, infoForm){
      state.fromId = infoForm.fromId
      state.appId = infoForm.appId
      state.clientType = infoForm.clientType
      state.imei = infoForm.imei
    },
  },
  actions:{

  }

}
