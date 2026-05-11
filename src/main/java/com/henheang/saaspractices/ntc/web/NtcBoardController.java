package com.henheang.saaspractices.ntc.web;

import com.henheang.saaspractices.cmm.PaginationVO;
import com.henheang.saaspractices.ntc.service.NtcBoardInVO;
import com.henheang.saaspractices.ntc.service.NtcBoardOutVO;
import com.henheang.saaspractices.ntc.service.NtcBoardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * Notice Board Controller — same pattern as SmpBoardController in saas-olv.
 * <p>
 * URL pattern: /ntcBoard/{action}.do
 * <p>
 * saas-olv uses AJAX list (CommAjax).
 * This practice uses simple server-side rendering to keep it easy to understand first.
 * Once you understand the flow, you can add AJAX later.
 */
@Controller
public class NtcBoardController {

    private static final Logger log = LoggerFactory.getLogger(NtcBoardController.class);

    @Autowired
    private NtcBoardService ntcBoardService;

    /**
     * List page — loads HTML + data together (server-side rendering)
     * In saas-olv this is split into a list. Do (HTML) + selectList. Do (AJAX JSON)
     */
    @RequestMapping("/ntcBoard/list.do")
    public String selectList(
            @ModelAttribute("searchVO") NtcBoardInVO inVO,
            ModelMap model) throws Exception {

        log.debug("=== selectList called: page={}, condition={}, keyword={}",
                inVO.getPageIndex(), inVO.getSearchCondition(), inVO.getSearchKeyword());

        List<NtcBoardOutVO> resultList = ntcBoardService.selectList(inVO);
        int totCnt = ntcBoardService.selectListTotCnt(inVO);

        log.debug("=== selectList result: totCnt={}, returned={}", totCnt, resultList.size());

        PaginationVO paging = new PaginationVO(totCnt, inVO.getPageIndex(), inVO.getRecordCountPerPage());

        model.addAttribute("resultList", resultList);
        model.addAttribute("totCnt", totCnt);
        model.addAttribute("searchVO", inVO);
        model.addAttribute("paging", paging);

        return "ntc/NtcBoardList";
    }

    /**
     * Detail page — loads one record by PK
     */
    @RequestMapping("/ntcBoard/detail.do")
    public String selectDetail(
            @RequestParam("noticeSn") long noticeSn,
            @ModelAttribute("searchVO") NtcBoardInVO searchVO,
            ModelMap model) throws Exception {

        NtcBoardInVO inVO = new NtcBoardInVO();
        inVO.setNoticeSn(noticeSn);
        NtcBoardOutVO outVO = ntcBoardService.selectDetail(inVO);

        model.addAttribute("ntcBoardVO", outVO);
        return "ntc/NtcBoardUpdt";
    }

    /**
     * Register page
     */
    @RequestMapping("/ntcBoard/insertView.do")
    public String insertView(
            @ModelAttribute("ntcBoardVO") NtcBoardInVO inVO) throws Exception {
        return "ntc/NtcBoardRegist";
    }

    /**
     * Register process — redirect after success (PRG pattern, same as saas-olv)
     */
    @RequestMapping("/ntcBoard/insert.do")
    public String insert(
            @ModelAttribute("ntcBoardVO") NtcBoardInVO inVO) throws Exception {

        inVO.setDataRegId("practice_user");
        ntcBoardService.insert(inVO);
        return "redirect:/ntcBoard/list.do";
    }

    /**
     * Update process
     */
    @RequestMapping("/ntcBoard/update.do")
    public String update(
            @ModelAttribute("ntcBoardVO") NtcBoardInVO inVO) throws Exception {

        inVO.setDataChgId("practice_user");
        ntcBoardService.update(inVO);
        return "redirect:/ntcBoard/list.do";
    }

    /**
     * Delete a process
     */
    @RequestMapping("/ntcBoard/delete.do")
    public String delete(
            @ModelAttribute("ntcBoardVO") NtcBoardInVO inVO) throws Exception {

        inVO.setDataChgId("practice_user");
        ntcBoardService.delete(inVO);
        return "redirect:/ntcBoard/list.do";
    }

    /**
     * Deleted records list — shows use_yn = 'N' records only
     * 삭제된 목록 / Deleted List
     */
    @RequestMapping("/ntcBoard/deletedList.do")
    public String selectDeletedList(
            @ModelAttribute("searchVO") NtcBoardInVO inVO,
            ModelMap model) throws Exception {

        List<NtcBoardOutVO> deletedList = ntcBoardService.selectDeletedList(inVO);
        model.addAttribute("deletedList", deletedList);
        return "ntc/NtcBoardDeletedList";
    }

    /**
     * Restore — set use_yn back to 'Y'
     * 복원 처리
     */
    @RequestMapping("/ntcBoard/restore.do")
    public String restore(
            @ModelAttribute("ntcBoardVO") NtcBoardInVO inVO) throws Exception {

        inVO.setDataChgId("practice_user");
        ntcBoardService.restore(inVO);
        return "redirect:/ntcBoard/deletedList.do";
    }
}
