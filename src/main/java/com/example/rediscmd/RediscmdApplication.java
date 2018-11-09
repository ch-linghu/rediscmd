package com.example.rediscmd;

import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
public class RediscmdApplication implements CommandLineRunner {
    class CommandInfo {
        String cmd;
        List<String> args = new ArrayList<>();
    }

    @Autowired
    RedisTemplate<String, String> redisTemplate;

    public static void main(String[] args) {
        SpringApplication.run(RediscmdApplication.class, args);
    }

    private CommandInfo parseCmd(String line) {
        CommandInfo commandInfo = new CommandInfo();

        line = line.trim();

        String[] segs = line.split(" ");
        if (segs == null) {
            commandInfo.cmd = line;
        } else {
            commandInfo.cmd = segs[0];
            boolean quoted = false;
            String quoteStr = "";
            for (int i = 1; i < segs.length; i++) {
                if (StringUtils.startsWithIgnoreCase(segs[i], "\"")) {
                    if (StringUtils.endsWithIgnoreCase(segs[i], "\"")) {
                        commandInfo.args.add(segs[i].substring(1, segs[i].length() - 1));
                    } else {
                        quoted = true;
                        quoteStr = segs[i].substring(1);
                    }
                } else {
                    if (quoted) {
                        if (StringUtils.endsWithIgnoreCase(segs[i], "\"")) {
                            quoted = false;
                            quoteStr += " " + segs[i].substring(0, segs[i].length() - 1);
                            commandInfo.args.add(quoteStr);
                        } else {
                            quoteStr += " " + segs[i];
                        }
                    } else {
                        if (!StringUtils.isEmpty(segs[i])) {
                            commandInfo.args.add(segs[i]);
                        }
                    }
                }

            }
        }
        return commandInfo;
    }

    @Override
    public void run(String... args) throws Exception {
        LineReader reader = LineReaderBuilder.builder().build();
        while (true) {
            String line = reader.readLine("> ");

            CommandInfo commandInfo = parseCmd(line);

            try {
                switch (commandInfo.cmd) {
                    case "quit":
                    case "exit":
                        System.out.println("QUIT!");
                        System.exit(0);

                    case "":
                        break;

                    case "keys": {
                        if (commandInfo.args.size() < 1) {
                            System.out.println("usage: keys PATTERN");
                            continue;
                        }
                        String pattern = commandInfo.args.get(0);

                        int i = 0;
                        for (String key : redisTemplate.keys(pattern)) {
                            System.out.printf("%d): '%s'\n", ++i, key);
                        }
                        if (i == 0) {
                            System.out.println("(empty list or set)");
                        }
                        break;
                    }

                    case "get": {
                        if (commandInfo.args.size() < 1) {
                            System.out.println("usage: get KEY");
                            continue;
                        }
                        String key = commandInfo.args.get(0);

                        String value = redisTemplate.opsForValue().get(key);
                        System.out.println(value == null ? "(nil)" : String.format("'%s'", value));
                        break;
                    }

                    case "hkeys": {
                        if (commandInfo.args.size() < 1) {
                            System.out.println("usage: hkeys KEY");
                            continue;
                        }

                        String key = commandInfo.args.get(0);

                        int i = 0;
                        for (Object hkey : redisTemplate.opsForHash().keys(key)) {
                            System.out.printf("%d): '%s'\n", ++i, hkey);
                        }
                        if (i == 0) {
                            System.out.println("(empty list or set)");
                        }
                        break;
                    }

                    case "hgetall": {
                        if (commandInfo.args.size() < 1) {
                            System.out.println("usage: hgetall KEY");
                            continue;
                        }

                        String key = commandInfo.args.get(0);

                        int i = 0;
                        for (Map.Entry<Object, Object> entry : redisTemplate.opsForHash().entries(key).entrySet()) {
                            System.out.printf("%d): '%s' -> '%s'\n", ++i, entry.getKey(), entry.getValue());
                        }
                        if (i == 0) {
                            System.out.println("(empty list or set)");
                        }
                        break;
                    }

                    case "hget": {
                        if (commandInfo.args.size() < 2) {
                            System.out.println("usage: hgetall KEY FIELD");
                            continue;
                        }

                        String key = commandInfo.args.get(0);
                        String hkey = commandInfo.args.get(1);

                        Object value = redisTemplate.opsForHash().get(key, hkey);
                        System.out.println(value == null ? "(nil)" : String.format("'%s'", value));
                        break;
                    }

                    case "ttl": {
                        if (commandInfo.args.size() < 1) {
                            System.out.println("usage: ttl KEY");
                            continue;
                        }

                        String key = commandInfo.args.get(0);

                        System.out.println("TTL: " + redisTemplate.getExpire(key));
                        break;
                    }

                    case "del": {
                        if (commandInfo.args.size() < 1) {
                            System.out.println("usage: del KEY1 [KEY2] [KEY3]...");
                            continue;
                        }

                        int count = 0;
                        for (String key : commandInfo.args) {
                            if (redisTemplate.delete(key)) {
                                count++;
                            }
                        }

                        System.out.printf("%d keys deleted\n", count);
                        break;
                    }

                    case "hdel": {
                        if (commandInfo.args.size() < 1) {
                            System.out.println("usage: hdel KEY [FIELD1] [FIELD2]...");
                            continue;
                        }

                        String key = commandInfo.args.get(0);

                        int count = 0;
                        for (int i = 1; i < commandInfo.args.size(); i++) {
                            String hkey = commandInfo.args.get(i);

                            count += redisTemplate.opsForHash().delete(key, hkey);
                        }

                        System.out.printf("%d fields deleted\n", count);
                        break;
                    }

                    case "set": {
                        if (commandInfo.args.size() < 2 || commandInfo.args.size() == 3) {
                            System.out.println("usage: set KEY VALUE [expiration EX seconds|PX milliseconds]");
                            continue;
                        }

                        String key = commandInfo.args.get(0);
                        String value = commandInfo.args.get(1);

                        TimeUnit timeUnit = null;
                        Long expireTime = null;
                        if (commandInfo.args.size() >= 4) {
                            String tuStr = commandInfo.args.get(2);

                            if (tuStr.equals("EX") || tuStr.equals("ex")) {
                                timeUnit = TimeUnit.SECONDS;
                            } else if (tuStr.equals("PX") || tuStr.equals("px")) {
                                timeUnit = TimeUnit.MILLISECONDS;
                            } else {
                                System.out.println("usage: set KEY VALUE [expiration EX seconds|PX milliseconds]");
                                continue;
                            }

                            expireTime = Long.parseLong(commandInfo.args.get(3));
                        }

                        if (timeUnit != null && expireTime != null) {
                            redisTemplate.opsForValue().set(key, value, expireTime, timeUnit);
                        } else {
                            redisTemplate.opsForValue().set(key, value);
                        }

                        System.out.println("'OK'");

                        break;
                    }

                    case "hset": {
                        if (commandInfo.args.size() < 3) {
                            System.out.println("usage: hset KEY FIELD VALUE");
                            continue;
                        }

                        String key = commandInfo.args.get(0);
                        String hkey = commandInfo.args.get(1);
                        String value = commandInfo.args.get(2);

                        int result;
                        if (redisTemplate.opsForHash().hasKey(key, hkey)) {
                            result = 0;
                        } else {
                            result = 1;
                        }

                        redisTemplate.opsForHash().put(key, hkey, value);

                        System.out.printf("RESULT: %d\n", result);
                        break;
                    }

                    case "expire": {
                        if (commandInfo.args.size() < 2) {
                            System.out.println("usage: expire KEY SECONDS");
                            continue;
                        }

                        String key = commandInfo.args.get(0);
                        Long timeout = Long.parseLong(commandInfo.args.get(1));


                        int result;
                        if (redisTemplate.hasKey(key)) {
                            result = 1;
                            redisTemplate.expire(key, timeout, TimeUnit.SECONDS);
                        } else {
                            result = 0;
                        }

                        System.out.printf("RESULT: %d\n", result);
                        break;

                    }

                    default:
                        System.out.println("Unknown command");
                        break;
                }

            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
        }
    }
}
