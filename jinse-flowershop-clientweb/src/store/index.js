import { defineStore } from 'pinia'
import router from '@/router'

export const useUserStore = defineStore('user', {
  state: () => ({
    user: JSON.parse(localStorage.getItem('user') || 'null')
  }),

  getters: {
    isLoggedIn: (state) => !!state.user && !!state.user.token,
    userName: (state) => state.user?.name || state.user?.username || '',
    userId: (state) => state.user?.id || null
  },

  actions: {
    setUser(data) {
      this.user = data
      localStorage.setItem('user', JSON.stringify(data))
    },

    logout() {
      this.user = null
      localStorage.removeItem('user')
      router.push('/login')
    }
  }
})
