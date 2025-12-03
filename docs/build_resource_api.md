# Build Resource Node / 搭建资源节点
## Use Nginx or other web server to proxy / 使用 Nginx 或其他 Web 服务器代理

> Nginx Example / Nginx 示例
```
# Cache settings / 缓存设置
proxy_cache_path /var/cache/nginx/r2_proxy levels=1:2 keys_zone=r2_cdn:20m max_size=20g
                 inactive=7d use_temp_path=off;

server {
    listen 80;
    server_name example.domain.com; # Your domain / 你的域名

    # Force HTTPS redirect / 强制跳转 HTTPS（推荐）
    return 301 https://$host$request_uri;
}

server {
    # HTTPS
    listen 443 ssl http2;
    server_name example.domain.com; # Your domain / 你的域名

    # Your certificate / 你的证书
    ssl_certificate /etc/letsencrypt/live/maimai-assets-cdn-aliyun.skydynamic.top/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/maimai-assets-cdn-aliyun.skydynamic.top/privkey.pem;

    # Cache log / 缓存日志标识
    add_header X-Cache $upstream_cache_status;

    location / {
        # Enable cache / 启用缓存
        proxy_cache r2_cdn;
        proxy_cache_valid 200 302 24h;     # 成功响应缓存 24 小时
        proxy_cache_valid 404 5m;          # 404 缓存 5 分钟
        proxy_cache_use_stale error timeout updating http_500 http_502 http_503 http_504;
        proxy_cache_revalidate on;

        # 代理到你已有的 R2 自定义域名
        proxy_pass https://maimai-assets.skydynamic.top;

        # 透传 Host 头（必须！否则 Cloudflare 无法识别）
        proxy_set_header Host maimai-assets.skydynamic.top;

        # 保留客户端信息
        proxy_set_header User-Agent $http_user_agent;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header Accept-Encoding "";  # 防止压缩干扰缓存

	    # SSL 设置
        proxy_ssl_server_name on;
        proxy_ssl_protocols TLSv1.2 TLSv1.3;
        proxy_ssl_ciphers ECDHE+AESGCM:ECDHE+CHACHA20:DHE+AESGCM:DHE+CHACHA20:!aNULL:!MD5:!DSS;

        # 超时设置
        proxy_connect_timeout 10s;
        proxy_send_timeout 30s;
        proxy_read_timeout 30s;
    }
}
```

## Custom Server / 自定义服务器

> Custom Server Notice / 自定义服务器注意事项
> 1. Your server should implement path: `/{path}/{resId}.png` / 你的服务器应该实现路径格式：`/{path}/{resId}.png`
> 2. Return `404` if the resource does not exist / 如果资源不存在则返回 `404`
> 3. Image format: `png` / 图片格式：`png`
> 4. Resource ID must be music common ID, not distinction music type / 资源 ID 必须是乐曲通用 ID, 不区分乐曲类型

API Path: `plate` / `frame` / `jacket` / `icon`