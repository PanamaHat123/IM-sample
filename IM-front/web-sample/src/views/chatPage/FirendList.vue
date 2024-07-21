<template>
  <div class="firend-container">
    <div class="friend-item" v-for="(f,i) in firendList" :key="f.toId">
      {{f.toId}}
    </div>
    {{infoForm.imei}}
  </div>
</template>


<script>
import {queryAllFriendShip} from "../../api/baseApi";
import {mapState} from "vuex";

export default {
  props:{

  },
  data(){
    return {
      firendList:[],
    }
  },
  computed:{
    ...mapState(["infoForm"])
  },
  async created() {
    console.log(this.$store)
    this.firendList = await this.getFriendList()
  },
  methods:{
    async getFriendList(){
      let data = {
        fromId:"app01"
      }
      try {
        let res = await queryAllFriendShip(10000, data)
        if(res.code != 200){
          throw new Error(res.msg)
        }
        return res.data
      } catch (e) {
        this.$message.error(e.msg || "get friend failed")
      }
    },
  }
}
</script>


<style>
.firend-container{
  width: 100%;
  height: 100%;
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
</style>
