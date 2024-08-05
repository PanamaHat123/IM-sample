

export default {
  namespaced:true,
  state:{
    current:{}, //current conversation entity
    list:[],
    records:{}
  },
  mutations:{
    setConversation(state, conversation){
      state.current = conversation
      state.list =  state.list.filter((item)=>item.conversationId !== conversation.conversationId)
      state.list.unshift(conversation)
    },
    setConversationList(state,list){
      state.list = list
    },
    addRecord(state, {conversationId, message}){
      if(state.records[conversationId] ){
        state.records[conversationId].push(message)
        //todo
        //limit record
      }else{
        state.records[conversationId] = [message]
      }
    }
  },
  actions:{

  }

}
