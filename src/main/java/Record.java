public class Record {
    private String lineCode;
    private String routeCode;
    private String vehicleId;
    private double latitude;
    private double longtitude;
    private String timeStampOfBusPosition;

    public Record(String lineCode, String routeCode, String vehicleId, double latitude, double longtitude, String timeStampOfBusPosition) {
        this.lineCode = lineCode;
        this.routeCode = routeCode;
        this.vehicleId = vehicleId;
        this.latitude = latitude;
        this.longtitude = longtitude;
        this.timeStampOfBusPosition = timeStampOfBusPosition;
    }

    public String getLineCode() {
        return lineCode;
    }

    public void setLineCode(String lineCode) {
        this.lineCode = lineCode;
    }

    public String getRouteCode() {
        return routeCode;
    }

    public void setRouteCode(String routeCode) {
        this.routeCode = routeCode;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(String vehicleId) {
        this.vehicleId = vehicleId;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongtitude() {
        return longtitude;
    }

    public void setLongtitude(double longtitude) {
        this.longtitude = longtitude;
    }

    public String getTimeStampOfBusPosition() {
        return timeStampOfBusPosition;
    }

    public void setTimeStampOfBusPosition(String timeStampOfBusPosition) {
        this.timeStampOfBusPosition = timeStampOfBusPosition;
    }

    @Override
    public String toString() {
        return "Record{" +
                "lineCode='" + lineCode + '\'' +
                ", routeCode='" + routeCode + '\'' +
                ", vehicleId='" + vehicleId + '\'' +
                ", latitude=" + latitude +
                ", longtitude=" + longtitude +
                ", timeStampOfBusPosition='" + timeStampOfBusPosition + '\'' +
                '}';
    }
}
