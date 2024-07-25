// The Vue build version to load with the `import` command
// (runtime-only or standalone) has been set in webpack.base.conf with an alias.
import Vue from 'vue'
import App from './App'
import router from './router'
import ElementUI from 'element-ui';
import 'element-ui/lib/theme-chalk/index.css';
import imSdk from "./common/im-sdk"
import Pubsub from "pubsub-js"
import {initDB} from "./utils/webDatabase"

import store from './store/index';

Vue.config.productionTip = false
Vue.use(ElementUI);
Vue.prototype.$tim = imSdk.im
window.Pubsub = Pubsub

//init database
initDB("im-store","1").then(db=>{
  Vue.prototype.$db = db;
  window.db = db;
})

console.log("$tim",imSdk)
window.vuex = store
/* eslint-disable no-new */
new Vue({
  el: '#app',
  router,
  store,
  components: { App },
  template: '<App/>',
})
