import Vue from 'vue'
import Router from 'vue-router'
import ChatPage from '@/views/chatPage/index.vue'

Vue.use(Router)

export default new Router({
  routes: [
    {
      path: '/',
      name: 'ChatPage',
      component: ChatPage
    }
  ]
})
