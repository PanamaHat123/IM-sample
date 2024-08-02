<template>
  <div class="chat-container">

    <div class="chat-box">
      <div class="left-side">
        <ConversationList/>
      </div>
      <div class="right-side">
        <Chat/>
      </div>
    </div>

  </div>
</template>

<script>
import ConversationList from "./ConversationList.vue";
import Chat from "./Chat.vue";

export default {
  name: 'ChatPage',
  components:{
    ConversationList,
    Chat
  },
  data () {
    return {
      msg: 'Welcome to Your Vue.js App',

    }
  },
  async created() {
    this.init();
    this.register();
  },
  methods:{

    async queryGroupList(){

    },
    async init(){

    },
    register(){
    },

  },

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
  width: 100%;
  height: 100%;
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
