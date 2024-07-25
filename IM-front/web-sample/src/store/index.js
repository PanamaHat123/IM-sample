import Vue from 'vue'
import Vuex from 'vuex'
Vue.use(Vuex)

const state = {
  test:"okk",
  infoForm:{
    fromId: "app01",
    appId:10000,
    clientType:1, //web
    imei:1
  },
  conversation:{
    toId:""
  }
}
const actions = {

}
const mutations = {
  setInfoForm(state, infoForm){
    state.infoForm = infoForm
  },
  setConversationToId(state,toId){
    state.conversation.toId = toId
  }
}
const getters = {

}
export default new Vuex.Store({
  state,
  actions,
  mutations,
  getters
})
