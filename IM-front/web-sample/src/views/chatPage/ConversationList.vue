<template>
  <div class="conversation-container">
    <div class="friend-item"  v-for="(f,i) in conversation.list" :key="f.toId" >
      <span @click="selectConversation(f)" :class="f.toId==$store.state.conversation.toId?'current':''">
        {{f.toId}}
      </span>
    </div>
    <div>
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
  },
  methods:{
    ...mapMutations(["setConversationToId"]),
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
