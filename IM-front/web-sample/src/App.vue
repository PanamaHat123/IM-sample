<template>
  <div id="app">
    <div class="container">
      <div class="navigator-side">
        <div>
          <router-link to="/">
            <div class="nav-item">
              <i class="el-icon-chat-dot-square" ></i>
            </div>
          </router-link>
          <router-link to="/friend">
            <div class="nav-item">
              <i class="el-icon-user" ></i>
            </div>
          </router-link>
        </div>
      </div>
      <div class="router-container">
        <router-view />
      </div>
      <div class="info-box">
        <InfoContainer :listenerMap="listenerMap"/>
      </div>
    </div>
  </div>
</template>

<script>
import InfoContainer from "./views/InfoContainer.vue";
import Pubsub from "pubsub-js";
import {mapMutations} from "vuex";
import {appCreatedInit} from "./appInit"
import friend from "./store/modules/friend";
export default {
  name: 'App',
  components:{
    InfoContainer
  },
  data(){
    return{
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
  created() {
    appCreatedInit()
    Pubsub.subscribe("socketConnected",(name,params)=>{
      this.init()
    })
  },
  methods:{
    ...mapMutations("friend",["setFriendList"]),
    ...mapMutations("conversation",["setConversationList","addRecord"]),
    async init(){
      await this.initLocalConversation()
      let friendList = await this.getFriendList()
      this.setFriendList(friendList)
      await  this.getRecords()
    },

    async getFriendList(){
      try {
        let res = await this.$tim.getAllFriend()
        if(res.code != 200){
          throw new Error(res.msg)
        }
        return res.data
      } catch (e) {
        this.$message.error(e.msg || "get friend failed")
      }
    },
    async initLocalConversation(){
      let [localLoginUser]  =  await this.$db.userInfo.toArray()
      console.log("localLoginUser",localLoginUser)
      let localConversation =  await this.$db.conversationList.toArray();
      if(!localLoginUser||(""+localLoginUser.userId+localLoginUser.userId.appId !== ""+this.$store.state.infoForm.fromId+this.$store.state.infoForm.appId)
        || localConversation.length === 0){
        //first login this endpoint
        // fully pull latest 100
        let onlineConversation = await this.getConversation(100,0)
        this.setConversationList(onlineConversation);
        await this.$db.conversationList.clear()
        await this.$db.conversationList.bulkAdd(onlineConversation)
        console.log("first sync conversation")
      }else{
        // just use local conversation
        this.setConversationList(localConversation);
      }
      await this.$db.userInfo.clear();
      await this.$db.userInfo.add({
        ...this.$store.state.infoForm,
        userId:this.$store.state.infoForm.fromId
      })
    },
    async getConversation(maxLimit,lastSequence){
      let res =  await this.$tim.getConversation({
        maxLimit,
        lastSequence
      })
      console.log("getConversation==>",res)
      return res.data.dataList
    },
    async getRecords(){
      for (const item of this.$store.state.conversation.list) {
        let records =await this.$db.records.where("conversationId").equals(item.conversationId).limit(40).toArray();
        for (const msg of records) {
          this.addRecord({
            conversationId:msg.conversationId,
            message:msg,
          })
        }
      }

    }
  }
}
</script>

<style>
html,body{
  width: 100%;
  height: 100%;
  margin: 0;
  padding: 0;
}
#app{
  width: 90%;
  height: 90%;
}
.container{
  display: flex;
  width: 1250px;
  height: 800px;
}
.router-container{
  flex: 1;
  height: 100%;
}

.navigator-side{
  width: 50px;
  height:  100%;
  display: flex;
  flex-direction: column;
  background: #42b983;
}
.info-box{
  width: 300px;
  height: 800px;
  background: cadetblue;
}
.nav-item{
  width: 100%;
  height: 50px;
  font-size: 28px;
  display: flex;
  justify-content: center;
  align-items: center;
}
</style>
