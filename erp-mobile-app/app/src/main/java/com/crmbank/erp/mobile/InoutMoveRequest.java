package com.crmbank.erp.mobile;

import java.util.List;

public class InoutMoveRequest {
    public Inout_MoveDto inout_MoveDto;
    public List<Inout_MoveDtlDto> inout_MoveDtlDto;

    public InoutMoveRequest(Inout_MoveDto mst, List<Inout_MoveDtlDto> dtls) {
        this.inout_MoveDto = mst;
        this.inout_MoveDtlDto = dtls;
    }
}
