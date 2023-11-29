package com.uzum.uzum_final_task.controller;

import com.uzum.uzum_final_task.model.CommissionDto;
import com.uzum.uzum_final_task.model.ConversionDto;
import com.uzum.uzum_final_task.model.OfficialRateDto;
import com.uzum.uzum_final_task.service.AppService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping
public class AppController {

    private final AppService appService;

    @GetMapping("/convert")
    public ResponseEntity<ConversionDto> getCalculation(@RequestParam String from, @RequestParam String to, @RequestParam Double amount) {
        return ResponseEntity.ok(appService.getCalculation(from, to, amount));
    }

    @GetMapping("/officialrates")
    public ResponseEntity<OfficialRateDto> getRates(@RequestParam String date, @RequestParam String pair) {
        return ResponseEntity.ok(appService.getOfficialRate(date, pair));
    }

    @PostMapping("/setcommission")
    public ResponseEntity<CommissionDto> setCommission(@RequestHeader("Secret-Key") String secretKey, @RequestBody CommissionDto commissionModel) {
        return ResponseEntity.ok(appService.setCommission(secretKey, commissionModel));
    }

    @PostMapping("/convert")
    public ResponseEntity<ConversionDto> convert(@RequestBody ConversionDto dto) {
        return ResponseEntity.ok(appService.convert(dto));
    }

}
