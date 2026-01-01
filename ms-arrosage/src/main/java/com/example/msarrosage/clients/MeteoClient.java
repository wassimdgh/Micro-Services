package com.example.msarrosage.clients;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.example.msarrosage.dto.PrevisionResponse;

@FeignClient(name = "${meteo.service-id:ms-meteo}", path = "/api/meteo")
public interface MeteoClient {

    @GetMapping("/previsions/{stationId}")
    List<PrevisionResponse> getPrevisions(@PathVariable("stationId") Long stationId);
}
