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
      <InfoContainer :listenerMap="listenerMap"/>
    </div>
  </div>
</template>

<script>
import InfoContainer from "./InfoContainer.vue";
import ConversationList from "./ConversationList.vue";
import FirendList from "./FirendList.vue";
import Chat from "./Chat.vue";
import Pubsub from "pubsub-js";
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
      listenerMap :{
        onSocketConnectEvent: (option, status, data) => {
          console.log("connect success:" + JSON.stringify(status));
          Pubsub.publish("socketConnected")
        },
        onSocketErrorEvent: (error) => {
          console.log("connect error:" + this.$store.state.infoForm.fromId);
        },
        onSocketReConnectEvent: () => {
          console.log("reconnecting:" );
        },
        onSocketCloseEvent: () => {
          console.log("connect close:" );
        },
        onSocketReConnectSuccessEvent: () => {
          console.log("reconnect success" );
        },
        onTestMessage: (e) => {
          console.log("onTestMessage ：" + e );
        },
        onP2PMessage: (e) => {
          // console.log("onP2PMessage ：" + e );
          e = JSON.parse(e)
          Pubsub.publish('P2PMessage', e.data);
        },
        onGroupMessage: (e) => {
          // console.log("GroupMessage ：" + e );
          e = JSON.parse(e)
          Pubsub.publish('GroupMessage', e.data);
        },
        onLogin: (uid) => {
          console.log("userid: "+uid+", login sdk success" );
        }
      }
    }
  },
  async created() {

    // this.$tim.init("http://127.0.0.1:8000/v1",res.data.appId,res.data.userId,res.data.imUserSign,listeners,function (sdk) {
    //   if (sdk) {
    //     console.log('sdk 成功连接的回调, 可以使用 sdk 请求数据了.');
    //     return uni.switchTab({
    //       url: "/pages/tabbar/index/index"
    //     })
    //   }
    // }
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
