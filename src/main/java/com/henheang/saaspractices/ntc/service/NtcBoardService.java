package com.henheang.saaspractices.ntc.service;

import java.util.List;

/**
 * Notice Board Service interface — same pattern as SmpBoardService in saas-olv.
 * Defines WHAT the service can do. HOW is in NtcBoardServiceImpl.
 */
public interface NtcBoardService {

    List<NtcBoardOutVO> selectList(NtcBoardInVO inVO) throws Exception;

    int selectListTotCnt(NtcBoardInVO inVO) throws Exception;

    NtcBoardOutVO selectDetail(NtcBoardInVO inVO) throws Exception;

    void insert(NtcBoardInVO inVO) throws Exception;

    void update(NtcBoardInVO inVO) throws Exception;

    void delete(NtcBoardInVO inVO) throws Exception;

    List<NtcBoardOutVO> selectDeletedList(NtcBoardInVO inVO) throws Exception;

    void restore(NtcBoardInVO inVO) throws Exception;
}
