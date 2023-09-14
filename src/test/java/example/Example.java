package example;

public class Example {

    public void example(){

        var remoteServer = RemoteServer.builder()
                .withPort(8080)
                .withIp("42.42.42.42")
                .build();

    }
}
