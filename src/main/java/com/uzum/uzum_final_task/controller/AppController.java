package com.uzum.uzum_final_task.controller;

import com.uzum.uzum_final_task.model.CommissionDto;
import com.uzum.uzum_final_task.model.ConversionDto;
import com.uzum.uzum_final_task.model.OfficialRateDto;
import com.uzum.uzum_final_task.service.AppService;
import com.uzum.uzum_final_task.service.CommissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping
public class AppController {

    private final AppService appService;
    private final CommissionService commissionService;

    @GetMapping("/convert")
    public ResponseEntity<ConversionDto> getCalculation(@RequestParam String from, @RequestParam String to, @RequestParam Double amount) {
        return ResponseEntity.ok(appService.getCalculation(from, to, amount));
    }

    @PostMapping("/convert")
    public ResponseEntity<ConversionDto> convert(@RequestBody ConversionDto dto) {
        return ResponseEntity.ok(appService.convert(dto));
    }

    @GetMapping("/officialrates")
    public ResponseEntity<OfficialRateDto> getRates(@RequestParam String date, @RequestParam String pair) {
        return ResponseEntity.ok(appService.getOfficialRate(date, pair));
    }

    @PostMapping("/setcommission")
    public ResponseEntity<CommissionDto> setCommission(@RequestHeader("Secret-Key") String secretKey, @RequestBody CommissionDto commissionModel) {
        return ResponseEntity.ok(commissionService.setCommission(secretKey, commissionModel));
    }

}
