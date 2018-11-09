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
            for (int i = 1; i < segs.length; i++) {
                if (!StringUtils.isEmpty(segs[i])) {
                    commandInfo.args.add(segs[i]);
                }
            }
        }
        return commandInfo;
    }

    @Override
    public void run(String... args) throws Exception {
        LineReader reader = LineReaderBuilder.builder().build();
        while (true) {
            String line  = reader.readLine("> ");

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

                        System.out.printf("'%s'\n", redisTemplate.opsForValue().get(key));
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
                            System.out.println("usage: hgetall KEY HKEY");
                            continue;
                        }

                        String key = commandInfo.args.get(0);
                        String hkey = commandInfo.args.get(1);

                        System.out.printf("'%s'\n", redisTemplate.opsForValue().get(key));
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
