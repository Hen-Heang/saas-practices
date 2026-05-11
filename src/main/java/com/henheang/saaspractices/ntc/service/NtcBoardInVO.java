package com.henheang.saaspractices.ntc.service;

import com.henheang.saaspractices.cmm.BaseVO;

import java.io.Serial;

/**
 * Notice Board InVO — practice equivalent of SmpBoardInVO in saas-olv.
 * Holds all INPUT from browser → server (search, form fields, PK).
 * Extends BaseVO = the same as extending CmmVO in saas-olv.
 */
public class NtcBoardInVO extends BaseVO {

    @Serial
    private static final long serialVersionUID = 1L;

    /* ── Table columns (co_notice_m) ── */

    /** PK — DB: notice_sn */
    private long noticeSn;

    /** Title — DB: notice_title */
    private String noticeTitle;

    /** Content — DB: notice_cn */
    private String noticeCn;

    /** Use Y/N — DB: use_yn */
    private String useYn = "Y";

    /* ── Getters / Setters ── */

    public long getNoticeSn() { return noticeSn; }
    public void setNoticeSn(long noticeSn) { this.noticeSn = noticeSn; }

    public String getNoticeTitle() { return noticeTitle; }
    public void setNoticeTitle(String noticeTitle) { this.noticeTitle = noticeTitle; }

    public String getNoticeCn() { return noticeCn; }
    public void setNoticeCn(String noticeCn) { this.noticeCn = noticeCn; }

    public String getUseYn() { return useYn; }
    public void setUseYn(String useYn) { this.useYn = useYn; }
}
