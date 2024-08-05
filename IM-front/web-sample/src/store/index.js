import Vue from 'vue'
import Vuex from 'vuex'
import friend from "./modules/friend";
import infoForm from "./modules/infoForm";
import conversation from "./modules/conversation";
Vue.use(Vuex)


export default new Vuex.Store({
  modules:{
    friend:friend,
    infoForm:infoForm,
    conversation:conversation
  }
})
