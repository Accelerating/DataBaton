package io.databaton;

import io.databaton.net.databaton.DataBatonServer;
import io.databaton.net.databaton.DataBatonContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DataBatonApplication implements CommandLineRunner {

    @Autowired
    private DataBatonContext dataBatonContext;

    public static void main(String[] args) {
        SpringApplication.run(DataBatonApplication.class);

    }

    @Override
    public void run(String... args) throws Exception {
        DataBatonServer server = dataBatonContext.createDataBatonServer();
        server.start();
    }
}
