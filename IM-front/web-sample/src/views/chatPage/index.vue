<template>
  <div class="chat-container">

    <div class="chat-box">
      <div class="left-side">
<!--        <ConversationList/>-->
        <FirendList/>
      </div>
      <div class="right-side">
        <Chat/>
      </div>
    </div>
    <div class="info-box">
      <InfoContainer/>
    </div>
  </div>
</template>

<script>
import {queryAllFriendShip} from "../../api/baseApi"
import InfoContainer from "./InfoContainer.vue";
import ConversationList from "./ConversationList.vue";
import FirendList from "./FirendList.vue";
import Chat from "./Chat.vue";
export default {
  name: 'ChatPage',
  components:{
    InfoContainer,
    ConversationList,
    FirendList,
    Chat
  },
  data () {
    return {
      msg: 'Welcome to Your Vue.js App',

    }
  },
  async created() {

  },
  methods:{

    async queryGroupList(){

    }
  }
}
</script>

<style scoped>
.chat-container{
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
}
.chat-box{
  width: 1200px;
  height: 800px;
  background: #42b983;
  display: flex;
}
.left-side{
  width: 30%;
  height: 100%;
  background: palegoldenrod;
}
.right-side{
  width: 70%;
  height: 100%;
  background: skyblue;
}
.info-box{
  width: 300px;
  height: 800px;
  background: cadetblue;
}
</style>
