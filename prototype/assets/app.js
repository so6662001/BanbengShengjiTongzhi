// 共享后台布局：自动注入侧边栏 + 顶栏。各页面只需声明 data-active 与 data-crumb。
(function () {
  const nav = [
    { group: "概览", items: [
      { key: "dashboard", icon: "📊", text: "数据看板", href: "admin-dashboard.html" },
    ]},
    { group: "内容管理", items: [
      { key: "version", icon: "🗂️", text: "版本管理", href: "admin-version.html" },
      { key: "server", icon: "🖥️", text: "服务器管理", href: "admin-server.html" },
      { key: "audience", icon: "🎯", text: "客户分层", href: "admin-audience.html" },
    ]},
    { group: "发布与审批", items: [
      { key: "release", icon: "🚀", text: "发布流程", href: "admin-release.html" },
      { key: "approval", icon: "✅", text: "审批中心", href: "admin-approval.html" },
    ]},
    { group: "客户端预览", items: [
      { key: "popup", icon: "🔔", text: "更新弹窗", href: "client-popup.html" },
      { key: "changelog", icon: "📜", text: "更新日志页", href: "client-changelog.html" },
    ]},
  ];

  function renderAdmin(root) {
    const active = root.dataset.active || "";
    const crumb = root.dataset.crumb || "";
    const navHtml = nav.map(g => `
      <div class="nav-group-title">${g.group}</div>
      ${g.items.map(it => `
        <a class="nav-item ${it.key === active ? "active" : ""}" href="${it.href}">
          <span class="ico">${it.icon}</span><span>${it.text}</span>
        </a>`).join("")}
    `).join("");

    root.innerHTML = `
      <div class="layout">
        <aside class="sidebar">
          <div class="brand">
            <span class="logo">🛰️</span>
            <span>版本通知中心<small>Release Notify Center</small></span>
          </div>
          ${navHtml}
          <div class="foot">v0.1 原型 · 演示数据</div>
        </aside>
        <div class="main">
          <div class="topbar">
            <div class="crumb">版本更新通知工具 / <b>${crumb}</b></div>
            <div class="tools">
              <a class="tool-ico" href="index.html" title="返回原型导航">🏠</a>
              <div class="tool-ico" title="通知">🔔</div>
              <div class="tool-ico" title="帮助">❓</div>
              <div class="avatar">研</div>
            </div>
          </div>
          <div class="content">${root.dataset.content || root.innerHTML}</div>
        </div>
      </div>`;
  }

  window.addEventListener("DOMContentLoaded", () => {
    const root = document.getElementById("admin-root");
    if (root) {
      // 把页面正文保存后再注入布局
      const inner = root.innerHTML;
      root.dataset.content = inner;
      renderAdmin(root);
    }
    // 原型交互：点击 chip 切换选中态（演示用）
    document.body.addEventListener("click", (e) => {
      const chip = e.target.closest(".chip");
      if (chip && !chip.style.opacity) chip.classList.toggle("on");
    });
  });
})();
