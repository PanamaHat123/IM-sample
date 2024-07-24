import Vue from 'vue'
import Vuex from 'vuex'
Vue.use(Vuex)

// 用来存储数据
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
// 响应组件中的事件
const actions = {

}
// 操作数据
const mutations = {
  setInfoForm(state, infoForm){
    state.infoForm = infoForm
  },
  setConversationToId(state,toId){
    state.conversation.toId = toId
  }
}
// 用来将state数据进行加工
const getters = {

}
// 新建并暴露store
export default new Vuex.Store({
  state,
  actions,
  mutations,
  getters
})
