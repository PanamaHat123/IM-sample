<template>
  <div class="firend-container">
    <div class="friend-item"  v-for="(f,i) in $store.state.friend.friendList" :key="f.toId" >
      <span @click="selectFriend(f.toId)" >
        {{f.toId}}
      </span>
    </div>
    <div>
      <el-button size="mini" @click="refresh">refresh friend</el-button>
    </div>
  </div>
</template>
<script>
import {mapState,mapMutations} from "vuex";
import Pubsub from "pubsub-js"
export default {
  props:{

  },
  data(){
    return {

    }
  },
  computed:{
  },
  async created() {
    console.log(this.$store)

  },
  methods:{
    refresh(){
    },
    selectFriend(toId){
      // this.setConversationToId(toId)
    }
  }
}
</script>


<style scoped>
.firend-container{
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
