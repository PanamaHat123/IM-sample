// The Vue build version to load with the `import` command
// (runtime-only or standalone) has been set in webpack.base.conf with an alias.
import Vue from 'vue'
import App from './App'
import router from './router'
import ElementUI from 'element-ui';
import 'element-ui/lib/theme-chalk/index.css';
import imSdk from "./common/im-sdk"

import store from './store/index';

Vue.config.productionTip = false
Vue.use(ElementUI);
Vue.prototype.$tim = imSdk
window.vuex = store
/* eslint-disable no-new */
new Vue({
  el: '#app',
  router,
  store,
  components: { App },
  template: '<App/>',
})
// new Vue({
//   render: h => h(App),
//   store
// }).$mount('#app')
