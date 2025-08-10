package com.knowledge.security;
import com.knowledge.util.JwtUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class SecurityValidator {

    private final RedisTemplate<String, Object> redisTemplate;
    private final JwtUtils jwtUtils;

    @Autowired
    public SecurityValidator(RedisTemplate<String, Object> redisTemplate, JwtUtils jwtUtils) {
        this.redisTemplate = redisTemplate;
        this.jwtUtils = jwtUtils;
    }

    /**
     * 验证用户ID和令牌的匹配性
     *
     * @param userId 用户ID
     * @param token 认证令牌
     * @return 验证结果
     */
    public boolean validateUserToken(Long userId, String token) {
        if (userId == null || token == null || token.isEmpty()) {
            return false;
        }
        try {
            // 2. 验证Token有效性
            if (!jwtUtils.validateToken(token)) {
                return false;
            }

            // 3. 从Token中提取用户ID
            Long tokenUserId = jwtUtils.getUserIdFromToken(token);

            // 4. 验证用户ID匹配
            if (!userId.equals(tokenUserId)) {
                return false;
            }

            // 5. 从Token中提取角色信息
            Claims claims = Jwts.parser()
                    .setSigningKey(jwtUtils.getSecretKey())
                    .parseClaimsJws(token)
                    .getBody();

            Integer role = claims.get("role", Integer.class);

            // 6. 返回验证结果和角色
            return true;

        } catch (Exception e) {
            // 处理所有可能的异常
            return false;
        }
    }

    /**
     * 验证管理员ID和令牌的匹配性
     *
     * @param userId 用户ID
     * @param token 认证令牌
     * @return 验证结果
     */
    public boolean validateAdminToken(Long userId, String token) {
        if (userId == null || token == null || token.isEmpty()) {
            return false;
        }
        try {
            // 2. 验证Token有效性
            if (!jwtUtils.validateToken(token)) {
                return false;
            }

            // 3. 从Token中提取用户ID
            Long tokenUserId = jwtUtils.getUserIdFromToken(token);

            // 4. 验证用户ID匹配
            if (!userId.equals(tokenUserId)) {
                return false;
            }

            // 5. 从Token中提取角色信息
            Claims claims = Jwts.parser()
                    .setSigningKey(jwtUtils.getSecretKey())
                    .parseClaimsJws(token)
                    .getBody();

            Integer role = claims.get("role", Integer.class);

            // 6. 返回验证结果和角色
            return role != 1;

        } catch (Exception e) {
            // 处理所有可能的异常
            return false;
        }
    }
}