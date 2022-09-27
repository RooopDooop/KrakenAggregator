import java.time.Instant;
import java.util.Set;

public class instanceProxy {
    private final int Speed;
    private final String IP;
    private final String Port;
    private final long LastCheckup;
    private final String[] Descriptors;
    private final String Location;

    public instanceProxy(int Speed, String IP, String Port, long LastCheckup, String[] Descriptors, String Location) {
        this.Speed = Speed;
        this.IP = IP;
        this.Port = Port;
        this.LastCheckup = LastCheckup;
        this.Descriptors = Descriptors;
        this.Location = Location;
    }

    public String returnIPPort() {
        return this.IP + ":" + this.Port;
    }
}
