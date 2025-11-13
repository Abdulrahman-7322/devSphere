---
-- 原子性地获取并删除一个 Redis 键。
--
-- @param KEYS[1] string - 需要操作的键名。
-- @return string - 如果键存在，返回其对应的值；如果键不存在，返回 nil。

-- 1. 尝试获取键的值
local value = redis.call('GET', KEYS[1])

-- 2. 检查值是否存在 (在 Lua 中，nil 表示不存在)
if value then
    -- 3. 如果值存在，则删除这个键
    redis.call('DEL', KEYS[1])
end

-- 4. 返回获取到的值。如果键原先不存在，这里返回的就是 nil
return value