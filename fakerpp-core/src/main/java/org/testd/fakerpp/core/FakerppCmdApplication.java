package org.testd.fakerpp.core;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.testd.fakerpp.core.util.SeedableThreadLocalRandom;

import javax.annotation.Resource;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@SpringBootApplication
public class FakerppCmdApplication implements ApplicationRunner {

    @Resource
    ERMLExecutor executor;

    public static void main(String[] args) {
        SpringApplication.run(FakerppCmdApplication.class, args);
    }

    private static final String HELP_INFO = "Usage: '--dir=directory' to specify the directory of your fakerpp config";
    private static final String DIR_OPTION = "dir";

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (args.containsOption(DIR_OPTION)){
            args.getOptionValues(DIR_OPTION).forEach(dir -> {
                try {
                    executor.diskExec(Paths.get(dir));
                } catch (ERMLException e) {
                    log.error(e.getMessage());
                }
                log.info("{} execute ok", dir);
            });
        } else {
            log.info(HELP_INFO);
        }
    }
}
