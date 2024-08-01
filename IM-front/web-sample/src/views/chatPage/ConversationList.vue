<template>
  <div class="conversation-container">
    <div class="friend-item"  v-for="(f,i) in conversation.list" :key="f.toId" >
      <span @click="selectConversation(f)" :class="f.toId==$store.state.conversation.toId?'current':''">
        {{f.toId}}
      </span>
    </div>
    <div>
      ---------------------------------
<!--      <el-button size="mini" @click="re">refresh</el-button>-->
    </div>
  </div>
</template>


<script>
import Pubsub from "pubsub-js";
import {mapMutations,mapState} from "vuex";

export default {
  data(){
    return{

    }
  },
  computed:{
    ...mapState(["conversation"])
  },
  created() {
    this.register()
  },
  methods:{
    ...mapMutations(["setConversationList","setConversationToId"]),
    register(){
      Pubsub.subscribe("socketConnected",this.initLocalConversation)
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
    selectConversation(row){
      console.log("conversation",row)
      this.setConversationToId(row.toId)
    }
  }
}
</script>


<style scoped>
.conversation-container{
  width: 100%;
  /*height: 100%;*/
}
.friend-item{
  width: 100%;
  height: 40px;
  display: flex;
  align-items: center;
  padding-left: 10px;
  cursor: pointer;
  border-bottom: 1px rebeccapurple solid;
}
.current{
  color: red;
}
</style>
