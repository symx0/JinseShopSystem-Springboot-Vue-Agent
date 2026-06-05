<template>
  <footer class="app-footer">
    <div class="footer-inner">
      <div class="footer-top">
        <div class="footer-col">
          <h4>关于锦瑟</h4>
          <p>锦瑟无端五十弦，一弦一柱思华年。我们致力于为每一位顾客提供最新鲜、最美丽的鲜花。</p>
        </div>
        <div class="footer-col">
          <h4>快速链接</h4>
          <router-link to="/home">首页</router-link>
          <router-link to="/flowers">鲜花商城</router-link>
          <router-link to="/activity">热门活动</router-link>
        </div>
        <div class="footer-col">
          <h4>联系我们</h4>
          <p>📍 花城大道520号</p>
          <p>📞 400-888-JINSE</p>
          <p>📧 service@jinseflowers.com</p>
        </div>
      </div>
      <div class="footer-bottom">
        <p>© 2024 锦瑟花店 Jinse Flower Shop. All rights reserved.</p>
      </div>
    </div>
  </footer>
</template>

<style scoped>
.app-footer {
  background: #2D2320;
  color: #B5A39C;
  margin-top: 60px;
}

.footer-inner {
  max-width: 1200px;
  margin: 0 auto;
  padding: 40px 20px 20px;
}

.footer-top {
  display: grid;
  grid-template-columns: 2fr 1fr 1fr;
  gap: 40px;
  padding-bottom: 30px;
  border-bottom: 1px solid rgba(232, 185, 107, 0.15);
}

.footer-col h4 {
  color: #E8B96B;
  font-size: 16px;
  margin-bottom: 16px;
  letter-spacing: 2px;
}

.footer-col p {
  font-size: 13px;
  line-height: 1.8;
}

.footer-col a {
  display: block;
  color: #B5A39C;
  font-size: 13px;
  line-height: 2;
  transition: color 0.3s;
}

.footer-col a:hover {
  color: #E8B96B;
}

.footer-bottom {
  text-align: center;
  padding-top: 20px;
  font-size: 12px;
  color: #8A7A70;
}
</style>