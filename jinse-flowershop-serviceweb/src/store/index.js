import { defineStore } from 'pinia'
import router from '@/router'

export const useUserStore = defineStore('user', {
  state: () => ({
    currentPathName: '',
    manager: JSON.parse(localStorage.getItem('manager') || 'null')
  }),

  getters: {
    isLoggedIn: (state) => !!state.manager,
    userName: (state) => state.manager?.name || '',
    avatar: (state) => state.manager?.avatar || ''
  },

  actions: {
    setPath() {
      this.currentPathName = localStorage.getItem('currentPathName') || ''
    },

    setManager(data) {
      this.manager = data
      localStorage.setItem('manager', JSON.stringify(data))
    },

    logout() {
      this.manager = null
      localStorage.removeItem('manager')
      localStorage.removeItem('menus')
      localStorage.removeItem('currentPathName')
      router.push('/login')
    }
  }
})