<template>
  <div class="firend-page">
    <div class="left-side">
      <FriendList/>
    </div>
    <div class="right-side">
      friendInfo
    </div>
  </div>
</template>

<script>
import FriendList from "./FriendList";

export default {
  components:{
    FriendList
  },
  data(){
    return{

    }
  }
}
</script>


<style>
.firend-page{
  width: 100%;
  height: 100%;
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
</style>
