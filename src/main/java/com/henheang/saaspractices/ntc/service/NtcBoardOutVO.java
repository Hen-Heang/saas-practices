package com.henheang.saaspractices.ntc.service;

import java.io.Serial;
import java.io.Serializable;

/**
 * Notice Board OutVO — practice equivalent of SmpBoardOutVO in saas-olv.
 * Holds all OUTPUT from DB → browser (query results only).
 * Does NOT extend BaseVO — just Serializable. Keep it lightweight.
 */
public class NtcBoardOutVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /* ── Table columns (co_notice_m) ── */

    private long noticeSn;
    private String noticeTitle;
    private String noticeCn;
    private String useYn;

    /* ── Audit columns (for display only) ── */

    private String dataRegId;
    private String dataRegDt;
    private String dataChgId;
    private String dataChgDt;

    /* ── Getters / Setters ── */

    public long getNoticeSn() { return noticeSn; }
    public void setNoticeSn(long noticeSn) { this.noticeSn = noticeSn; }

    public String getNoticeTitle() { return noticeTitle; }
    public void setNoticeTitle(String noticeTitle) { this.noticeTitle = noticeTitle; }

    public String getNoticeCn() { return noticeCn; }
    public void setNoticeCn(String noticeCn) { this.noticeCn = noticeCn; }

    public String getUseYn() { return useYn; }
    public void setUseYn(String useYn) { this.useYn = useYn; }

    public String getDataRegId() { return dataRegId; }
    public void setDataRegId(String dataRegId) { this.dataRegId = dataRegId; }

    public String getDataRegDt() { return dataRegDt; }
    public void setDataRegDt(String dataRegDt) { this.dataRegDt = dataRegDt; }

    public String getDataChgId() { return dataChgId; }
    public void setDataChgId(String dataChgId) { this.dataChgId = dataChgId; }

    public String getDataChgDt() { return dataChgDt; }
    public void setDataChgDt(String dataChgDt) { this.dataChgDt = dataChgDt; }
}
