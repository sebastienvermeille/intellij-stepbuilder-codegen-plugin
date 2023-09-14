package example;

public class RemoteServer {
    private final int port;
    private final String ip;

    private RemoteServer(Builder builder) {
        port = builder.port;
        ip = builder.ip;
    }

    public static IPort builder() {
        return new Builder();
    }

    public interface IBuild {
        RemoteServer build();
    }

    public interface IIp {
        IBuild withIp(String val);
    }

    public interface IPort {
        IIp withPort(int val);
    }

    public static final class Builder implements IIp, IPort, IBuild {
        private String ip;
        private int port;

        private Builder() {
        }

        @Override
        public IBuild withIp(String val) {
            ip = val;
            return this;
        }

        @Override
        public IIp withPort(int val) {
            port = val;
            return this;
        }

        public RemoteServer build() {
            return new RemoteServer(this);
        }
    }
}
