<template>
  <div class="wrap">
    <div class="wrap-container-direct">
      <div class="wrap-direct">
        <p class="back-btn" @click="goDM">〈 </p>
        <div class="in-img" @click="goProfile">
          <img v-if="!profileImg" src="../assets/images/default-user.png" alt="" class="in-img-profile">
          <img v-if="profileImg" :src="profileImg" alt="" class="in-img-profile">
        </div>
        <p class="back-btn-name" @click="goProfile">{{ othername }}</p>
      </div>
      <div class="message-content-wrap">
        <div class="message-content">
         <div v-for="message in messages" :key="message.id">
          <div v-show='message.senduser == nick' class="user-me">
            <p class="user-me-content">{{ message.message }}</p>
          </div>
          <div v-show='message.senduser != nick' class="user-opponent">
            <!-- <p class="user-me-content">{{ message.senduser }}</p> -->
            <!-- 이미지 보여주기 -->
            <img :src="profileImg" alt="" class="in-img-profile" v-if="profileImg">
            <img src="../assets/images/default-user.png" alt="" class="in-img-content" v-if="!profileImg">
            <p class="in-user-content">{{ message.message }}</p>
          </div>
         </div>         
      </div>
      </div>
      <div class="input-message">
        <input type="textarea" name="" id="" class="input-message-in" placeholder="메세지 보내기..." v-model="text" @keyup.enter="saveMessage">
        <button class="butn" @click="saveMessage">↑</button>
      </div>
    </div>
  </div>
</template>

<script>
import "../components/css/directmessage.css"
import { mapState } from "vuex"
import firebase from 'firebase'
import axios from 'axios'

export default {
  name: 'DirectMessage',
  data() {
    return {
      text: '',
      messages:[],
      authUser:{},
      roomname:'',
      nick: '',
      othername: '',
      profileImg:'',
    }
  },
  computed: {
    ...mapState(['user' ,'flag','dmProfileImg'])
  },
  updated() {
    this.goDown()
    this.defaultDark()
  },
  watch: {
    flag() {
      this.defaultDark()
    }
  },

  methods: {
    saveMessage(){
      //save to firestore
      firebase.firestore().collection(this.roomname).add({
        message: this.text,
        createdAt: firebase.firestore.Timestamp.fromDate(new Date()),
        senduser: this.user.nickname,
      })
      
      //  if (this.text !== '') {
      // //save to firestore
      //   var divParent = document.querySelector('.direct-message-content');
      // db.collection(`chat`).add({
      //   divParent.className = 'direct-message-content';
      //   message: this.text,

      //   createdAt: new Date(),
      //   var div = document.createElement('div');
      //   senduser: this.user.nickname,
      //   div.className = 'user-me'; 
      // })

      //   var p = document.createElement('p');
      //   p.className = 'user-me-content';
      //   p.innerText = this.text;
      this.text = null;
    },
    fetchMessage(){
      firebase.firestore().collection(this.roomname).orderBy('createdAt').onSnapshot((querySnapshot)=>{
       
        let allMessages = [];
        querySnapshot.forEach(doc=>{
          allMessages.push(doc.data());
        })

        this.messages=allMessages;
        this.goDown()
        this.defaultDark()
        
      })
    },
    goProfile() {
      this.$router.push(`/otheruser/${this.othername}`).catch(()=>{})
    },
    goDM() {
      this.$router.go(-1)
    },
 
    defaultDark() {
      const Dark = this.$cookies.get('dark')
      const HTML = document.querySelector('html')
      const wrap = document.querySelector('.wrap')
      const INPUT = document.querySelectorAll('input')
      const PTAG = document.querySelectorAll('.in-user-content, .back-btn-name')

      const MessageDM = document.querySelector('.input-message-in')
      const MessageMe = document.querySelectorAll('.user-me-content')
      
      if (Dark === null) {
        this.$cookies.set('dark', 'on')
      }

      if (Dark === 'off') {
        HTML.classList.add('black')
        wrap.classList.add('wrap-dark')
        for (let i=0; i<INPUT.length ; i++) {
          INPUT[i].classList.add('input-dark')
        }
        for (let i=0; i<PTAG.length ; i++) {
          PTAG[i].classList.add('font-dark')
        }

        MessageDM.classList.add('input-message-in-dark')
        for (let i=0; i<MessageMe.length ; i++) {
          MessageMe[i].classList.add('user-me-content-dark')
        }

      } else {
        HTML.classList.remove('black')
        wrap.classList.remove('wrap-dark')
        for (let i=0; i<INPUT.length ; i++) {
          INPUT[i].classList.remove('input-dark')
        }
        for (let i=0; i<PTAG.length ; i++) {
          PTAG[i].classList.remove('font-dark')
        }

        MessageDM.classList.remove('input-message-in-dark')
        for (let i=0; i<MessageMe.length ; i++) {
          MessageMe[i].classList.remove('user-me-content-dark')
        }
      }
    },
    goDown() {
      const WRAPMESSAGE = document.querySelector('.message-content-wrap')
      if (WRAPMESSAGE) {
        document.querySelector('.message-content-wrap').scrollTop = document.querySelector('.message-content-wrap').scrollHeight;
      }
    },
  },
    
    created(){
    this.roomname = this.$route.params.roomname
    this.othername = this.$route.params.othername
    this.fetchMessage();
    this.profileImg=this.dmProfileImg;
  },
  mounted(){
    this.defaultDark()  
    this.goDown()
    let nickdata = this.$cookies.get('auth-nickname')
    let uri = nickdata;
    let uri_enc = encodeURIComponent(uri);
    let uri_dec = decodeURIComponent(uri_enc);
    let res = uri_dec;
    this.nick = res
      axios.get('https://i3b304.p.ssafy.io/api/mypage/otheruser',{
        params:{
        nickname: this.othername,
      }
      }).then((data)=>{
        if (data.data.userinfo.profile_img) {
          
          this.profileImg = data.data.userinfo.profile_img.substring(2,);
        }
      })
      .catch(
      )
  }
}
</script>
