import Vue from 'vue'
import Router from 'vue-router'
import ChatPage from '@/views/chatPage/index.vue'
import FriendPage from '@/views/friendPage/index.vue'

Vue.use(Router)

export default new Router({
  routes: [
    {
      path: '/',
      name: 'ChatPage',
      component: ChatPage
    },
    {
      path: '/friend',
      name: 'FriendPage',
      component: FriendPage
    }
  ]
})
