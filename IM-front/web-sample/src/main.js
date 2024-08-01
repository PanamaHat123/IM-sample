// The Vue build version to load with the `import` command
// (runtime-only or standalone) has been set in webpack.base.conf with an alias.
import Vue from 'vue'
import App from './App'
import router from './router'
import ElementUI from 'element-ui';
import 'element-ui/lib/theme-chalk/index.css';
import imSdk from "./common/tim-sdk"
import Pubsub from "pubsub-js"
import {initDB} from "./utils/webDatabase"

import store from './store/index';

Vue.config.productionTip = false
Vue.use(ElementUI);
Vue.prototype.$tim = imSdk.im
window.im = imSdk.im
window.Pubsub = Pubsub

//init database
const db = initDB()
Vue.prototype.$db = db;
window.db = db;

window.vuex = store
/* eslint-disable no-new */
new Vue({
  el: '#app',
  router,
  store,
  components: { App },
  template: '<App/>',
})
