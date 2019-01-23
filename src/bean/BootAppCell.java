package bean;

public class BootAppCell {
    public static final String STATUS_RUNNING = "running";
    public static final String STATUS_STOPPED = "stopped";

    public static final String TEXT_RUN = "run";
    public static final String TEXT_STOP = "stop";

    String name;
    String path;
    String status;
    String buttonText;
    String port;
    String url;

    public BootAppCell() {
    }

    public BootAppCell(String name, String path, String status, String buttonText, String port, String url) {
        this.name = name;
        this.path = path;
        this.status = status;
        this.buttonText = buttonText;
        this.port = port;
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getButtonText() {
        return buttonText;
    }

    public void setButtonText(String buttonText) {
        this.buttonText = buttonText;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return "BootAppCell{" +
                "name='" + name + '\'' +
                ", path='" + path + '\'' +
                ", status='" + status + '\'' +
                ", buttonText='" + buttonText + '\'' +
                ", port='" + port + '\'' +
                ", url='" + url + '\'' +
                '}';
    }
}
