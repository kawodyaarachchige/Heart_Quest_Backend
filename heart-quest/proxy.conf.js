/**
 * Proxy config so /api goes to the backend. Rewrites Set-Cookie in responses
 * so the session cookie is stored for localhost and sent on subsequent requests.
 */
const PROXY_CONFIG = {
  "/uob/heart": {
    target: "http://marcconrad.com",
    secure: false,
    changeOrigin: true,
    logLevel: "warn",
  },
  "/api": {
    target: "http://localhost:7070",
    secure: false,
    changeOrigin: true,
    logLevel: "warn",
    onProxyRes(proxyRes) {
      const setCookie = proxyRes.headers["set-cookie"];
      if (!setCookie) return;
      const rewrite = (c) => {
        let s = c.replace(/;\s*Domain=[^;]+/i, "");
        if (!/;\s*Domain=/i.test(s)) {
          s += "; Domain=localhost";
        }
        return s;
      };
      if (Array.isArray(setCookie)) {
        proxyRes.headers["set-cookie"] = setCookie.map(rewrite);
      } else {
        proxyRes.headers["set-cookie"] = rewrite(setCookie);
      }
    },
  },
};

module.exports = PROXY_CONFIG;
