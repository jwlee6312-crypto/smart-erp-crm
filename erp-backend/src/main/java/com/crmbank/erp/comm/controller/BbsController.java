package com.crmbank.erp.comm.controller;

import com.crmbank.erp.comm.dto.UserSession;
import com.crmbank.erp.comm.service.BbsService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/bbs")
@RequiredArgsConstructor
public class BbsController {

    private final BbsService bbsService;

    @GetMapping("/list")
    public ResponseEntity<?> getList(@RequestParam Map<String, Object> params, HttpSession session) {
        UserSession user = (UserSession) session.getAttribute("user_session");
        if (user == null) return ResponseEntity.status(401).build();
        params.put("cmpycd", user.getCmpycd());
        return ResponseEntity.ok(bbsService.getBbsList(params));
    }

    @GetMapping("/detail")
    public ResponseEntity<?> getDetail(@RequestParam Map<String, Object> params, HttpSession session) {
        UserSession user = (UserSession) session.getAttribute("user_session");
        if (user == null) return ResponseEntity.status(401).build();
        params.put("cmpycd", user.getCmpycd());
        return ResponseEntity.ok(bbsService.getBbsDetail(params));
    }

    @PostMapping("/save")
    public ResponseEntity<?> save(@RequestBody Map<String, Object> params, HttpSession session) {
        UserSession user = (UserSession) session.getAttribute("user_session");
        if (user == null) return ResponseEntity.status(401).build();
        params.put("cmpycd", user.getCmpycd());
        params.put("userid", user.getUserid());
        params.put("deptcd", user.getDeptcd());
        bbsService.saveBbs(params);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/delete")
    public ResponseEntity<?> delete(@RequestBody Map<String, Object> params, HttpSession session) {
        UserSession user = (UserSession) session.getAttribute("user_session");
        if (user == null) return ResponseEntity.status(401).build();
        params.put("cmpycd", user.getCmpycd());
        bbsService.deleteBbs(params);
        return ResponseEntity.ok().build();
    }
}
