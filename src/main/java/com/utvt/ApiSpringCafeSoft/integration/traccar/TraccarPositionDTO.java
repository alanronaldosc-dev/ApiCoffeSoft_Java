package com.utvt.ApiSpringCafeSoft.integration.traccar;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TraccarPositionDTO {

    private Long id;
    private Long deviceId;
    private Double latitude;
    private Double longitude;
    private Double speed;
    private String deviceTime;
    private String fixTime;

    // nombre del repartidor — se rellena en el service, no viene de Traccar
    private String repartidorNombre;
    private String traccarDeviceId;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getDeviceId() { return deviceId; }
    public void setDeviceId(Long deviceId) { this.deviceId = deviceId; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public Double getSpeed() { return speed; }
    public void setSpeed(Double speed) { this.speed = speed; }

    public String getDeviceTime() { return deviceTime; }
    public void setDeviceTime(String deviceTime) { this.deviceTime = deviceTime; }

    public String getFixTime() { return fixTime; }
    public void setFixTime(String fixTime) { this.fixTime = fixTime; }

    public String getRepartidorNombre() { return repartidorNombre; }
    public void setRepartidorNombre(String repartidorNombre) { this.repartidorNombre = repartidorNombre; }

    public String getTraccarDeviceId() { return traccarDeviceId; }
    public void setTraccarDeviceId(String traccarDeviceId) { this.traccarDeviceId = traccarDeviceId; }
}
